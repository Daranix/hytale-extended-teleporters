package com.hytale.extendedteleport;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.adventure.teleporter.system.CreateWarpWhenTeleporterPlacedSystem;
import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hytale.extendedteleport.config.ExtendedTeleportConfig;
import com.hytale.extendedteleport.data.CustomDestination;
import com.hytale.extendedteleport.data.TeleporterInfo;
import com.hytale.extendedteleport.files.CustomDestinationFile;
import com.hytale.extendedteleport.files.TeleporterBlockingFile;
import com.hytale.extendedteleport.i18n.Translations;
import com.hytale.extendedteleport.permission.ConfigFallbackProvider;
import com.hytale.extendedteleport.permission.PermissionProvider;
import com.hytale.extendedteleport.system.TeleporterRestrictionTickingSystem;
import com.hytale.extendedteleport.util.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import javax.annotation.Nullable;


public final class TeleporterManager
{
    private static final String TELEPORTER_BLOCK_NAME = "Teleporter";
    private static final long SAVE_INTERVAL_MS = 5000L;
    public static final long CROSS_WORLD_TELEPORT_COOLDOWN_MS = 5000L;
    private static volatile TeleporterManager instance;
    private final HytaleLogger logger = HytaleLogger.getLogger().getSubLogger("ExtendedTeleportHistory");
    private final TeleporterBlockingFile teleporterFile;
    private final CustomDestinationFile customDestinationFile;
    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();


    private final Map<String, TeleporterInfo> teleportersByLocation = new ConcurrentHashMap<>();


    private final Map<UUID, Set<TeleporterInfo>> teleportersByOwner = new ConcurrentHashMap<>();


    private final Map<UUID, Set<TeleporterInfo>> teleportersByTrustedPlayer = new ConcurrentHashMap<>();


    private final Map<String, World> worlds = new ConcurrentHashMap<>();


    private final Map<String, Warp> hiddenWarps = new ConcurrentHashMap<>();


    private final Map<String, CustomDestination> customDestinations = new ConcurrentHashMap<>();


    private final Set<UUID> bypassPlayers = ConcurrentHashMap.newKeySet();


    private final Map<UUID, Long> crossWorldTeleportTime = new ConcurrentHashMap<>();


    private PermissionProvider permissionProvider;


