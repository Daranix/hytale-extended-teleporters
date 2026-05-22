package com.hytale.extendedteleport.data;

import com.hytale.extendedteleport.TeleporterManager;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;


public final class TeleporterInfo
{
    public static final long SELF_DESTRUCT_DURATION_MS = 300000L;
    @Nullable
    private final UUID ownerUuid;
    private final String dimension;
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final long placedTimestamp;
    private final String locationKey;
    private volatile boolean isPrivate;
    private volatile boolean isPartyOnly;
    private volatile boolean isRestricted;
    private volatile boolean isInteractionLocked;
    private volatile boolean isBreakLocked;
    private volatile boolean displayWorld = true;
    private volatile boolean displayCoordinates = true;
    @Nullable
    private volatile String warpName;
    @Nullable
    private volatile String warpDestination;
    private volatile boolean isSelfDestruct;
    @Nullable
    private volatile Long selfDestructActivatedAt;
    private volatile boolean isServerTeleporter;
    private volatile boolean isSingleUse;
    private volatile boolean hideMapWaypoint;
    private volatile boolean allowPublicDestinationChange = true;
    private final Map<UUID, SubownerPermissions> subowners = new ConcurrentHashMap<>();

    public TeleporterInfo(@Nullable UUID ownerUuid, String dimension, int blockX, int blockY, int blockZ) {
        this(ownerUuid, dimension, blockX, blockY, blockZ, false, false, false, false, false, true, true, null, null, System.currentTimeMillis(), false, null, false, false, false, true, (Map<UUID, SubownerPermissions>)null);
    }


    public TeleporterInfo(@Nullable UUID ownerUuid, String dimension, int blockX, int blockY, int blockZ, boolean isPrivate, boolean isPartyOnly, boolean isRestricted, boolean isInteractionLocked, boolean isBreakLocked, boolean displayWorld, boolean displayCoordinates, @Nullable String warpName, @Nullable String warpDestination, long placedTimestamp) {
        this(ownerUuid, dimension, blockX, blockY, blockZ, isPrivate, isPartyOnly, isRestricted, isInteractionLocked, isBreakLocked, displayWorld, displayCoordinates, warpName, warpDestination, placedTimestamp, false, null, false, false, false, true, (Map<UUID, SubownerPermissions>)null);
    }


    public TeleporterInfo(@Nullable UUID ownerUuid, String dimension, int blockX, int blockY, int blockZ, boolean isPrivate, boolean isPartyOnly, boolean isRestricted, boolean isInteractionLocked, boolean isBreakLocked, boolean displayWorld, boolean displayCoordinates, @Nullable String warpName, @Nullable String warpDestination, long placedTimestamp, boolean isSelfDestruct, @Nullable Long selfDestructActivatedAt, boolean isServerTeleporter, boolean isSingleUse, boolean hideMapWaypoint, @Nullable Set<UUID> trustedPlayers) {
        this(ownerUuid, dimension, blockX, blockY, blockZ, isPrivate, isPartyOnly, isRestricted, isInteractionLocked, isBreakLocked, displayWorld, displayCoordinates, warpName, warpDestination, placedTimestamp, isSelfDestruct, selfDestructActivatedAt, isServerTeleporter, isSingleUse, hideMapWaypoint, true, convertLegacyTrustedPlayers(trustedPlayers));
    }


    public TeleporterInfo(@Nullable UUID ownerUuid, String dimension, int blockX, int blockY, int blockZ, boolean isPrivate, boolean isPartyOnly, boolean isRestricted, boolean isInteractionLocked, boolean isBreakLocked, boolean displayWorld, boolean displayCoordinates, @Nullable String warpName, @Nullable String warpDestination, long placedTimestamp, boolean isSelfDestruct, @Nullable Long selfDestructActivatedAt, boolean isServerTeleporter, boolean isSingleUse, boolean hideMapWaypoint, boolean allowPublicDestinationChange, @Nullable Map<UUID, SubownerPermissions> subowners) {
        this.ownerUuid = ownerUuid;
        this.dimension = Objects.<String>requireNonNull(dimension, "dimension cannot be null");
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.isPrivate = isPrivate;
        this.isPartyOnly = isPartyOnly;
        this.isRestricted = isRestricted;
        this.isInteractionLocked = isInteractionLocked;
        this.isBreakLocked = isBreakLocked;
        this.displayWorld = displayWorld;
        this.displayCoordinates = displayCoordinates;
        this.warpName = warpName;
        this.warpDestination = warpDestination;
        this.placedTimestamp = placedTimestamp;
        this.isSelfDestruct = isSelfDestruct;
        this.selfDestructActivatedAt = selfDestructActivatedAt;
        this.isServerTeleporter = isServerTeleporter;
        this.isSingleUse = isSingleUse;
        this.hideMapWaypoint = hideMapWaypoint;
        this.allowPublicDestinationChange = allowPublicDestinationChange;
        this.locationKey = formatLocationKey(dimension, blockX, blockY, blockZ);
        if (subowners != null) {
            this.subowners.putAll(subowners);
        }
    }


