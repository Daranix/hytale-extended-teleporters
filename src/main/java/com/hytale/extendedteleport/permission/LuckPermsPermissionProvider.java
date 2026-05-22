package com.hytale.extendedteleport.permission;

import java.util.UUID;

public class LuckPermsPermissionProvider implements PermissionProvider {
    public LuckPermsPermissionProvider() {
    }

    @Override
    public boolean hasPermission(UUID playerUuid, String permission) {
        return false;
    }

    @Override
    public int getMetaInteger(UUID playerUuid, String key, int defaultValue) {
        return defaultValue;
    }

    @Override
    public String getName() {
        return "LuckPerms";
    }
}