    private final AtomicBoolean isDirty = new AtomicBoolean(false);
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);


    private Thread saveThread;


    private TeleporterRestrictionTickingSystem restrictionSystem;


    public static TeleporterManager getInstance() {
        if (instance == null) {
            synchronized (TeleporterManager.class) {
                if (instance == null) {
                    instance = new TeleporterManager();
                }
            }
        }
        return instance;
    }

    private TeleporterManager() {
        this.teleporterFile = new TeleporterBlockingFile();
        this.customDestinationFile = new CustomDestinationFile();


        this.permissionProvider = null;

        loadData();
        startSaveThread();
    }


    public void initializePermissionProvider() {
        if (this.permissionProvider != null) {
            this.logger.at(Level.INFO).log("Permission provider already initialized: " + this.permissionProvider.getName());

            return;
        }
        this.logger.at(Level.INFO).log("Initializing permission provider...");


        boolean luckPermsAvailable = false;
        Throwable luckPermsError = null;
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            luckPermsAvailable = true;
            this.logger.at(Level.INFO).log("LuckPerms API class found");
        } catch (Throwable t) {

            luckPermsAvailable = false;
            luckPermsError = t;
            this.logger.at(Level.INFO).log("LuckPerms API class not found: " + t.getClass().getSimpleName() + " - " + t.getMessage());
        }

        if (luckPermsAvailable) {


            try {
                Class<?> providerClass = Class.forName("com.hytale.extendedteleport.permission.LuckPermsPermissionProvider");
                this.permissionProvider = (PermissionProvider)providerClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                this.logger.at(Level.INFO).log("Using LuckPerms for permission handling");
            } catch (Throwable t) {

                ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(t)).log("Failed to initialize LuckPerms provider, using config fallback");
                this.permissionProvider = (PermissionProvider)new ConfigFallbackProvider();
            }
        } else {
            this.permissionProvider = (PermissionProvider)new ConfigFallbackProvider();
            this.logger.at(Level.INFO).log("LuckPerms not found, using config fallback for permissions");
        }
    }


    public PermissionProvider getPermissionProvider() {
        if (this.permissionProvider == null) {
            initializePermissionProvider();
        }
        return this.permissionProvider;
    }


    public void reloadAll() {
        this.logger.at(Level.INFO).log("Reloading all teleporter data...");


        loadData();


        ensureAllTeleporterWarpsExist();


        enforcePrivateWarpsInRegistry();

        this.logger.at(Level.INFO).log("Reload complete - all teleporter data refreshed");
    }


    private void enforcePrivateWarpsInRegistry() {
        int hiddenCount = 0;

        for (TeleporterInfo info : this.teleportersByLocation.values()) {

            if (info.warpName() != null && !info.warpName().isEmpty() && info.isPrivate()) {
                String warpId = info.warpName().toLowerCase();

                try {
                    Map<String, Warp> gameWarps = TeleportPlugin.get().getWarps();
                    if (gameWarps.containsKey(warpId) && !this.hiddenWarps.containsKey(warpId)) {
                        Warp warp = gameWarps.remove(warpId);
                        if (warp != null) {
                            this.hiddenWarps.put(warpId, warp);
                            hiddenCount++;
                        }
                    }
                } catch (Exception exception) {}
            }
        }

        if (hiddenCount > 0) {
            this.logger.at(Level.INFO).log("Enforced privacy on %d warps".formatted(new Object[] { Integer.valueOf(hiddenCount) }));
        }
    }

    private void loadData() {
        FileUtils.ensureMainDirectory();

        try {
            FileUtils.ensureFile(FileUtils.TELEPORTERS_PATH, "{}");
            this.teleporterFile.syncLoad();


            this.dataLock.writeLock().lock();
            try {
                this.teleportersByLocation.clear();
                this.teleportersByOwner.clear();
                this.teleportersByTrustedPlayer.clear();

                for (TeleporterInfo info : this.teleporterFile.getTeleporters().values()) {
                    this.teleportersByLocation.put(info.locationKey(), info);
                    indexByOwner(info);
                    indexByTrustedPlayers(info);
                }
            } finally {
                this.dataLock.writeLock().unlock();
            }

            this.logger.at(Level.INFO).log("Loaded %d teleporter records".formatted(new Object[] { Integer.valueOf(this.teleportersByLocation.size()) }));
        } catch (Exception e) {
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(e)).log("Error loading teleporter data");
        }


        try {
            FileUtils.ensureFile(FileUtils.CUSTOM_DESTINATIONS_PATH, "{\"Destinations\":[]}");
            this.customDestinationFile.syncLoad();

            this.customDestinations.clear();
            this.customDestinations.putAll(this.customDestinationFile.getDestinations());

            this.logger.at(Level.INFO).log("Loaded %d custom destinations".formatted(new Object[] { Integer.valueOf(this.customDestinations.size()) }));
        } catch (Exception e) {
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(e)).log("Error loading custom destinations");
        }
    }

    private void indexByOwner(TeleporterInfo info) {
        UUID owner = info.ownerUuid();
        if (owner != null) {
            ((Set<TeleporterInfo>)this.teleportersByOwner.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet())).add(info);
        }
    }

    private void removeFromOwnerIndex(TeleporterInfo info) {
        UUID owner = info.ownerUuid();
        if (owner != null) {
            Set<TeleporterInfo> owned = this.teleportersByOwner.get(owner);
            if (owned != null) {
                owned.remove(info);
                if (owned.isEmpty()) {
                    this.teleportersByOwner.remove(owner);
                }
            }
        }
    }


    private void indexByTrustedPlayers(TeleporterInfo info) {
        for (UUID trustedUuid : info.getTrustedPlayers()) {
            ((Set<TeleporterInfo>)this.teleportersByTrustedPlayer.computeIfAbsent(trustedUuid, k -> ConcurrentHashMap.newKeySet())).add(info);
        }
    }


    private void removeFromTrustedIndex(TeleporterInfo info) {
        for (UUID trustedUuid : info.getTrustedPlayers()) {
            Set<TeleporterInfo> trusted = this.teleportersByTrustedPlayer.get(trustedUuid);
            if (trusted != null) {
                trusted.remove(info);
                if (trusted.isEmpty()) {
                    this.teleportersByTrustedPlayer.remove(trustedUuid);
                }
            }
        }
    }


    public void onTrustAdded(TeleporterInfo info, UUID trustedUuid) {
        ((Set<TeleporterInfo>)this.teleportersByTrustedPlayer.computeIfAbsent(trustedUuid, k -> ConcurrentHashMap.newKeySet())).add(info);
    }


    public void onTrustRemoved(TeleporterInfo info, UUID trustedUuid) {
        Set<TeleporterInfo> trusted = this.teleportersByTrustedPlayer.get(trustedUuid);
        if (trusted != null) {
            trusted.remove(info);
            if (trusted.isEmpty()) {
                this.teleportersByTrustedPlayer.remove(trustedUuid);
            }
        }
    }

    private void startSaveThread() {
        this.saveThread = Thread.ofVirtual().name("ExtendedTeleport-Saver").start(() -> {
            while (!this.isShuttingDown.get()) {
                try {
                    Thread.sleep(5000L);


                    if (this.isDirty.compareAndSet(true, false)) {
                        performSave();
                    }


                    enforcePrivateWarps();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    this.logger.at(Level.WARNING).log("Error in save thread: " + e.getMessage());
                }
            }
        });
    }


    public void setRestrictionSystem(TeleporterRestrictionTickingSystem system) {
        this.restrictionSystem = system;
    }


    public void addWorld(World world) {
        this.worlds.put(world.getName(), world);


        resetPersistedBlockCounter(world.getName());


        initializeRestrictedTeleportersInWorld(world);
    }


    private void resetPersistedBlockCounter(String worldName) {
        Path blockCounterPath = Constants.UNIVERSE_PATH.resolve("worlds").resolve(worldName).resolve("resources").resolve("block counter.json");

        if (!Files.exists(blockCounterPath, new java.nio.file.LinkOption[0])) {
            return;
        }

        try {
            String content = Files.readString(blockCounterPath);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            if (!root.has("Teleporter")) {
                return;
            }

            int currentCount = root.get("Teleporter").getAsInt();
            if (currentCount <= 0) {
                return;
            }


            root.addProperty("Teleporter", Integer.valueOf(0));
            Files.writeString(blockCounterPath, root.toString(), new java.nio.file.OpenOption[0]);
        }
        catch (IOException|RuntimeException iOException) {}
    }


    private void initializeRestrictedTeleportersInWorld(World world) {
        if (this.restrictionSystem == null)
        return;
        String worldName = world.getName();
        for (TeleporterInfo info : this.teleportersByLocation.values()) {
            if (info.isRestricted() && info.dimension().equals(worldName)) {
                this.restrictionSystem.onTeleporterRegistered(info, world);
            }
        }
    }

    public void removeWorld(String worldName) {
        this.worlds.remove(worldName);
    }

    public Map<String, World> getWorlds() {
        return Collections.unmodifiableMap(this.worlds);
    }


    @Nullable
    public World getWorld(String dimension) {
        return this.worlds.get(dimension);
    }


    public void onTeleporterPlaced(UUID ownerUuid, World world, int blockX, int blockY, int blockZ) {
        TeleporterInfo info = new TeleporterInfo(ownerUuid, world.getName(), blockX, blockY, blockZ);

        if (Main.CONFIG != null) {
            ExtendedTeleportConfig config = (ExtendedTeleportConfig)Main.CONFIG.get();
            info.setPrivate(config.isDefaultTeleporterPrivate());
            info.setRestricted(config.isDefaultTeleporterRestricted());
            info.setInteractionLocked(config.isDefaultTeleporterInteractionLocked());
            info.setBreakLocked(config.isDefaultTeleporterBreakLocked());
        }


        this.dataLock.writeLock().lock();
        try {
            this.teleportersByLocation.put(info.locationKey(), info);
            this.teleporterFile.getTeleporters().put(info.locationKey(), info);
            indexByOwner(info);
        } finally {
            this.dataLock.writeLock().unlock();
        }

        markDirty();


        if (this.restrictionSystem != null && info.isRestricted()) {
            this.restrictionSystem.onTeleporterRegistered(info, world);
        }

        this.logger.at(Level.INFO).log("Teleporter placed by %s at %s (Total: %d)"
        .formatted(new Object[] { ownerUuid, info.locationKey(), Integer.valueOf(this.teleportersByLocation.size()) }));
    }


    public void onTeleporterRemoved(String dimension, int blockX, int blockY, int blockZ) {
        TeleporterInfo removed;
        String key = TeleporterInfo.formatLocationKey(dimension, blockX, blockY, blockZ);

        this.dataLock.writeLock().lock();

        try {
            removed = this.teleportersByLocation.remove(key);
            this.teleporterFile.getTeleporters().remove(key);
            if (removed != null) {
                removeFromOwnerIndex(removed);
                removeFromTrustedIndex(removed);
            }
        } finally {
            this.dataLock.writeLock().unlock();
        }

        if (removed != null) {

            String warpName = removed.warpName();
            if (warpName != null && !warpName.isEmpty()) {
                this.hiddenWarps.remove(warpName.toLowerCase());
            }


            if (this.restrictionSystem != null) {
                this.restrictionSystem.onTeleporterRemoved(key);
            }

            markDirty();
            this.logger.at(Level.INFO).log("Teleporter removed at " + key);
        }
    }

    public PlacementCheckResult checkPlacementLimits(UUID playerUuid, boolean isPrivate, boolean isRestricted) {
        PermissionProvider provider = getPermissionProvider();


        int totalLimit = provider.getMetaInteger(playerUuid, "extendedteleporters.limit", 0);
        int currentCount = getPlayerTeleporters(playerUuid).size();

        if (totalLimit > 0 &&
        currentCount >= totalLimit) {
            return PlacementCheckResult.denied(
            Translations.tr("msg.limit.total", new Object[] { "current", Integer.valueOf(currentCount), "max", Integer.valueOf(totalLimit) }));
        }


        if (!isPrivate) {
            int publicLimit = provider.getMetaInteger(playerUuid, "extendedteleporters.limit.public", 0);
            if (publicLimit > 0) {
                int currentPublicCount = countPlayerPublicTeleporters(playerUuid);
                if (currentPublicCount >= publicLimit) {
                    return PlacementCheckResult.denied(
                    Translations.tr("msg.limit.public", new Object[] { "current", Integer.valueOf(currentPublicCount), "max", Integer.valueOf(publicLimit) }));
                }
            }
        }


        if (isPrivate) {
            if (!provider.hasPermission(playerUuid, "extendedteleporters.feature.private")) {
                return PlacementCheckResult.denied(Translations.tr("msg.noPermission.private", new Object[0]));
            }

            int privateLimit = provider.getMetaInteger(playerUuid, "extendedteleporters.limit.private", 0);
            if (privateLimit > 0) {
                int currentPrivateCount = countPlayerPrivateTeleporters(playerUuid);
                if (currentPrivateCount >= privateLimit) {
                    return PlacementCheckResult.denied(
                    Translations.tr("msg.limit.private", new Object[] { "current", Integer.valueOf(currentPrivateCount), "max", Integer.valueOf(privateLimit) }));
                }
            }
        }


        if (isRestricted) {
            if (!provider.hasPermission(playerUuid, "extendedteleporters.feature.restricted")) {
                return PlacementCheckResult.denied(Translations.tr("msg.noPermission.restricted", new Object[0]));
            }

            int restrictedLimit = provider.getMetaInteger(playerUuid, "extendedteleporters.limit.restricted", 0);
            if (restrictedLimit > 0) {
                int currentRestrictedCount = countPlayerRestrictedTeleporters(playerUuid);
                if (currentRestrictedCount >= restrictedLimit) {
                    return PlacementCheckResult.denied(
                    Translations.tr("msg.limit.restricted", new Object[] { "current", Integer.valueOf(currentRestrictedCount), "max", Integer.valueOf(restrictedLimit) }));
                }
            }
        }

        return PlacementCheckResult.ALLOWED;
    }


    public int countPlayerPrivateTeleporters(UUID playerUuid) {
        Set<TeleporterInfo> owned = this.teleportersByOwner.get(playerUuid);
        if (owned == null || owned.isEmpty()) {
            return 0;
        }
        return (int)owned.stream().filter(TeleporterInfo::isPrivate).count();
    }


    public int countPlayerRestrictedTeleporters(UUID playerUuid) {
        Set<TeleporterInfo> owned = this.teleportersByOwner.get(playerUuid);
        if (owned == null || owned.isEmpty()) {
            return 0;
        }
        return (int)owned.stream().filter(TeleporterInfo::isRestricted).count();
    }


    public int countPlayerPartyOnlyTeleporters(UUID playerUuid) {
        Set<TeleporterInfo> owned = this.teleportersByOwner.get(playerUuid);
        if (owned == null || owned.isEmpty()) {
            return 0;
        }
        return (int)owned.stream().filter(TeleporterInfo::isPartyOnly).count();
    }


    public int countPlayerPublicTeleporters(UUID playerUuid) {
        Set<TeleporterInfo> owned = this.teleportersByOwner.get(playerUuid);
        if (owned == null || owned.isEmpty()) {
            return 0;
        }
        return (int)owned.stream().filter(info -> !info.isPrivate()).count();
    }


    @Nullable
    public TeleporterInfo getTeleporter(String dimension, int blockX, int blockY, int blockZ) {
        String key = TeleporterInfo.formatLocationKey(dimension, blockX, blockY, blockZ);
        return this.teleportersByLocation.get(key);
    }
    @Nullable
    public TeleporterInfo getTeleporterByKey(String locationKey) {
        return this.teleportersByLocation.get(locationKey);
    }


    @Nullable
    public TeleporterInfo getTeleporterByWarpName(String warpName) {
        if (warpName == null || warpName.isEmpty()) return null;
        String lowerWarpName = warpName.toLowerCase();
        for (TeleporterInfo info : this.teleportersByLocation.values()) {
            String infoWarpName = info.warpName();
            if (infoWarpName != null && infoWarpName.toLowerCase().equals(lowerWarpName)) {
                return info;
            }
        }
        return null;
    }


    public String formatWarpDisplayName(String warpName) {
        if (warpName == null || warpName.isEmpty()) return warpName;

        TeleporterInfo info = getTeleporterByWarpName(warpName);
        if (info != null) {
            if (info.displayWorld()) {
                if (info.displayCoordinates()) {
                    return "%s (%s: %d, %d, %d)".formatted(new Object[] { warpName, info

                        .dimension(),
                        Integer.valueOf(info.blockX()),
                        Integer.valueOf(info.blockY()),
                    Integer.valueOf(info.blockZ()) });
                }

                return "%s (%s)".formatted(new Object[] { warpName, info.dimension() });
            }


            return warpName;
        }


        return warpName;
    }


    public List<TeleporterInfo> getPlayerTeleporters(UUID playerUuid) {
        Set<TeleporterInfo> owned = this.teleportersByOwner.get(playerUuid);
        if (owned == null || owned.isEmpty()) {
            return List.of();
        }
        return List.copyOf(owned);
    }


    public List<TeleporterInfo> getPlayerTeleportersSynced(UUID playerUuid) {
        List<TeleporterInfo> teleporters = getPlayerTeleporters(playerUuid);
        for (TeleporterInfo info : teleporters) {
            syncTeleporterInfoWithComponent(info);
        }
        return teleporters;
    }


    public List<TeleporterInfo> getTeleportersWhereTrusted(UUID playerUuid) {
        Set<TeleporterInfo> trusted = this.teleportersByTrustedPlayer.get(playerUuid);
        if (trusted == null || trusted.isEmpty()) {
            return List.of();
        }
        return List.copyOf(trusted);
    }


    public List<TeleporterInfo> getVisibleTeleporters(UUID playerUuid) {
        return this.teleportersByLocation.values().stream()
        .filter(info -> info.canPlayerSee(playerUuid))
        .toList();
    }

    public Collection<TeleporterInfo> getAllTeleporters() {
        return Collections.unmodifiableCollection(this.teleportersByLocation.values());
    }

    public int getTeleporterCount() {
        return this.teleportersByLocation.size();
    }


    public boolean shouldShowOnMap(TeleporterInfo info) {
        if (info == null) return false;
        if (info.isPrivate()) return false;
        if (info.hideMapWaypoint()) return false;
        return true;
    }

    public boolean canPlayerUseTeleporter(UUID playerUuid, String dimension, int x, int y, int z) {
        TeleporterInfo info = getTeleporter(dimension, x, y, z);
        return (info == null || info.canPlayerUse(playerUuid));
    }


    public boolean setTeleporterPrivate(String dimension, int x, int y, int z, boolean isPrivate) {
        TeleporterInfo info = getTeleporter(dimension, x, y, z);
        if (info == null) return false;

        boolean wasPrivate = info.isPrivate();
        info.setPrivate(isPrivate);


        String warpName = info.warpName();
        if (warpName != null && !warpName.isEmpty() && wasPrivate != isPrivate) {
            if (isPrivate) {
                hideWarpFromRegistry(warpName);
            } else {
                restoreWarpToRegistry(warpName);
            }
        }

        markDirty();
        return true;
    }

    public boolean setTeleporterRestricted(String dimension, int x, int y, int z, boolean isRestricted) {
        TeleporterInfo info = getTeleporter(dimension, x, y, z);
        if (info == null) return false;

        boolean wasRestricted = info.isRestricted();
        info.setRestricted(isRestricted);


        if (this.restrictionSystem != null && wasRestricted != isRestricted) {
            World world = this.worlds.get(info.dimension());
            if (world != null) {
                this.restrictionSystem.onRestrictionChanged(info, world, isRestricted);
            }
        }

        markDirty();
        return true;
    }

    public boolean setTeleporterInteractionLocked(String dimension, int x, int y, int z, boolean isInteractionLocked) {
        TeleporterInfo info = getTeleporter(dimension, x, y, z);
        if (info == null) return false;

        info.setInteractionLocked(isInteractionLocked);
        markDirty();
        return true;
    }

    public boolean setTeleporterBreakLocked(String dimension, int x, int y, int z, boolean isBreakLocked) {
        TeleporterInfo info = getTeleporter(dimension, x, y, z);
        if (info == null) return false;

        info.setBreakLocked(isBreakLocked);
        markDirty();
        return true;
    }

    public boolean setTeleporterDisplayWorld(String dimension, int x, int y, int z, boolean displayWorld) {
        TeleporterInfo info = getTeleporter(dimension, x, y, z);
        if (info == null) return false;

        info.setDisplayWorld(displayWorld);
        markDirty();
        return true;
    }

    public boolean setTeleporterDisplayCoordinates(String dimension, int x, int y, int z, boolean displayCoordinates) {
        TeleporterInfo info = getTeleporter(dimension, x, y, z);
        if (info == null) return false;

        info.setDisplayCoordinates(displayCoordinates);
        markDirty();
        return true;
    }


    public boolean toggleBypassMode(UUID playerUuid) {
        if (this.bypassPlayers.contains(playerUuid)) {
            this.bypassPlayers.remove(playerUuid);
            return false;
        }
        this.bypassPlayers.add(playerUuid);
        return true;
    }


    public boolean isInBypassMode(UUID playerUuid) {
        return this.bypassPlayers.contains(playerUuid);
    }


    public void removeBypassMode(UUID playerUuid) {
        this.bypassPlayers.remove(playerUuid);
    }


    public void recordCrossWorldTeleport(UUID playerUuid) {
        this.crossWorldTeleportTime.put(playerUuid, Long.valueOf(System.currentTimeMillis()));
    }


    public boolean isOnCrossWorldCooldown(UUID playerUuid) {
        Long lastTeleport = this.crossWorldTeleportTime.get(playerUuid);
        if (lastTeleport == null) {
            return false;
        }
        return (System.currentTimeMillis() - lastTeleport.longValue() < 5000L);
    }


    public long getCrossWorldCooldownRemaining(UUID playerUuid) {
        Long lastTeleport = this.crossWorldTeleportTime.get(playerUuid);
        if (lastTeleport == null) {
            return 0L;
        }
        long elapsed = System.currentTimeMillis() - lastTeleport.longValue();
        long remaining = 5000L - elapsed;
        return Math.max(0L, remaining);
    }


    public void clearCrossWorldCooldown(UUID playerUuid) {
        this.crossWorldTeleportTime.remove(playerUuid);
    }


    public boolean hideWarpFromRegistry(String warpId) {
        if (warpId == null || warpId.isEmpty()) return false;

        try {
            Map<String, Warp> gameWarps = TeleportPlugin.get().getWarps();
            String lowerWarpId = warpId.toLowerCase();

            Warp warp = gameWarps.remove(lowerWarpId);
            if (warp != null) {
                this.hiddenWarps.put(lowerWarpId, warp);
                this.logger.at(Level.INFO).log("Hidden warp from registry: " + warpId);
                return true;
            }
        } catch (Exception e) {
            this.logger.at(Level.WARNING).log("Failed to hide warp: " + e.getMessage());
        }
        return false;
    }

    public boolean restoreWarpToRegistry(String warpId) {
        if (warpId == null || warpId.isEmpty()) return false;

        try {
            String lowerWarpId = warpId.toLowerCase();
            Warp warp = this.hiddenWarps.remove(lowerWarpId);

            if (warp != null) {
                TeleportPlugin.get().getWarps().put(lowerWarpId, warp);
                this.logger.at(Level.INFO).log("Restored warp to registry: " + warpId);
                return true;
            }
        } catch (Exception e) {
            this.logger.at(Level.WARNING).log("Failed to restore warp: " + e.getMessage());
        }
        return false;
    }

    public boolean renameWarpInRegistry(String oldWarpId, String newWarpId) {
        if (oldWarpId == null || oldWarpId.isEmpty()) return false;

        try {
            Map<String, Warp> gameWarps = TeleportPlugin.get().getWarps();
            String lowerOldId = oldWarpId.toLowerCase();

            Warp removedFromGame = gameWarps.remove(lowerOldId);
            Warp removedFromHidden = this.hiddenWarps.remove(lowerOldId);

            if (removedFromGame != null || removedFromHidden != null) {
                this.logger.at(Level.INFO).log("Removed old warp: %s%s"
                .formatted(new Object[] { oldWarpId, (newWarpId != null) ? (" (renaming to " + newWarpId + ")") : "" }));
                return true;
            }
        } catch (Exception e) {
            this.logger.at(Level.WARNING).log("Failed to remove old warp: " + e.getMessage());
        }
        return false;
    }


    public boolean removeWarpFromRegistry(String warpId) {
        if (warpId == null || warpId.isEmpty()) return false;

        try {
            Map<String, Warp> gameWarps = TeleportPlugin.get().getWarps();
            String lowerWarpId = warpId.toLowerCase();

            Warp removedFromGame = gameWarps.remove(lowerWarpId);
            Warp removedFromHidden = this.hiddenWarps.remove(lowerWarpId);

            if (removedFromGame != null || removedFromHidden != null) {
                this.logger.at(Level.INFO).log("Permanently removed warp from registry: " + warpId);
                return true;
            }
        } catch (Exception e) {
            this.logger.at(Level.WARNING).log("Failed to remove warp from registry: " + e.getMessage());
        }
        return false;
    }

    public boolean isWarpHidden(String warpId) {
        return (warpId != null && !warpId.isEmpty() && this.hiddenWarps.containsKey(warpId.toLowerCase()));
    }
    @Nullable
    public Warp getWarp(String warpId) {
        if (warpId == null || warpId.isEmpty()) return null;

        try {
            Map<String, Warp> gameWarps = TeleportPlugin.get().getWarps();


            Warp warp = gameWarps.get(warpId);
            if (warp != null) return warp;


            String str = warpId.toLowerCase();
            if (!warpId.equals(str)) {
                warp = gameWarps.get(str);
                if (warp != null) return warp;
            }
        } catch (Exception exception) {}


        Warp hidden = this.hiddenWarps.get(warpId);
        if (hidden != null) return hidden;

        String lowerWarpId = warpId.toLowerCase();
        if (!warpId.equals(lowerWarpId)) {
            return this.hiddenWarps.get(lowerWarpId);
        }
        return null;
    }

    public List<String> getVisibleWarps(UUID playerUuid) {
        List<String> visibleWarps = new ArrayList<>();
        boolean isBypassing = isInBypassMode(playerUuid);


        if (isBypassing) {

            for (TeleporterInfo info : getAllTeleporters()) {
                String warpName = info.warpName();
                if (warpName != null && !warpName.isEmpty()) {
                    String lower = warpName.toLowerCase();
                    if (!visibleWarps.contains(lower)) {
                        visibleWarps.add(lower);
                    }
                }
            }


            try {
                for (String warpKey : TeleportPlugin.get().getWarps().keySet()) {
                    String lower = warpKey.toLowerCase();

                    if (!isCustomDestination(lower) &&
                    !visibleWarps.contains(lower)) {
                        visibleWarps.add(lower);
                    }
                }
            } catch (Exception e) {
                this.logger.at(Level.WARNING).log("Failed to get game warps: " + e.getMessage());
            }


            for (String hiddenWarpId : this.hiddenWarps.keySet()) {

                if (!isCustomDestination(hiddenWarpId) &&
                !visibleWarps.contains(hiddenWarpId)) {
                    visibleWarps.add(hiddenWarpId);
                }
            }

            return visibleWarps;
        }


        Set<String> privateWarpsNotOwned = new HashSet<>();


        for (TeleporterInfo info : getAllTeleporters()) {
            String warpName = info.warpName();
            if (warpName == null || warpName.isEmpty())
            continue;
            String lower = warpName.toLowerCase();

            if (info.isPrivate()) {
                if (info.isOwner(playerUuid)) {

                    if (!visibleWarps.contains(lower)) {
                        visibleWarps.add(lower);
                    }
                    continue;
                }
                privateWarpsNotOwned.add(lower);

                continue;
            }
            if (!visibleWarps.contains(lower)) {
                visibleWarps.add(lower);
            }
        }


        try {
            for (String warpKey : TeleportPlugin.get().getWarps().keySet()) {
                String lower = warpKey.toLowerCase();

                if (isCustomDestination(lower))
                continue;
                if (!privateWarpsNotOwned.contains(lower) && !visibleWarps.contains(lower)) {
                    visibleWarps.add(lower);
                }
            }
        } catch (Exception e) {
            this.logger.at(Level.WARNING).log("Failed to get game warps: " + e.getMessage());
        }

        return visibleWarps;
    }


    @Nullable
    public Teleporter getTeleporterComponent(World world, int blockX, int blockY, int blockZ) {
        if (world == null) return null;

        try {
            ChunkStore chunkStore = world.getChunkStore();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);

            BlockComponentChunk blockComponentChunk = (BlockComponentChunk)chunkStore.getChunkComponent(chunkIndex, BlockComponentChunk.getComponentType());
            if (blockComponentChunk == null) return null;

            int blockIndex = ChunkUtil.indexBlockInColumn(blockX, blockY, blockZ);
            Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);

            if (blockRef == null || !blockRef.isValid()) return null;

            return (Teleporter)chunkStore.getStore().getComponent(blockRef, Teleporter.getComponentType());
        } catch (Exception e) {
            this.logger.at(Level.WARNING).log("Failed to get teleporter component: " + e.getMessage());
            return null;
        }
    }

    public boolean syncTeleporterInfoWithComponent(TeleporterInfo info) {
        if (info == null) return false;

        World world = this.worlds.get(info.dimension());
        if (world == null) return false;

        Teleporter teleporter = getTeleporterComponent(world, info.blockX(), info.blockY(), info.blockZ());
        if (teleporter == null) return false;

        boolean changed = false;


        String ownedWarp = teleporter.getOwnedWarp();
        if (ownedWarp != null && !ownedWarp.isEmpty() && !ownedWarp.equals(info.warpName())) {
            info.setWarpName(ownedWarp);
            changed = true;
        }


        String componentDest = teleporter.getWarp();
        String currentDest = info.warpDestination();
        if (componentDest != null && !componentDest.isEmpty() && !componentDest.equals(currentDest)) {
            info.setWarpDestination(componentDest);
            changed = true;
        }

        if (changed) markDirty();
        return true;
    }

    public boolean updateTeleporterComponentWarpName(TeleporterInfo info, String newWarpName) {
        if (info == null) return false;

        World world = this.worlds.get(info.dimension());
        if (world == null) return false;

        try {
            ChunkStore chunkStore = world.getChunkStore();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(info.blockX(), info.blockZ());

            BlockComponentChunk blockComponentChunk = (BlockComponentChunk)chunkStore.getChunkComponent(chunkIndex, BlockComponentChunk.getComponentType());
            if (blockComponentChunk == null) return false;

            int blockIndex = ChunkUtil.indexBlockInColumn(info.blockX(), info.blockY(), info.blockZ());
            Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);

            if (blockRef == null || !blockRef.isValid()) return false;

            Teleporter teleporter = (Teleporter)chunkStore.getStore().getComponent(blockRef, Teleporter.getComponentType());
            if (teleporter == null) return false;

            String oldOwnedWarp = teleporter.getOwnedWarp();


            if (oldOwnedWarp != null && !oldOwnedWarp.isEmpty()) {
                TeleportPlugin.get().getWarps().remove(oldOwnedWarp.toLowerCase());
                this.hiddenWarps.remove(oldOwnedWarp.toLowerCase());
            }


            String currentWarp = teleporter.getWarp();
            boolean shouldUpdateWarp = (oldOwnedWarp != null && oldOwnedWarp.equalsIgnoreCase(currentWarp));


            world.execute(() -> {
                teleporter.setOwnedWarp(newWarpName);
                teleporter.setIsCustomName((newWarpName != null && !newWarpName.isEmpty()));


                if (shouldUpdateWarp) {
                    teleporter.setWarp(newWarpName);
                }
            });

            if (newWarpName != null && !newWarpName.isEmpty()) {
                WorldChunk worldChunk = (WorldChunk)chunkStore.getChunkComponent(chunkIndex, WorldChunk.getComponentType());
                BlockModule.BlockStateInfo blockStateInfo = (BlockModule.BlockStateInfo)chunkStore.getStore().getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());

                if (worldChunk != null && blockStateInfo != null) {
                    CreateWarpWhenTeleporterPlacedSystem.createWarp(worldChunk, blockStateInfo, newWarpName);
                }
            }

            this.logger.at(Level.INFO).log("Updated teleporter warp name: '%s' -> '%s'".formatted(new Object[] { oldOwnedWarp, newWarpName }));
            return true;
        }
        catch (Exception e) {
            this.logger.at(Level.SEVERE).log("Failed to update teleporter component: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTeleporterWarpDestination(TeleporterInfo info, String newDestination) {
        if (info == null) return false;

        World world = this.worlds.get(info.dimension());
        if (world == null) return false;

        try {
            ChunkStore chunkStore = world.getChunkStore();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(info.blockX(), info.blockZ());

            BlockComponentChunk blockComponentChunk = (BlockComponentChunk)chunkStore.getChunkComponent(chunkIndex, BlockComponentChunk.getComponentType());
            if (blockComponentChunk == null) return false;

            int blockIndex = ChunkUtil.indexBlockInColumn(info.blockX(), info.blockY(), info.blockZ());
            Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);

            if (blockRef == null || !blockRef.isValid()) return false;

            Teleporter teleporter = (Teleporter)chunkStore.getStore().getComponent(blockRef, Teleporter.getComponentType());
            if (teleporter == null) return false;

            BlockModule.BlockStateInfo blockStateInfo = (BlockModule.BlockStateInfo)chunkStore.getStore().getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
            Ref<ChunkStore> chunkRef = (blockStateInfo != null) ? blockStateInfo.getChunkRef() : null;


            WorldChunk worldChunk = (chunkRef != null && chunkRef.isValid()) ? (WorldChunk)chunkRef.getStore().getComponent(chunkRef, WorldChunk.getComponentType()) : null;


            String destToSet = newDestination;
            world.execute(() -> {
                teleporter.setWarp(destToSet);


                if (worldChunk != null) {
                    boolean hasDestination = (destToSet != null && !destToSet.isEmpty());

                    String newState = hasDestination ? "active" : "default";

                    BlockType blockType = worldChunk.getBlockType(info.blockX(), info.blockY(), info.blockZ());

                    if (blockType != null) {
                        String currentState = blockType.getStateForBlock(blockType);

                        if (currentState == null || !currentState.equals(newState)) {
                            BlockType variantBlockType = blockType.getBlockForState(newState);

                            if (variantBlockType != null) {
                                worldChunk.setBlockInteractionState(info.blockX(), info.blockY(), info.blockZ(), variantBlockType, newState, true);
                            }
                        }
                    }
                }

                if (blockStateInfo != null) {
                    blockStateInfo.markNeedsSaving();
                }
            });
            this.logger.at(Level.INFO).log("Updated teleporter destination to: " + newDestination);
            return true;
        }
        catch (Exception e) {
            this.logger.at(Level.SEVERE).log("Failed to update teleporter destination: " + e.getMessage());
            return false;
        }
    }


    public void ensureAllTeleporterWarpsExist() {
        int created = 0;
        int found = 0;
        int failed = 0;

        for (TeleporterInfo info : this.teleportersByLocation.values()) {
            if (info.warpName() == null || info.warpName().isEmpty())
            continue;
            String warpId = info.warpName().toLowerCase();


            Warp existingWarp = getWarp(info.warpName());
            if (existingWarp != null) {
                found++;


                continue;
            }

            World world = this.worlds.get(info.dimension());
            if (world == null) {
                failed++;


                continue;
            }


            try {
                Transform transform = new Transform(info.blockX() + 0.5D, info.blockY() + 0.65D, info.blockZ() + 0.5D);


                Warp warp = new Warp(transform, info.warpName(), world, "*Teleporter", Instant.ofEpochMilli(info.placedTimestamp()));


                boolean shouldHide = (info.isPrivate() || info.hideMapWaypoint());
                if (shouldHide) {

                    this.hiddenWarps.put(warpId, warp);
                    String reason = info.isPrivate() ? "private" : "hideMapWaypoint";
                    this.logger.at(Level.INFO).log("Recreated hidden warp from info (%s): %s".formatted(new Object[] { reason, info.warpName() }));
                } else {

                    TeleportPlugin.get().getWarps().put(warpId, warp);
                    this.logger.at(Level.INFO).log("Recreated public warp from info: " + info.warpName());
                }
                created++;
            }
            catch (Exception e) {
                this.logger.at(Level.WARNING).log("Failed to recreate warp for " + info.warpName() + ": " + e.getMessage());
                failed++;
            }
        }

        this.logger.at(Level.INFO).log("Warp recreation: %d found, %d created, %d failed".formatted(new Object[] { Integer.valueOf(found), Integer.valueOf(created), Integer.valueOf(failed) }));
    }

    public void syncPrivateWarpsWithRegistry() {
        int hiddenCount = 0;
        int syncedCount = 0;

        for (TeleporterInfo info : this.teleportersByLocation.values()) {

            if ((info.warpName() == null || info.warpName().isEmpty()) &&
            syncTeleporterInfoWithComponent(info)) {
                syncedCount++;
            }


            if (info.warpName() != null && !info.warpName().isEmpty() && info.isPrivate()) {
                String warpId = info.warpName().toLowerCase();


                try {
                    Map<String, Warp> gameWarps = TeleportPlugin.get().getWarps();
                    if (gameWarps.containsKey(warpId) && !this.hiddenWarps.containsKey(warpId)) {
                        Warp warp = gameWarps.remove(warpId);
                        if (warp != null) {
                            this.hiddenWarps.put(warpId, warp);
                            hiddenCount++;
                        }
                    }
                } catch (Exception exception) {}
            }
        }

        this.logger.at(Level.INFO).log("Synced warps: %d names, %d hidden".formatted(new Object[] { Integer.valueOf(syncedCount), Integer.valueOf(hiddenCount) }));
    }

    private void enforcePrivateWarps() {
        try {
            Map<String, Warp> gameWarps = TeleportPlugin.get().getWarps();

            for (TeleporterInfo info : this.teleportersByLocation.values()) {
                if (info.isPrivate() && info.warpName() != null && !info.warpName().isEmpty()) {
                    String warpId = info.warpName().toLowerCase();
                    if (gameWarps.containsKey(warpId) && !this.hiddenWarps.containsKey(warpId)) {
                        Warp warp = gameWarps.remove(warpId);
                        if (warp != null) {
                            this.hiddenWarps.put(warpId, warp);
                        }
                    }
                }
            }
        } catch (Exception exception) {}
    }


    public void markDirty() {
        this.isDirty.set(true);
    }

    public void forceSave() {
        this.isDirty.set(false);
        performSave();
    }

    private void performSave() {
        try {
            FileUtils.ensureMainDirectory();
            FileUtils.ensureFile(FileUtils.TELEPORTERS_PATH, "{}");
            this.teleporterFile.syncSave();
            this.logger.at(Level.INFO).log("Saved %d teleporters".formatted(new Object[] { Integer.valueOf(this.teleportersByLocation.size()) }));
        } catch (Exception e) {
            this.logger.at(Level.SEVERE).log("Error saving teleporter data: " + e.getMessage());
        }


        try {
            FileUtils.ensureFile(FileUtils.CUSTOM_DESTINATIONS_PATH, "{\"Destinations\":[]}");
            this.customDestinationFile.getDestinations().clear();
            this.customDestinationFile.getDestinations().putAll(this.customDestinations);
            this.customDestinationFile.syncSave();
        } catch (Exception e) {
            this.logger.at(Level.SEVERE).log("Error saving custom destinations: " + e.getMessage());
        }
    }

    public int getNewLimit() {
        if (Main.CONFIG != null) {
            return ((ExtendedTeleportConfig)Main.CONFIG.get()).getTeleporterLimit();
        }
        return 9999;
    }


    public boolean createCustomDestination(String name, String dimension, double x, double y, double z, @Nullable String creatorUuid) {
        if (name == null || name.isEmpty()) return false;

        String lowerName = name.toLowerCase();


        if (this.customDestinations.containsKey(lowerName)) {
            return false;
        }


        if (TeleportPlugin.get().getWarps().containsKey(lowerName)) {
            return false;
        }

        CustomDestination dest = new CustomDestination(name, dimension, x, y, z, System.currentTimeMillis(), creatorUuid);
        this.customDestinations.put(lowerName, dest);


        registerCustomDestinationAsWarp(dest);

        markDirty();
        this.logger.at(Level.INFO).log("Created custom destination: " + String.valueOf(dest));
        return true;
    }


    public boolean removeCustomDestination(String name) {
        if (name == null || name.isEmpty()) return false;

        String lowerName = name.toLowerCase();
        CustomDestination removed = this.customDestinations.remove(lowerName);

        if (removed != null) {

            TeleportPlugin.get().getWarps().remove(lowerName);
            this.hiddenWarps.remove(lowerName);
            markDirty();
            this.logger.at(Level.INFO).log("Removed custom destination: " + name);
            return true;
        }

        return false;
    }


    @Nullable
    public CustomDestination getCustomDestination(String name) {
        if (name == null || name.isEmpty()) return null;
        return this.customDestinations.get(name.toLowerCase());
    }


    public Collection<CustomDestination> getAllCustomDestinations() {
        return Collections.unmodifiableCollection(this.customDestinations.values());
    }


    public boolean customDestinationExists(String name) {
        if (name == null || name.isEmpty()) return false;
        return this.customDestinations.containsKey(name.toLowerCase());
    }


    private void registerCustomDestinationAsWarp(CustomDestination dest) {
        try {
            World world = this.worlds.get(dest.dimension());
            if (world == null) {
                this.logger.at(Level.WARNING).log("Cannot register custom destination '%s': world '%s' not loaded"
                .formatted(new Object[] { dest.name(), dest.dimension() }));

                return;
            }

            Transform transform = new Transform(dest.x(), dest.y(), dest.z());


            Warp warp = new Warp(transform, dest.name().toLowerCase(), world, "*CustomDestination", Instant.ofEpochMilli(dest.createdTimestamp()));


            TeleportPlugin.get().getWarps().put(dest.name().toLowerCase(), warp);


            this.hiddenWarps.put(dest.name().toLowerCase(), warp);

            this.logger.at(Level.INFO).log("Registered custom destination as hidden warp: " + dest.name());
        } catch (Exception e) {
            this.logger.at(Level.WARNING).log("Failed to register custom destination as warp: " + e.getMessage());
        }
    }


    public boolean isCustomDestination(String warpName) {
        if (warpName == null || warpName.isEmpty()) return false;
        return this.customDestinations.containsKey(warpName.toLowerCase());
    }


    public void registerAllCustomDestinationsAsWarps() {
        for (CustomDestination dest : this.customDestinations.values()) {
            registerCustomDestinationAsWarp(dest);
        }
        this.logger.at(Level.INFO).log("Registered %d custom destinations as warps".formatted(new Object[] { Integer.valueOf(this.customDestinations.size()) }));
    }


    public void shutdown() {
        this.isShuttingDown.set(true);

        if (this.saveThread != null) {
            this.saveThread.interrupt();
        }

        if (this.isDirty.get()) {
            performSave();
        }

        this.logger.at(Level.INFO).log("Shutdown complete");
    }
}