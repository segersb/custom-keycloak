package com.acme.keycloak.extension.boundary;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class ExtensionResourceProviderFactory implements RealmResourceProviderFactory {

    public RealmResourceProvider create(KeycloakSession session) {
        return new ExtensionResourceProvider(session);
    }

    public void init(Config.Scope scope) {

    }

    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    public void close() {

    }

    public String getId() {
        return ExtensionResourceProvider.PROVIDER_ID;
    }
}
