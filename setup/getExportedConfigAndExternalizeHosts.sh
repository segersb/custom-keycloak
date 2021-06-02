#!/bin/sh

running=$(docker ps -q -f "name=local-development_custom-keycloak_1")
docker cp ${running}:/tmp/setup/ ./export
mv ./export/* .
rm -rf ./export

ADMIN_PASSWORD_SALT=37zwO+8KHipVlKraZnTNkw==
ADMIN_PASSWORD_VALUE=ozClO6MhM3KKOJWnRbrIfH9IMt9ZFRHi9ezWihSeTOU1OC0bl/t46WA00UtvwloCi9dY7XFAjoh7QKRogS4llg==
ADMIN_REST_PASSWORD_SALT=Rn7NdGKksrJg6+aRlQYSgw==
ADMIN_REST_PASSWORD_VALUE=miqdsYDrRrb7e9xv71sY9y/YlMAE108PW/oZ8WeHytrFAiaqpG8LmhUt85bWmdFf4TsL6uY5PfVNBP6Wq4cgng==

ADMIN_PASSWORD_VALUE_ESC=$(printf '%s\n' "$ADMIN_PASSWORD_VALUE" | sed -e 's/[\/&]/\\&/g')
ADMIN_PASSWORD_SALT_ESC=$(printf '%s\n' "$ADMIN_PASSWORD_SALT" | sed -e 's/[\/&]/\\&/g')
ADMIN_REST_PASSWORD_VALUE_ESC=$(printf '%s\n' "$ADMIN_REST_PASSWORD_VALUE" | sed -e 's/[\/&]/\\&/g')
ADMIN_REST_PASSWORD_SALT_ESC=$(printf '%s\n' "$ADMIN_REST_PASSWORD_SALT" | sed -e 's/[\/&]/\\&/g')

# Works with both GNU and BSD/macOS Sed, due to a *non-empty* option-argument:
# Create a backup file *temporarily* and remove it on success.
sed -i.bak "s/${ADMIN_PASSWORD_VALUE_ESC}/{{ADMIN_PASSWORD_VALUE}}/g" realms.json && rm realms.json.bak
sed -i.bak "s/${ADMIN_PASSWORD_SALT_ESC}/{{ADMIN_PASSWORD_SALT}}/g" realms.json && rm realms.json.bak
sed -i.bak "s/${ADMIN_REST_PASSWORD_VALUE_ESC}/{{ADMIN_REST_PASSWORD_VALUE}}/g" realms.json && rm realms.json.bak
sed -i.bak "s/${ADMIN_REST_PASSWORD_SALT_ESC}/{{ADMIN_REST_PASSWORD_SALT}}/g" realms.json && rm realms.json.bak