    @Nullable
    private static Map<UUID, SubownerPermissions> convertLegacyTrustedPlayers(@Nullable Set<UUID> trustedPlayers) {
        if (trustedPlayers == null || trustedPlayers.isEmpty()) return null;
        Map<UUID, SubownerPermissions> result = new ConcurrentHashMap<>();
        for (UUID uuid : trustedPlayers) {
            result.put(uuid, new SubownerPermissions(uuid));
        }
        return result;
    }


    @Nullable
    public UUID ownerUuid() {
        return this.ownerUuid;
    }

    public String dimension() {
        return this.dimension;
    }

    public int blockX() {
        return this.blockX;
    }

    public int blockY() {
        return this.blockY;
    }

    public int blockZ() {
        return this.blockZ;
    }

    public long placedTimestamp() {
        return this.placedTimestamp;
    }

    public String locationKey() {
        return this.locationKey;
    }


    public boolean hasOwner() {
        return (this.ownerUuid != null);
    }

    public boolean isOwner(UUID playerUuid) {
        return (this.ownerUuid != null && this.ownerUuid.equals(playerUuid));
    }


    public boolean isTrusted(UUID playerUuid) {
        return (playerUuid != null && this.subowners.containsKey(playerUuid));
    }


    public boolean isOwnerOrTrusted(UUID playerUuid) {
        return (isOwner(playerUuid) || isTrusted(playerUuid));
    }


