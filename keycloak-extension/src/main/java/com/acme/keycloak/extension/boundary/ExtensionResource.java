package com.acme.keycloak.extension.boundary;

import org.jboss.logging.Logger;
import org.keycloak.common.ClientConnection;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class ExtensionResource {
    protected static final Logger logger = Logger.getLogger(ExtensionResource.class);

    protected AppAuthManager authManager;
    protected KeycloakSession session;
    protected ClientConnection clientConnection;
    protected HttpHeaders headers;
    protected AdminPermissionEvaluator permissionEvaluator;
    protected RealmModel realm;
    protected EntityManager entityManager;

    public ExtensionResource(KeycloakSession session) {
        this.authManager = new AppAuthManager();
        this.session = session;
        this.clientConnection = session.getContext().getConnection();
        this.headers = session.getContext().getRequestHeaders();

        RealmModel originalRealm = session.getContext().getRealm();
        AdminAuth adminAuth = authenticateRealmAdminRequest(headers);
        session.getContext().setRealm(originalRealm); //this is needed as otherwise master is used...

        this.permissionEvaluator = AdminPermissions.evaluator(session, session.getContext().getRealm(), adminAuth);
        this.realm = session.getContext().getRealm();
        this.entityManager = session.getProvider(JpaConnectionProvider.class).getEntityManager();

        permissionEvaluator.users().requireManage();
    }

    @POST
    @Path("/ping")
    @Produces(MediaType.APPLICATION_JSON)
    public Long ping(
            @QueryParam("disabled") Boolean disabled,
            @QueryParam("unverified") Boolean unverified
    ) {
        return System.currentTimeMillis();
    }

    AdminAuth authenticateRealmAdminRequest(HttpHeaders headers) {
        //Copied from org.keycloak.services.resources.admin.AdminRoot
        String tokenString = authManager.extractAuthorizationHeaderToken(headers);
        if (tokenString == null) throw new NotAuthorizedException("Bearer");
        AccessToken token;
        try {
            JWSInput input = new JWSInput(tokenString);
            token = input.readJsonContent(AccessToken.class);
        } catch (JWSInputException e) {
            logger.info("Bearer token format error");
            throw new NotAuthorizedException("Bearer token format error");
        }
        String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
        RealmManager realmManager = new RealmManager(session);
        RealmModel realm = realmManager.getRealmByName(realmName);
        if (realm == null) {
            logger.info("Unknown realm in token");
            throw new NotAuthorizedException("Unknown realm in token");
        }
        session.getContext().setRealm(realm);
        AuthenticationManager.AuthResult authResult = authManager.authenticateBearerToken(session, realm, session.getContext().getUri(), clientConnection, headers);
        if (authResult == null) {
            logger.info("Token not valid");
            throw new NotAuthorizedException("Bearer");
        }

        ClientModel client = realm.getClientByClientId(token.getIssuedFor());
        if (client == null) {
            throw new NotFoundException("Could not find client for authorization");

        }
        return new AdminAuth(realm, authResult.getToken(), authResult.getUser(), client);
    }
}
