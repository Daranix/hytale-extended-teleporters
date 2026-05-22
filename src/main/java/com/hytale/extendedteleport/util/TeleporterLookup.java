package com.hytale.extendedteleport.util;

import com.hytale.extendedteleport.TeleporterManager;
import com.hytale.extendedteleport.data.TeleporterInfo;

public final class TeleporterLookup {
    public static TeleporterInfo findExact(TeleporterManager manager, String name) {
        return manager.getTeleporterByWarpName(name);
    }

    public static TeleporterInfo findFlexible(TeleporterManager manager, String name) {
        TeleporterInfo info = manager.getTeleporterByWarpName(name);
        if (info == null) {
            for (TeleporterInfo t : manager.getAllTeleporters()) {
                if (t.displayName().toLowerCase().contains(name.toLowerCase())) {
                    return t;
                }
            }
        }
        return info;
    }
}