    public boolean isPrivate() {
        return this.isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public boolean isPartyOnly() {
        return this.isPartyOnly;
    }

    public void setPartyOnly(boolean isPartyOnly) {
        this.isPartyOnly = isPartyOnly;
    }

    public boolean isRestricted() {
        return this.isRestricted;
    }

    public void setRestricted(boolean isRestricted) {
        this.isRestricted = isRestricted;
    }


    public boolean addTrustedPlayer(UUID playerUuid) {
        if (playerUuid == null || isOwner(playerUuid)) return false;
        if (this.subowners.containsKey(playerUuid)) return false;
        this.subowners.put(playerUuid, new SubownerPermissions(playerUuid));

        TeleporterManager.getInstance().onTrustAdded(this, playerUuid);
        return true;
    }


    public boolean addTrustedPlayer(UUID playerUuid, SubownerPermissions permissions) {
        if (playerUuid == null || isOwner(playerUuid)) return false;
        if (this.subowners.containsKey(playerUuid)) return false;
        this.subowners.put(playerUuid, permissions);

        TeleporterManager.getInstance().onTrustAdded(this, playerUuid);
        return true;
    }


    public boolean removeTrustedPlayer(UUID playerUuid) {
        if (playerUuid == null) return false;
        boolean removed = (this.subowners.remove(playerUuid) != null);
        if (removed)
        {
            TeleporterManager.getInstance().onTrustRemoved(this, playerUuid);
        }
        return removed;
    }


    public Set<UUID> getTrustedPlayers() {
        return Collections.unmodifiableSet(new HashSet<>(this.subowners.keySet()));
    }


    @Nullable
    public SubownerPermissions getSubownerPermissions(UUID playerUuid) {
        if (playerUuid == null) return null;
        return this.subowners.get(playerUuid);
    }


    public Collection<SubownerPermissions> getAllSubownerPermissions() {
        return Collections.unmodifiableCollection(this.subowners.values());
    }


    public Map<UUID, SubownerPermissions> getSubownersMap() {
        return Collections.unmodifiableMap(this.subowners);
    }


    public void clearTrustedPlayers() {
        TeleporterManager manager = TeleporterManager.getInstance();
        for (UUID trustedUuid : this.subowners.keySet()) {
            manager.onTrustRemoved(this, trustedUuid);
        }
        this.subowners.clear();
    }


    public boolean canSubownerRename(UUID playerUuid) {
        if (isOwner(playerUuid)) return true;
        SubownerPermissions perms = this.subowners.get(playerUuid);
        return (perms != null && perms.canRename());
    }


    public boolean canSubownerSetDestination(UUID playerUuid) {
        if (isOwner(playerUuid)) return true;
        SubownerPermissions perms = this.subowners.get(playerUuid);
        return (perms != null && perms.canSetDestination());
    }


    public boolean canSubownerModifySettings(UUID playerUuid) {
        if (isOwner(playerUuid)) return true;
        SubownerPermissions perms = this.subowners.get(playerUuid);
        return (perms != null && perms.canModifySettings());
    }


    public boolean canSubownerBreak(UUID playerUuid) {
        if (isOwner(playerUuid)) return true;
        SubownerPermissions perms = this.subowners.get(playerUuid);
        return (perms != null && perms.canBreak());
    }

    public boolean isInteractionLocked() {
        return this.isInteractionLocked;
    }

    public void setInteractionLocked(boolean isInteractionLocked) {
        this.isInteractionLocked = isInteractionLocked;
    }

    public boolean isBreakLocked() {
        return this.isBreakLocked;
    }

    public void setBreakLocked(boolean isBreakLocked) {
        this.isBreakLocked = isBreakLocked;
    }

    public boolean displayWorld() {
        return this.displayWorld;
    }

    public void setDisplayWorld(boolean displayWorld) {
        this.displayWorld = displayWorld;

        if (!displayWorld) {
            this.displayCoordinates = false;
        }
    }

    public boolean displayCoordinates() {
        return this.displayCoordinates;
    }


    public void setDisplayCoordinates(boolean displayCoordinates) {
        this.displayCoordinates = (displayCoordinates && this.displayWorld);
    }

    public boolean isSelfDestruct() {
        return this.isSelfDestruct;
    }
    @Nullable
    public Long selfDestructActivatedAt() {
        return this.selfDestructActivatedAt;
    }


    public void activateSelfDestruct() {
        this.isSelfDestruct = true;
        this.selfDestructActivatedAt = Long.valueOf(System.currentTimeMillis());

        this.isPrivate = false;
        this.isPartyOnly = false;
        this.isRestricted = false;

        this.isInteractionLocked = true;
        this.isBreakLocked = true;
    }


    public boolean isSelfDestructExpired() {
        if (!this.isSelfDestruct || this.selfDestructActivatedAt == null) {
            return false;
        }
        long elapsedMs = System.currentTimeMillis() - this.selfDestructActivatedAt.longValue();
        return (elapsedMs >= 300000L);
    }


    public long getSelfDestructRemainingMs() {
        if (!this.isSelfDestruct || this.selfDestructActivatedAt == null) {
            return -1L;
        }
        long elapsedMs = System.currentTimeMillis() - this.selfDestructActivatedAt.longValue();
        long remaining = 300000L - elapsedMs;
        return Math.max(0L, remaining);
    }
    @Nullable
    public String warpName() {
        return this.warpName;
    }

    public void setWarpName(@Nullable String warpName) {
        this.warpName = warpName;
    }
    @Nullable
    public String warpDestination() {
        return this.warpDestination;
    }

    public void setWarpDestination(@Nullable String warpDestination) {
        this.warpDestination = warpDestination;
    }

    public boolean isServerTeleporter() {
        return this.isServerTeleporter;
    }

    public void setServerTeleporter(boolean isServerTeleporter) {
        this.isServerTeleporter = isServerTeleporter;
    }

    public boolean isSingleUse() {
        return this.isSingleUse;
    }

    public void setSingleUse(boolean isSingleUse) {
        this.isSingleUse = isSingleUse;
    }

    public boolean hideMapWaypoint() {
        return this.hideMapWaypoint;
    }

    public void setHideMapWaypoint(boolean hideMapWaypoint) {
        this.hideMapWaypoint = hideMapWaypoint;
    }


    public boolean allowPublicDestinationChange() {
        return this.allowPublicDestinationChange;
    }

    public void setAllowPublicDestinationChange(boolean allowPublicDestinationChange) {
        this.allowPublicDestinationChange = allowPublicDestinationChange;
    }


    public boolean canPlayerUse(UUID playerUuid) {
        if (this.ownerUuid == null) return true;

        if (isOwner(playerUuid)) return true;

        if (isTrusted(playerUuid)) return true;

        if (this.isRestricted) return false;

        if (this.isPartyOnly) return false;

        return true;
    }


    public boolean canPlayerSee(UUID playerUuid) {
        if (this.ownerUuid == null) return true;

        if (!this.isPrivate) return true;

        return isOwnerOrTrusted(playerUuid);
    }


    public boolean canPlayerInteract(UUID playerUuid) {
        if (this.isSelfDestruct) return false;

        if (this.ownerUuid == null) return true;

        if (isOwnerOrTrusted(playerUuid)) return true;

        return !this.isInteractionLocked;
    }


    public boolean canPlayerBreak(UUID playerUuid) {
        if (this.isSelfDestruct) return false;

        if (this.ownerUuid == null) return true;

        if (isOwner(playerUuid)) return true;

        if (canSubownerBreak(playerUuid)) return true;

        return !this.isBreakLocked;
    }


    public String displayName() {
        String name = this.warpName;
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return "%s (%d, %d, %d)".formatted(new Object[] { this.dimension, Integer.valueOf(this.blockX), Integer.valueOf(this.blockY), Integer.valueOf(this.blockZ) });
    }


    public String displayNameWithRestrictionStatus(String restrictedLabel) {
        String baseName = displayName();
        if (this.isRestricted || this.isPartyOnly) {
            return baseName + " - (" + baseName + ")";
        }
        return baseName;
    }


    public static String formatLocationKey(String dimension, int x, int y, int z) {
        return "%s:%d:%d:%d".formatted(new Object[] { dimension, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z) });
    }


