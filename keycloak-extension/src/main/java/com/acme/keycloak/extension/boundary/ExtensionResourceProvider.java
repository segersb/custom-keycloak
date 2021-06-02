package com.acme.keycloak.extension.boundary;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class ExtensionResourceProvider implements RealmResourceProvider {
    public static final String PROVIDER_ID = "extension-resource";
    private KeycloakSession session;

    public ExtensionResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    public Object getResource() {
        ExtensionResource result = new ExtensionResource(session);
        ResteasyProviderFactory.getInstance().injectProperties(result);
        return result;
    }

    public void close() {
    }
}
