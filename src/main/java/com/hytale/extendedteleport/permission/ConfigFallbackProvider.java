package com.hytale.extendedteleport.permission;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hytale.extendedteleport.Main;
import com.hytale.extendedteleport.config.ExtendedTeleportConfig;
import java.util.UUID;
import java.util.logging.Level;


public final class ConfigFallbackProvider
implements PermissionProvider
{
    private static final HytaleLogger logger = HytaleLogger.getLogger().getSubLogger("ExtendedTeleport-ConfigFallback");

    public ConfigFallbackProvider() {
        logger.at(Level.INFO).log("Config fallback permission provider initialized (LuckPerms not available)");
    }


    public String getName() {
        return "ConfigFallback";
    }

    private ExtendedTeleportConfig getConfig() {
        if (Main.CONFIG != null) {
            return (ExtendedTeleportConfig)Main.CONFIG.get();
        }
        return new ExtendedTeleportConfig();
    }


    public boolean hasPermission(UUID playerUuid, String node) {
        if (playerUuid == null || node == null) {
            return false;
        }

        ExtendedTeleportConfig config = getConfig();


        switch (node) { case "extendedteleporters.feature.private": case "extendedteleporters.feature.restricted": case "extendedteleporters.feature.truerestricted": case "extendedteleporters.feature.lock_interaction": case "extendedteleporters.feature.lock_break": case "extendedteleporters.feature.hide_world": case "extendedteleporters.feature.hide_coords": case "extendedteleporters.feature.self_destruct": case "extendedteleporters.feature.hide_map_waypoint":  }  return true;
    }


    public int getMetaInteger(UUID playerUuid, String key, int fallback) {
        if (playerUuid == null || key == null) {
            return fallback;
        }

        ExtendedTeleportConfig config = getConfig();


        switch (key) { case "extendedteleporters.limit": case "extendedteleporters.limit.private": case "extendedteleporters.limit.restricted": case "extendedteleporters.limit.public":  }  return


        fallback;
    }
}