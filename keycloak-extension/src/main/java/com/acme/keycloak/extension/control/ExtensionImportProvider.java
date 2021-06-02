package com.acme.keycloak.extension.control;

import lombok.SneakyThrows;
import org.keycloak.common.constants.ServiceAccountConstants;
import org.keycloak.exportimport.ImportProvider;
import org.keycloak.exportimport.Strategy;
import org.keycloak.exportimport.util.ExportImportSessionTask;
import org.keycloak.exportimport.util.ExportUtils;
import org.keycloak.exportimport.util.ImportUtils;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.util.JsonSerialization;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ExtensionImportProvider implements ImportProvider {
    public static final String PROVIDER_ID = "extension-import";
    private static final String APP_REALM = "acme";

    @Override
    public void importModel(KeycloakSessionFactory factory, Strategy strategy) throws IOException {
        try {
            KeycloakModelUtils.runJobInTransaction(factory, new ExportImportSessionTask() {
                @Override
                protected void runExportImportTask(KeycloakSession session) throws IOException {
                    List<String> groups = exportGroups(session);
                    String users = exportUsers(session);
                    importRealms(session);
                    importGroups(session, groups);
                    importUsers(session, users);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private void importRealms(KeycloakSession session) {
        InputStream realmsImportStream = new FileInputStream("/tmp/setup/realms.json");
        Map<String, RealmRepresentation> realms = ImportUtils.getRealmsFromStream(JsonSerialization.mapper, realmsImportStream);
        ImportUtils.importRealms(session, realms.values(), Strategy.OVERWRITE_EXISTING);
    }

    @SneakyThrows
    private List<String> exportGroups(KeycloakSession session) {
        RealmModel appRealm = session.realms().getRealm(APP_REALM);
        if (appRealm == null) {
            return Collections.emptyList();
        }

        return appRealm.getTopLevelGroups().stream()
                .map(GroupModel::getName)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private String exportUsers(KeycloakSession session) {
        RealmModel appRealm = session.realms().getRealm(APP_REALM);
        if (appRealm == null) {
            return null;
        }

        List<UserModel> userModels = session.users().getUsers(appRealm).stream()
                .filter(userModel -> !userModel.getUsername().startsWith(ServiceAccountConstants.SERVICE_ACCOUNT_USER_PREFIX))
                .collect(Collectors.toList());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExportUtils.exportUsersToStream(session, appRealm, userModels, JsonSerialization.mapper, outputStream);
        return new String(outputStream.toByteArray(), UTF_8);
    }

    private void importGroups(KeycloakSession session, List<String> groups) {
        RealmModel appRealm = session.realms().getRealm(APP_REALM);
        groups.forEach(appRealm::createGroup);
    }

    @SneakyThrows
    private void importUsers(KeycloakSession session, String users) {
        if (users != null) {
            System.out.println("users = " + users);
            ImportUtils.importUsersFromStream(session, APP_REALM, JsonSerialization.mapper, new ByteArrayInputStream(users.getBytes()));
        }
    }

    @Override
    public void importRealm(KeycloakSessionFactory factory, String realmName, Strategy strategy) throws IOException {
        throw new UnsupportedOperationException("single realm import not implemented");
    }

    @Override
    public boolean isMasterRealmExported() throws IOException {
        return true;
    }

    @Override
    public void close() {

    }
}
