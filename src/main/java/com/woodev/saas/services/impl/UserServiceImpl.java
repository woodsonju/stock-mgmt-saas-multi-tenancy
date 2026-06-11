package com.woodev.saas.services.impl;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.config.TenantContext;
import com.woodev.saas.entities.User;
import com.woodev.saas.entities.UserRole;
import com.woodev.saas.exceptions.DuplicateResourceException;
import com.woodev.saas.exceptions.InvalidRequestException;
import com.woodev.saas.mappers.UserMapper;
import com.woodev.saas.repositories.TenantRepository;
import com.woodev.saas.repositories.UserRepository;
import com.woodev.saas.requests.UserRequest;
import com.woodev.saas.responses.UserResponse;
import com.woodev.saas.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
                                    //        ↑
                                    // Hérite de l'interface Spring Security
                                    // → loadUserByUsername() obligatoire
                                    // → Utilisé par AuthenticationManager

    private final UserRepository userRepository;

    // Vérifier que le tenant existe avant de créer/modifier un user
    //Lors de la création d'un user :
    //  → On doit vérifier que son tenant existe
    //  → Un user DOIT appartenir à un tenant
    //  → Même logique que categoryId dans ProductService
    private final TenantRepository tenantRepository;

    // DTO ↔ Entity
    private final UserMapper userMapper;

    // BCrypt pour hasher les passwords
    private final PasswordEncoder passwordEncoder;

    //Spring l'utilise AUTOMATIQUEMENT en coulisses !
    //Tu ne l'appelles JAMAIS directement.
    // Utilisée AUTOMATIQUEMENT par Spring Security
    // lors de authenticationManager.authenticate()
    //Spring Security la trouve et l'utilise quand
    //authenticationManager.authenticate() est appelé.
    //Quand tu fais :
    //
    //authenticationManager.authenticate(
    //    new UsernamePasswordAuthenticationToken(
    //        "bob",
    //        "MotDePasse123"
    //    )
    //)
    //         │
    //         ▼
    //Spring Security utilise DaoAuthenticationProvider
    //(par défaut)
    //         │
    //         ▼
    //DaoAuthenticationProvider cherche :
    //  ① UserDetailsService → trouvé : UserServiceImpl
    //  ② PasswordEncoder    → BCrypt (que tu dois déclarer)
    //         │
    //         ▼
    //DaoAuthenticationProvider exécute :
    //  ① userDetailsService.loadUserByUsername("bob")
    //     → APPEL CACHÉ à TON UserServiceImpl !
    //     → Récupère le User en DB
    //  ② passwordEncoder.matches(
    //       "MotDePasse123",
    //       user.getPassword()
    //     )
    //     → Vérifie le password
    //         │
    //         ▼
    //Si tout OK → Authentication retournée
    //Sinon → BadCredentialsException
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user was found with : " + username));
    }

    //createUser() fait 5 étapes :
    //Récupérer le tenant courant
    //Vérifier username unique
    //Vérifier email unique
    //Vérifier rôle non PLATFORM_ADMIN
    //Créer et sauvegarder le user
    @Override
    public void createUser(UserRequest request) {

        // Pourquoi récupérer le tenantId ici ?
        // → On est en mode multi-tenant (Approche 2)
        // → Le user créé appartient au tenant courant
        // → tenantId servira à lier le user à son tenant

        // TenantContext contient le tenant du JWT :
        // JwtAuthenticationFilter → setCurrentTenant("abc-123")
        // → tenantId = "abc-123"
        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating user for tenant : {}", tenantId);

        //validate if username exists
        if(this.userRepository.existsByUsername(request.getUsername())) {
            log.debug("Username already exists");
            throw new DuplicateResourceException("Username already exists");
        }

        //check if email exists
        if(this.userRepository.existsByEmail(request.getEmail())) {
            log.debug("Email already exists");
            throw new DuplicateResourceException("Email already exists");
        }
        // POURQUOI cette vérification ?
        // ────────────────────────────────────────
        // PLATFORM_ADMIN = toi (créateur du SaaS)
        // → Un tenant NE PEUT PAS créer un PLATFORM_ADMIN !
        // → Ce serait une élévation de privilèges !
        //
        // Un COMPANY_ADMIN de Woodev ne peut pas créer
        // un user avec ROLE_PLATFORM_ADMIN qui aurait
        // accès à TOUS les tenants !
        //Validate role (cannot be PLATFORM_ADMIN)
        if(request.getRole() == UserRole.ROLE_PLATFORM_ADMIN) {
            log.debug("Role cannot be PLATFORM_ADMIN");
            throw new InvalidRequestException("Role cannot be PLATFORM_ADMIN");
        }

        final User user = this.userMapper.toEntity(request);

        //Hasher après le mapping
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        this.userRepository.save(user);

        log.info("User created successfully");

    }

    //updateUser() fait 6 étapes :
    //Récupérer le tenant courant
    //Vérifier que le user existe
    //Vérifier que le user appartient au tenant
    //Vérifier username unique (si changé)
    //Vérifier email unique (si changé)
    //rôle non PLATFORM_ADMIN
    //ettre à jour et sauvegarder
    @Override
    public void updateUser(String userId, UserRequest request) {

        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating user for tenant : {}", tenantId);

        //Verifier que l'utilisateur existe et n'a pas été supprimé (suppression logique)
        final User user = this.userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        //Vérifier l'appartenance au tenant
        //check if user belongs to tenant
        if(!user.getTenant().getId().equals(tenantId)) {
            log.debug("User does not belong to tenant");
            throw new InvalidRequestException("User does not belong to tenant");
        }

        //check if username is being changed and if it is already taken
        if(!request.getUsername().equals(user.getUsername()) && this.userRepository.existsByUsername(request.getUsername())) {
            log.debug("Username already exists");
            throw new DuplicateResourceException("Username already exists");
        }

        //check if email is being changed and if it is already taken
        if(!request.getEmail().equals(user.getEmail()) && this.userRepository.existsByEmail(request.getEmail())) {
            log.debug("Email already exists");
            throw new DuplicateResourceException("Email already exists");
        }

        //Validate role (cannot be PLATFORM_ADMIN)
        if(request.getRole() == UserRole.ROLE_PLATFORM_ADMIN) {
            log.debug("Role cannot be PLATFORM_ADMIN");
            throw new InvalidRequestException("Role cannot be PLATFORM_ADMIN");
        }

        //Update user details
        this.userMapper.toEntity(request);

        this.userRepository.save(user);

        log.info("User updated successfully");

    }

    //deleteUser() fait 4 étapes :
    //Récupérer le tenant courant
    //Vérifier que le user existe
    //Vérifier que le user appartient au tenant
    //Soft delete (deleted = true)
    @Override
    public void deleteUser(String userId) {

        //Tenant courant
        final String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting user for tenant : {}", tenantId);

        //Verifier que l'utilisateur existe et n'a pas été supprimé (suppression logique)
        final User user = this.userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        // User appartient au tenant ?
        //check if user belongs to tenant
        if(!user.getTenant().getId().equals(tenantId)) {
            log.debug("User does not belong to tenant");
            throw new InvalidRequestException("User does not belong to tenant");
        }

        //On ne supprime pas l'utilisateur physiquement
        //soft delete user (suppression logique)
        user.setDeleted(true);
        this.userRepository.save(user);

        log.info("User deleted successfully");
    }

    @Override
    public UserResponse getUserById(String userId) {

        // Chercher user existant ET non supprimé
        // Si deleted = true → "User does not exist"
        // Si inexistant → "User does not exist"
        final User user = this.userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        // Vérifier appartenance au tenant
        //check if user belongs to tenant
        if(!user.getTenant().getId().equals(TenantContext.getCurrentTenant())) {
            log.debug("User does not belong to tenant");
            throw new InvalidRequestException("User does not belong to tenant");
        }

        // Mapper et retourner
        return this.userMapper.toResponse(user) ;
    }

    //getAllUsers() = Liste paginée des users d'UN seul tenant
    //
    //Utilise :
    //→ TenantContext pour le tenantId
    //→ findAllByTenantId() avec pagination
    //→ Map en UserResponse
    //→ Retourne PageResponse
    @Override
    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        // Récupérer le tenant courant
        final String tenantId = TenantContext.getCurrentTenant();
        // Créer la pagination
        final PageRequest pageRequest = PageRequest.of(page, size);
        //Hibernate → SET search_path = tenant_woodev
        //findAll() → SELECT * FROM users
        //          → tenant_woodev.users
        //          → Seulement les users de Woodev
        //
        //"On n'a pas besoin de filtrer par tenantId
        // car le schéma est déjà isolé !"
        //
        //MAIS on utilise quand même findAllByTenantId() :
        //  → Couche de sécurité SUPPLÉMENTAIRE
        //  → Double protection :
        //    ① search_path → bon schéma
        //    ② WHERE tenant.id = tenantId → confirmation
        //
        //→ Défense en profondeur
        // Chercher les users du tenant
        Page<User> userPage = this.userRepository.findAllByTenantId(tenantId, pageRequest);
        // Mapper en UserResponse
        //Page<UserResponse> userResponsePage = userPage.map(user -> userMapper.toResponse(user));
        Page<UserResponse> userResponsePage = userPage.map( userMapper::toResponse);

        return PageResponse.of(userResponsePage);
    }



    @Override
    public void enableUser(String userId) {
        final String tenantId = TenantContext.getCurrentTenant();
        final User user = this.userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        //check if user belongs to tenant
        if(!user.getTenant().getId().equals(tenantId)) {
            log.debug("User does not belong to tenant");
            throw new InvalidRequestException("User does not belong to tenant");
        }

        user.setEnabled(true);
        this.userRepository.save(user);
        log.info("User enabled successfully");

    }


    @Override
    public void disableUser(String userId) {
        final String tenantId = TenantContext.getCurrentTenant();
        final User user = this.userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist"));

        //check if user belongs to tenant
        if(!user.getTenant().getId().equals(tenantId)) {
            log.debug("User does not belong to tenant");
            throw new InvalidRequestException("User does not belong to tenant");
        }

        user.setEnabled(false);
        this.userRepository.save(user);
        log.info("User disable successfully");
    }
}
