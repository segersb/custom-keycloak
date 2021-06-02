package com.acme.keycloak.extension.control;

import org.keycloak.Config;
import org.keycloak.exportimport.ImportProvider;
import org.keycloak.exportimport.ImportProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class ExtensionImportProviderFactory implements ImportProviderFactory {
    @Override
    public ImportProvider create(KeycloakSession keycloakSession) {
        return new ExtensionImportProvider();
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ExtensionImportProvider.PROVIDER_ID;
    }
}