    public boolean equals(Object obj) {
        if (obj instanceof TeleporterInfo) { TeleporterInfo other = (TeleporterInfo)obj; if (this.locationKey.equals(other.locationKey)); }  return false;
    }


    public int hashCode() {
        return this.locationKey.hashCode();
    }


    public String toString() {
        return "TeleporterInfo[%s, owner=%s, private=%b, partyOnly=%b, restricted=%b, interactionLocked=%b, breakLocked=%b, displayWorld=%b, displayCoords=%b, selfDestruct=%b, serverTeleporter=%b, singleUse=%b, hideMapWaypoint=%b, allowPublicDest=%b, warp=%s, subowners=%d]"
        .formatted(new Object[] { this.locationKey, this.ownerUuid, Boolean.valueOf(this.isPrivate), Boolean.valueOf(this.isPartyOnly), Boolean.valueOf(this.isRestricted), Boolean.valueOf(this.isInteractionLocked), Boolean.valueOf(this.isBreakLocked), Boolean.valueOf(this.displayWorld), Boolean.valueOf(this.displayCoordinates), Boolean.valueOf(this.isSelfDestruct), Boolean.valueOf(this.isServerTeleporter), Boolean.valueOf(this.isSingleUse), Boolean.valueOf(this.hideMapWaypoint), Boolean.valueOf(this.allowPublicDestinationChange), this.warpName, Integer.valueOf(this.subowners.size()) });
    }


    @Deprecated(forRemoval = true)
    @Nullable
    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }
    @Deprecated(forRemoval = true)
    public String getDimension() {
        return this.dimension;
    }
    @Deprecated(forRemoval = true)
    public int getBlockX() {
        return this.blockX;
    }
    @Deprecated(forRemoval = true)
    public int getBlockY() {
        return this.blockY;
    }
    @Deprecated(forRemoval = true)
    public int getBlockZ() {
        return this.blockZ;
    } @Deprecated(forRemoval = true)
    @Nullable
    public String getWarpName() {
        return this.warpName;
    } @Deprecated(forRemoval = true)
    @Nullable
    public String getWarpDestination() {
        return this.warpDestination;
    }
    @Deprecated(forRemoval = true)
    public String getLocationKey() {
        return this.locationKey;
    }
    @Deprecated(forRemoval = true)
    public String getDisplayName() {
        return displayName();
    }
    @Deprecated(forRemoval = true)
    public long getPlacedTimestamp() {
        return this.placedTimestamp;
    }
}