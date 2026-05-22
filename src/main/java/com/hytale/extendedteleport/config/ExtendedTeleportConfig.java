package com.hytale.extendedteleport.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class ExtendedTeleportConfig
{
    public static final BuilderCodec<ExtendedTeleportConfig> CODEC;

    static {
        BuilderCodec.Builder<ExtendedTeleportConfig> builder = BuilderCodec.builder(ExtendedTeleportConfig.class, ExtendedTeleportConfig::new);
        builder
        .addField(new KeyedCodec<>("TeleporterLimit", Codec.INTEGER), (ExtendedTeleportConfig config, Integer value) -> config.teleporterLimit = value, (ExtendedTeleportConfig config) -> config.teleporterLimit)
        .addField(new KeyedCodec<>("DefaultTeleporterPrivate", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.defaultTeleporterPrivate = value, (ExtendedTeleportConfig config) -> config.defaultTeleporterPrivate)
        .addField(new KeyedCodec<>("DefaultTeleporterRestricted", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.defaultTeleporterRestricted = value, (ExtendedTeleportConfig config) -> config.defaultTeleporterRestricted)
        .addField(new KeyedCodec<>("AllowPrivateTeleporters", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.allowPrivateTeleporters = value, (ExtendedTeleportConfig config) -> config.allowPrivateTeleporters)
        .addField(new KeyedCodec<>("AllowRestrictedTeleporters", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.allowRestrictedTeleporters = value, (ExtendedTeleportConfig config) -> config.allowRestrictedTeleporters)
        .addField(new KeyedCodec<>("DefaultTeleporterInteractionLocked", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.defaultTeleporterInteractionLocked = value, (ExtendedTeleportConfig config) -> config.defaultTeleporterInteractionLocked)
        .addField(new KeyedCodec<>("AllowInteractionLockedTeleporters", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.allowInteractionLockedTeleporters = value, (ExtendedTeleportConfig config) -> config.allowInteractionLockedTeleporters)
        .addField(new KeyedCodec<>("DefaultTeleporterBreakLocked", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.defaultTeleporterBreakLocked = value, (ExtendedTeleportConfig config) -> config.defaultTeleporterBreakLocked)
        .addField(new KeyedCodec<>("AllowBreakLockedTeleporters", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.allowBreakLockedTeleporters = value, (ExtendedTeleportConfig config) -> config.allowBreakLockedTeleporters)
        .addField(new KeyedCodec<>("MaxTeleportersPerPlayer", Codec.INTEGER), (ExtendedTeleportConfig config, Integer value) -> config.maxTeleportersPerPlayer = value, (ExtendedTeleportConfig config) -> config.maxTeleportersPerPlayer)
        .addField(new KeyedCodec<>("MaxPrivateTeleporters", Codec.INTEGER), (ExtendedTeleportConfig config, Integer value) -> config.maxPrivateTeleporters = value, (ExtendedTeleportConfig config) -> config.maxPrivateTeleporters)
        .addField(new KeyedCodec<>("MaxRestrictedTeleporters", Codec.INTEGER), (ExtendedTeleportConfig config, Integer value) -> config.maxRestrictedTeleporters = value, (ExtendedTeleportConfig config) -> config.maxRestrictedTeleporters)
        .addField(new KeyedCodec<>("MaxPublicTeleporters", Codec.INTEGER), (ExtendedTeleportConfig config, Integer value) -> config.maxPublicTeleporters = value, (ExtendedTeleportConfig config) -> config.maxPublicTeleporters)
        .addField(new KeyedCodec<>("AllowSelfDestructTeleporters", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.allowSelfDestructTeleporters = value, (ExtendedTeleportConfig config) -> config.allowSelfDestructTeleporters)
        .addField(new KeyedCodec<>("AllowHideMapWaypoint", Codec.BOOLEAN), (ExtendedTeleportConfig config, Boolean value) -> config.allowHideMapWaypoint = value, (ExtendedTeleportConfig config) -> config.allowHideMapWaypoint);
        CODEC = builder.build();
    }

    private int teleporterLimit = 9999;

    private boolean defaultTeleporterPrivate = false;

    private boolean defaultTeleporterRestricted = false;

    private boolean allowPrivateTeleporters = true;

    private boolean allowRestrictedTeleporters = true;

    private boolean defaultTeleporterInteractionLocked = false;

    private boolean allowInteractionLockedTeleporters = true;

    private boolean defaultTeleporterBreakLocked = false;

    private boolean allowBreakLockedTeleporters = true;

    private int maxTeleportersPerPlayer = 8;

    private int maxPrivateTeleporters = 4;

    private int maxRestrictedTeleporters = 4;

    private int maxPublicTeleporters = 0;

    private boolean allowSelfDestructTeleporters = true;

    private boolean allowHideMapWaypoint = true;

    public int getTeleporterLimit() {
        return this.teleporterLimit;
    }

    public boolean isDefaultTeleporterPrivate() {
        return this.defaultTeleporterPrivate;
    }

    public boolean isDefaultTeleporterRestricted() {
        return this.defaultTeleporterRestricted;
    }

    public boolean isAllowPrivateTeleporters() {
        return this.allowPrivateTeleporters;
    }

    public boolean isAllowRestrictedTeleporters() {
        return this.allowRestrictedTeleporters;
    }

    public boolean isDefaultTeleporterInteractionLocked() {
        return this.defaultTeleporterInteractionLocked;
    }

    public boolean isAllowInteractionLockedTeleporters() {
        return this.allowInteractionLockedTeleporters;
    }

    public boolean isDefaultTeleporterBreakLocked() {
        return this.defaultTeleporterBreakLocked;
    }

    public boolean isAllowBreakLockedTeleporters() {
        return this.allowBreakLockedTeleporters;
    }

    public int getMaxTeleportersPerPlayer() {
        return this.maxTeleportersPerPlayer;
    }

    public int getMaxPrivateTeleporters() {
        return this.maxPrivateTeleporters;
    }

    public int getMaxRestrictedTeleporters() {
        return this.maxRestrictedTeleporters;
    }

    public int getMaxPublicTeleporters() {
        return this.maxPublicTeleporters;
    }

    public boolean isAllowSelfDestructTeleporters() {
        return this.allowSelfDestructTeleporters;
    }

    public boolean isAllowHideMapWaypoint() {
        return this.allowHideMapWaypoint;
    }
}