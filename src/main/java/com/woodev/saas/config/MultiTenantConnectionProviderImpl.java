package com.woodev.saas.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

//MultiTenantConnectionProvider =
//"Je fournis à Hibernate une connexion DB
// pointant vers le BON schéma

//CurrentTenantIdentifierResolver dit :
//"Le schéma courant est tenant_woodev"
//         │
//         ▼
//Hibernate dit :
//"OK, je connais le schéma...
// MAIS comment je me connecte dessus ?"
//         │
//         ▼
//MultiTenantConnectionProvider répond :
//"Je te donne une connexion configurée
// pour tenant_woodev !"

//TenantContext                       Stocke le schéma
//                                    dans ThreadLocal
//
//TenantSchemaResolver                Traduit tenantId
//                                    → "tenant_woodev"
//
//CurrentTenantIdentifierResolver     Dit à Hibernate
//                                    quel schéma utiliser
//
//MultiTenantConnectionProvider  ←    Fournit la connexion
//                                    pointant vers ce schéma
//
//PostgreSQL                          Exécute dans
//                                    le bon schéma
@Component
@RequiredArgsConstructor
@Slf4j
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {

    // La source de données configurée
    // Dans application.yml
    private final DataSource dataSource;



    //Ouvrir une connexion
    /**
     * Allows access to the database metadata of the underlying database(s) in situations
     * where we do not have a tenant id (like startup processing, for example).
     *
     * @return The database metadata.
     * @throws SQLException Indicates a problem opening a connection
     */
    @Override
    public Connection getAnyConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    //Fermer la connexion
    /**
     * Release a connection obtained from {@link #getAnyConnection}
     *
     * @param connection The JDBC connection to release
     * @throws SQLException Indicates a problem closing the connection
     */
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    //Obtenir une connexion pour un tenant
    /**
     * Obtains a connection for use according to the underlying strategy of this provider.
     *
     * @param tenantIdentifier The identifier of the tenant for which to get a connection
     * @return The obtained JDBC connection
     * @throws SQLException       Indicates a problem opening a connection
     * @throws HibernateException Indicates a problem obtaining a connection
     */
    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        log.debug("Obtaining connection for tenant: {}", tenantIdentifier);
        final Connection connection = getAnyConnection(); //Récupérer la connexion vers la base de données non un schéma
        try {
            //Vérifier si c'est un vrai tenant (pas le schema public)
            //Vérification du tenantIdentifier (ou le nom du schema) et que le schéma sélectionné n'est pas le schéma public
            if(tenantIdentifier != null && !tenantIdentifier.equals("public")) {
                // Pointer vers le bon schéma !
                //ex: SET search_path TO tenant_woodev, public
                //SET search_path TO tenant_woodev, public;
                //SELECT * FROM categories;
                //-- → cherche d'abord dans tenant_woodev.categories
                //-- → Si pas là → cherche dans public.categories (fallback)
                //-- → Trouve tenant_woodev.categories
                connection.createStatement().execute("SET search_path TO " + tenantIdentifier.toLowerCase() + ", public");
                log.trace("Set search_path to: {}", tenantIdentifier.toLowerCase());
            }
        } catch (final SQLException e) {
            log.error("Error obtaining connection for tenant: {}", tenantIdentifier, e);
            throw e;
        }
      return connection;   // Connexion maintenant configurée
                        //   pour tenant_woodev
    }

    //Libérer une connexion
    /**
     * Release a connection from Hibernate use.
     *
     * @param tenantIdentifier The identifier of the tenant.
     * @param connection       The JDBC connection to release
     * @throws SQLException       Indicates a problem closing the connection
     * @throws HibernateException Indicates a problem releasing a connection
     */
    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            //Remettre le search_path à public
            //SANS remettre à public :
            //  User Alpha utilise → search_path = tenant_alpha
            //  Connexion rendue au pool
            //  User Beta prend la connexion
            //  search_path = tenant_alpha toujours !!!
            //  User Beta voit les données d'Alpha !
            //
            //AVEC remettre à public :
            //  User Alpha → search_path = tenant_alpha
            //  releaseConnection() :
            //    SET search_path = public
            //  Connexion rendue au pool (propre)
            //  User Beta prend la connexion
            //  getConnection() :
            //    SET search_path = tenant_beta
            //  User Beta voit ses données
            // connection.createStatement().execute("SET search_path TO public"): Exécuter une commande SQL directement
            // sur cette connexion.
            //connection : La connexion JDBC ouverte vers PostgreSQL
            //.createStatement() :
            // ↑ Crée un objet Statement
            // → L'outil pour envoyer du SQL à PostgreSQL
            // → Comme préparer un "stylo" pour écrire
            //.execute("SET search_path TO tenant_woodev, public") :
            //Envoie et exécute la commande SQL
            //
            //Pourquoi pas JdbcTemplate ou JPA ?
            //JPA (Hibernate) :
            //→ Pour les entités (@Entity)
            //→ Pour les opérations CRUD
            //→ On N'A PAS accès à la connexion brute
            //
            //JdbcTemplate :
            //→ Bean Spring injecté
            //→ Pas disponible dans ce contexte
            //→ Gère ses propres connexions
            //
            //connection.createStatement() :
            //→ On A DÉJÀ la connexion ouverte !
            //→ Utiliser DIRECTEMENT cette connexion
            //→ Pas besoin d'un outil Spring
            //→ SQL natif sur cette connexion précise
            connection.createStatement().execute("SET search_path TO public");
        } catch (final SQLException e) {
            log.error("Error obtaining connection for tenant: {}", tenantIdentifier, e);
        }
        connection.close();
    }

    /**
     * Does this connection provider support aggressive release of JDBC connections and later
     * re-acquisition of those connections if needed?
     * <p>
     * This is used in conjunction with {@link ConnectionReleaseMode#AFTER_STATEMENT}
     * to aggressively release JDBC connections. However, the configured {@link ConnectionProvider}
     * must support re-acquisition of the same underlying connection for that semantic to work.
     * <p>
     * Typically, this is only true in managed environments where a container tracks connections
     * by transaction or thread.
     * <p>
     * Note that JTA semantic depends on the fact that the underlying connection provider does
     * support aggressive release.
     *
     * @return {@code true} if aggressive releasing is supported; {@code false} otherwise.
     */
    @Override
    public boolean supportsAggressiveRelease() {
        return false; // false = on ne supporte pas la libération
                        // agressive des connexions
    }

    /**
     * Can this wrapped service be unwrapped as the indicated type?
     *
     * @param unwrapType The type to check.
     * @return True/false.
     */
    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    /**
     * Unproxy the service proxy
     *
     * @param unwrapType The java type as which to unwrap this instance.
     * @return The unwrapped reference
     * @throws UnknownUnwrapTypeException if the service cannot be unwrapped as the indicated type
     */
    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
    }
}
