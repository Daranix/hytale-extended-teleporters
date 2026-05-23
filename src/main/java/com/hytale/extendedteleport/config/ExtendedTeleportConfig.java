package com.hytale.extendedteleport.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec.Builder;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class ExtendedTeleportConfig {
   public static final BuilderCodec<ExtendedTeleportConfig> CODEC;

   static {
      Builder<ExtendedTeleportConfig> b = BuilderCodec.<ExtendedTeleportConfig>builder(
         ExtendedTeleportConfig.class, ExtendedTeleportConfig::new
      );
      b = b.addField(
         new KeyedCodec("TeleporterLimit", Codec.INTEGER),
         (ExtendedTeleportConfig cfg, Integer val) -> cfg.teleporterLimit = val,
         (ExtendedTeleportConfig cfg) -> cfg.teleporterLimit
      );
      b = b.addField(
         new KeyedCodec("DefaultTeleporterPrivate", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.defaultTeleporterPrivate = val,
         (ExtendedTeleportConfig cfg) -> cfg.defaultTeleporterPrivate
      );
      b = b.addField(
         new KeyedCodec("DefaultTeleporterRestricted", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.defaultTeleporterRestricted = val,
         (ExtendedTeleportConfig cfg) -> cfg.defaultTeleporterRestricted
      );
      b = b.addField(
         new KeyedCodec("AllowPrivateTeleporters", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.allowPrivateTeleporters = val,
         (ExtendedTeleportConfig cfg) -> cfg.allowPrivateTeleporters
      );
      b = b.addField(
         new KeyedCodec("AllowRestrictedTeleporters", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.allowRestrictedTeleporters = val,
         (ExtendedTeleportConfig cfg) -> cfg.allowRestrictedTeleporters
      );
      b = b.addField(
         new KeyedCodec("DefaultTeleporterInteractionLocked", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.defaultTeleporterInteractionLocked = val,
         (ExtendedTeleportConfig cfg) -> cfg.defaultTeleporterInteractionLocked
      );
      b = b.addField(
         new KeyedCodec("AllowInteractionLockedTeleporters", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.allowInteractionLockedTeleporters = val,
         (ExtendedTeleportConfig cfg) -> cfg.allowInteractionLockedTeleporters
      );
      b = b.addField(
         new KeyedCodec("DefaultTeleporterBreakLocked", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.defaultTeleporterBreakLocked = val,
         (ExtendedTeleportConfig cfg) -> cfg.defaultTeleporterBreakLocked
      );
      b = b.addField(
         new KeyedCodec("AllowBreakLockedTeleporters", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.allowBreakLockedTeleporters = val,
         (ExtendedTeleportConfig cfg) -> cfg.allowBreakLockedTeleporters
      );
      b = b.addField(
         new KeyedCodec("MaxTeleportersPerPlayer", Codec.INTEGER),
         (ExtendedTeleportConfig cfg, Integer val) -> cfg.maxTeleportersPerPlayer = val,
         (ExtendedTeleportConfig cfg) -> cfg.maxTeleportersPerPlayer
      );
      b = b.addField(
         new KeyedCodec("MaxPrivateTeleporters", Codec.INTEGER),
         (ExtendedTeleportConfig cfg, Integer val) -> cfg.maxPrivateTeleporters = val,
         (ExtendedTeleportConfig cfg) -> cfg.maxPrivateTeleporters
      );
      b = b.addField(
         new KeyedCodec("MaxRestrictedTeleporters", Codec.INTEGER),
         (ExtendedTeleportConfig cfg, Integer val) -> cfg.maxRestrictedTeleporters = val,
         (ExtendedTeleportConfig cfg) -> cfg.maxRestrictedTeleporters
      );
      b = b.addField(
         new KeyedCodec("MaxPublicTeleporters", Codec.INTEGER),
         (ExtendedTeleportConfig cfg, Integer val) -> cfg.maxPublicTeleporters = val,
         (ExtendedTeleportConfig cfg) -> cfg.maxPublicTeleporters
      );
      b = b.addField(
         new KeyedCodec("AllowSelfDestructTeleporters", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.allowSelfDestructTeleporters = val,
         (ExtendedTeleportConfig cfg) -> cfg.allowSelfDestructTeleporters
      );
      b = b.addField(
         new KeyedCodec("AllowHideMapWaypoint", Codec.BOOLEAN),
         (ExtendedTeleportConfig cfg, Boolean val) -> cfg.allowHideMapWaypoint = val,
         (ExtendedTeleportConfig cfg) -> cfg.allowHideMapWaypoint
      );
      CODEC = b.build();
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
