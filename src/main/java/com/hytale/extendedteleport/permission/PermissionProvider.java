package com.hytale.extendedteleport.permission;

import java.util.UUID;

public interface PermissionProvider {
    public static final String PERM_FEATURE_PRIVATE = "extendedteleporters.feature.private";

    public static final String PERM_FEATURE_RESTRICTED = "extendedteleporters.feature.restricted";

    public static final String PERM_FEATURE_TRUE_RESTRICTED = "extendedteleporters.feature.truerestricted";

    public static final String PERM_FEATURE_LOCK_INTERACTION = "extendedteleporters.feature.lock_interaction";

    public static final String PERM_FEATURE_LOCK_BREAK = "extendedteleporters.feature.lock_break";

    public static final String PERM_FEATURE_HIDE_WORLD = "extendedteleporters.feature.hide_world";

    public static final String PERM_FEATURE_HIDE_COORDS = "extendedteleporters.feature.hide_coords";

    public static final String PERM_FEATURE_SELF_DESTRUCT = "extendedteleporters.feature.self_destruct";

    public static final String PERM_FEATURE_SINGLE_USE = "extendedteleporters.feature.single_use";

    public static final String PERM_FEATURE_HIDE_MAP_WAYPOINT = "extendedteleporters.feature.hide_map_waypoint";

    public static final String META_LIMIT = "extendedteleporters.limit";

    public static final String META_LIMIT_PRIVATE = "extendedteleporters.limit.private";

    public static final String META_LIMIT_RESTRICTED = "extendedteleporters.limit.restricted";

    public static final String META_LIMIT_PUBLIC = "extendedteleporters.limit.public";

    boolean hasPermission(UUID paramUUID, String paramString);

    int getMetaInteger(UUID paramUUID, String paramString, int paramInt);

    String getName();
}