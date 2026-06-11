package com.woodev.saas.controllers;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.requests.UserRequest;
import com.woodev.saas.responses.UserResponse;
import com.woodev.saas.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //@PreAuthorize = Contrôle d'accès au niveau méthode
    //"Pas n'importe quel utilisateur doit pouvoir créer des utilisateurs, il faut que cet
    // utilisateur ait le rôle COMPANY_ADMIN"

    //@PreAuthorize("hasRole('COMPANY_ADMIN')")
    // hasRole('COMPANY_ADMIN')
    // → Spring ajoute automatiquement le préfixe ROLE_
    // → Cherche "ROLE_COMPANY_ADMIN" dans les authorities
    // → Si trouvé → accès autorisé ✅
    // → Si non trouvé → HTTP 403 Forbidden 🚫

    // Équivalent à :
        // hasAuthority('ROLE_COMPANY_ADMIN')
        // ← sans ajout automatique du préfixe

        // Dans le JWT :
    // "role": "ROLE_COMPANY_ADMIN"
    // → JwtAuthenticationFilter crée :
    //   new SimpleGrantedAuthority("ROLE_COMPANY_ADMIN")
    // → hasRole('COMPANY_ADMIN')

    // Comment @PreAuthorize fonctionne
    //Request :
    //POST /api/v1/users
    //Authorization: Bearer eyJhbGc...
    //         │
    //         ▼
    //JwtAuthenticationFilter :
    //  role = "ROLE_COMPANY_ADMIN"
    //  SecurityContextHolder.setAuthentication(
    //    authorities = [ROLE_COMPANY_ADMIN]
    //  )
    //         │
    //         ▼
    //@PreAuthorize("hasRole('COMPANY_ADMIN')") :
    //  → Vérifie authorities dans SecurityContext
    //  → ROLE_COMPANY_ADMIN trouvé ? ✅
    //     → Continue → createUser() ✅
    //  → ROLE_USER trouvé ?
    //     → AccessDeniedException → 403 Forbidden 🚫
    @PostMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> createUser(
            @Valid
            @RequestBody
            final UserRequest userRequest){

        this.userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    // hasAnyRole() = "Au moins UN de ces rôles suffit"
    // → COMPANY_ADMIN OU ADMINISTRATOR → accès ✅
    // → ROLE_USER → 403 Forbidden 🚫
    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'ADMINISTRATOR')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0")
            final int page,
            @RequestParam(name = "size", defaultValue = "10")
            final int size
    ) {
        final PageResponse<UserResponse> response = this.userService.getAllUsers(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{user-id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("user-id")
            final String id) {
        final UserResponse response = this.userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Void> updateUser(
            @PathVariable("user-id")
            String id,
            @Valid
            @RequestBody
            final UserRequest request
    ) {
        this.userService.updateUser(id, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{user-id}")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("user-id")
            String id
    ) {
        this.userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // PUT = remplacement COMPLET d'une ressource
    // On ne remplace pas un user entier !
    // PATCH = modification PARTIELLE ✅
    // On change juste enabled = true
    @PatchMapping("/{user-id}/enable")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> enableUser(
            @PathVariable("user-id")
            String id
    ) {
        this.userService.enableUser(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PatchMapping("/{user-id}/disable")
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<Void> disableUser(
            @PathVariable("user-id")
            String id
    ) {
        this.userService.disableUser(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


}
