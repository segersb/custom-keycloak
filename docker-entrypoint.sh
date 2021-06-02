#!/bin/sh

echo "escaping environment variables"
ADMIN_PASSWORD_VALUE_ESC=$(printf '%s\n' "$ADMIN_PASSWORD_VALUE" | sed -e 's/[\/&]/\\&/g')
ADMIN_PASSWORD_SALT_ESC=$(printf '%s\n' "$ADMIN_PASSWORD_SALT" | sed -e 's/[\/&]/\\&/g')
ADMIN_REST_PASSWORD_VALUE_ESC=$(printf '%s\n' "$ADMIN_REST_PASSWORD_VALUE" | sed -e 's/[\/&]/\\&/g')
ADMIN_REST_PASSWORD_SALT_ESC=$(printf '%s\n' "$ADMIN_REST_PASSWORD_SALT" | sed -e 's/[\/&]/\\&/g')

echo "replacing environment variables"
find /tmp/setup -name "*.json" -exec sed -i "s/{{ADMIN_PASSWORD_VALUE}}/${ADMIN_PASSWORD_VALUE_ESC}/g" '{}' +
find /tmp/setup -name "*.json" -exec sed -i "s/{{ADMIN_PASSWORD_SALT}}/${ADMIN_PASSWORD_SALT_ESC}/g" '{}' +
find /tmp/setup -name "*.json" -exec sed -i "s/{{ADMIN_REST_PASSWORD_VALUE}}/${ADMIN_REST_PASSWORD_VALUE_ESC}/g" '{}' +
find /tmp/setup -name "*.json" -exec sed -i "s/{{ADMIN_REST_PASSWORD_SALT}}/${ADMIN_REST_PASSWORD_SALT_ESC}/g" '{}' +

echo "running keycloak"
#exec /opt/jboss/tools/docker-entrypoint.sh "-b 0.0.0.0 -Dkeycloak.migration.action=export -Dkeycloak.migration.provider=singleFile -Dkeycloak.migration.file=/tmp/setup/realms.json -Djboss.http.port=8081 -Dkeycloak.profile.feature.token_exchange=enabled -Dkeycloak.profile.feature.admin_fine_grained_authz=enabled -Dkeycloak.profile.feature.upload_scripts=enabled"
exec /opt/jboss/tools/docker-entrypoint.sh "-b 0.0.0.0 -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=extension-import -Djboss.http.port=8081 -Dkeycloak.profile.feature.token_exchange=enabled -Dkeycloak.profile.feature.admin_fine_grained_authz=enabled -Dkeycloak.profile.feature.upload_scripts=enabled"
