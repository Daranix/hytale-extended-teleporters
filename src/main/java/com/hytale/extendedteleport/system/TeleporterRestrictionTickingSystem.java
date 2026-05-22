package com.hytale.extendedteleport.system;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hytale.extendedteleport.TeleporterManager;
import com.hytale.extendedteleport.data.TeleporterInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;


public final class TeleporterRestrictionTickingSystem
extends EntityTickingSystem<EntityStore>
{
    private static final double OWNER_ACTIVATION_RADIUS = 5.0D;
    private static final double OWNER_ACTIVATION_RADIUS_SQ = 25.0D;
    private static final long CHUNK_RELOAD_THRESHOLD_TICKS = 100L;
    private final Map<String, Boolean> teleporterActiveState = new ConcurrentHashMap<>();


    private final Map<Long, Long> seenChunks = new ConcurrentHashMap<>();
    private long currentTick = 0L;


    public void tick(float deltaTime, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        this.currentTick++;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());
        Player player = (Player)store.getComponent(ref, Player.getComponentType());

        if (playerRef == null || player == null)
        return;
        UUID playerUuid = playerRef.getUuid();
        Vector3d playerPos = playerRef.getTransform().getPosition();
        World world = player.getWorld();
        String dimension = world.getName();

        TeleporterManager manager = TeleporterManager.getInstance();


        List<TeleporterInfo> ownedTeleporters = manager.getPlayerTeleporters(playerUuid);


        List<TeleporterInfo> trustedTeleporters = manager.getTeleportersWhereTrusted(playerUuid);


        for (TeleporterInfo info : ownedTeleporters) {
            processRestrictedTeleporter(info, playerPos, world, dimension, true);
        }


        for (TeleporterInfo info : trustedTeleporters) {
            processRestrictedTeleporter(info, playerPos, world, dimension, false);
        }
    }


    private void processRestrictedTeleporter(TeleporterInfo info, Vector3d playerPos, World world, String dimension, boolean isOwner) {
        if (!info.isRestricted() || !info.dimension().equals(dimension)) {
            return;
        }


        long chunkIndex = ChunkUtil.indexChunkFromBlock(info.blockX(), info.blockZ());
        boolean chunkReloaded = detectChunkReload(chunkIndex);


        double dx = playerPos.getX() - info.blockX() + 0.5D;
        double dy = playerPos.getY() - info.blockY() + 0.5D;
        double dz = playerPos.getZ() - info.blockZ() + 0.5D;
        double distSq = dx * dx + dy * dy + dz * dz;

        boolean playerNearby = (distSq <= 25.0D);
        String locationKey = info.locationKey();

        Boolean wasActive = this.teleporterActiveState.get(locationKey);


        if (playerNearby) {
            if (wasActive == null || !wasActive.booleanValue() || chunkReloaded || isComponentStateOutOfSync(world, info, true)) {
                updateTeleporterState(world, info, true);
                this.teleporterActiveState.put(locationKey, Boolean.valueOf(true));
            }

            return;
        }

        if (!isOwner) {
            return;
        }


        boolean anyTrustedNearby = isAnyTrustedPlayerNearby(info, world);

        if (anyTrustedNearby) {

            if (wasActive == null || !wasActive.booleanValue() || chunkReloaded) {
                this.teleporterActiveState.put(locationKey, Boolean.valueOf(true));
            }

            return;
        }

        boolean stateChanged = (wasActive == null || wasActive.booleanValue() || chunkReloaded);

        if (!stateChanged) {
            stateChanged = isComponentStateOutOfSync(world, info, false);
        }

        if (stateChanged) {
            updateTeleporterState(world, info, false);
            this.teleporterActiveState.put(locationKey, Boolean.valueOf(false));
        }
    }


    private boolean isAnyTrustedPlayerNearby(TeleporterInfo info, World world) {
        Set<UUID> trustedPlayers = info.getTrustedPlayers();
        if (trustedPlayers.isEmpty()) {
            return false;
        }

        double teleporterX = info.blockX() + 0.5D;
        double teleporterY = info.blockY() + 0.5D;
        double teleporterZ = info.blockZ() + 0.5D;
        String teleporterDimension = info.dimension();

        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            Vector3d playerPos; if (!trustedPlayers.contains(playerRef.getUuid())) {
                continue;
            }


            try { playerPos = playerRef.getTransform().getPosition();
                if (playerPos == null)
            continue;  } catch (Exception e)
            { continue; }


            double dx = playerPos.getX() - teleporterX;
            double dy = playerPos.getY() - teleporterY;
            double dz = playerPos.getZ() - teleporterZ;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq <= 25.0D) {
                return true;
            }
        }

        return false;
    }


    private boolean detectChunkReload(long chunkIndex) {
        Long lastSeen = this.seenChunks.get(Long.valueOf(chunkIndex));
        this.seenChunks.put(Long.valueOf(chunkIndex), Long.valueOf(this.currentTick));


        return (lastSeen == null || this.currentTick - lastSeen.longValue() > 100L);
    }


    private boolean isComponentStateOutOfSync(World world, TeleporterInfo info, boolean shouldBeActive) {
        try {
            Teleporter teleporter = getTeleporterComponent(world, info);
            if (teleporter == null) return false;

            String currentWarp = teleporter.getWarp();
            boolean componentHasWarp = (currentWarp != null && !currentWarp.isEmpty());


            return (componentHasWarp != shouldBeActive);
        } catch (Exception e) {
            return false;
        }
    }


    private void updateTeleporterState(World world, TeleporterInfo info, boolean activate) {
        int blockX = info.blockX();
        int blockY = info.blockY();
        int blockZ = info.blockZ();

        if (activate) {


            String persistedWarp = info.warpDestination();


            world.execute(() -> {
                Teleporter teleporter = getTeleporterComponent(world, info);

                if (teleporter == null) {
                    return;
                }

                String warpToRestore = persistedWarp;

                if (warpToRestore == null || warpToRestore.isEmpty()) {
                    String componentWarp = teleporter.getWarp();

                    if (componentWarp != null && !componentWarp.isEmpty()) {
                        warpToRestore = componentWarp;

                        info.setWarpDestination(componentWarp);

                        TeleporterManager.getInstance().markDirty();
                    }
                }

                if (warpToRestore != null && !warpToRestore.isEmpty()) {
                    teleporter.setWarp(warpToRestore);
                    updateBlockState(world, blockX, blockY, blockZ, true);
                } else {
                    updateBlockState(world, blockX, blockY, blockZ, false);
                }
            });
        } else {
            world.execute(() -> {
                Teleporter teleporter = getTeleporterComponent(world, info);
                if (teleporter == null) {
                    return;
                }
                String currentWarp = teleporter.getWarp();
                if (currentWarp != null && !currentWarp.isEmpty()) {
                    info.setWarpDestination(currentWarp);
                    TeleporterManager.getInstance().markDirty();
                }
                teleporter.setWarp(null);
                updateBlockState(world, blockX, blockY, blockZ, false);
            });
        }
    }


    private void updateBlockState(World world, int blockX, int blockY, int blockZ, boolean isActive) {
        try {
            ChunkStore chunkStore = world.getChunkStore();
            if (chunkStore == null)
            return;
            long chunkIndex = ChunkUtil.indexChunkFromBlock(blockX, blockZ);

            BlockComponentChunk blockComponentChunk = (BlockComponentChunk)chunkStore.getChunkComponent(chunkIndex, BlockComponentChunk.getComponentType());
            if (blockComponentChunk == null)
            return;
            int blockIndex = ChunkUtil.indexBlockInColumn(blockX, blockY, blockZ);
            Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
            if (blockRef == null || !blockRef.isValid())
            return;
            BlockModule.BlockStateInfo blockStateInfo = (BlockModule.BlockStateInfo)chunkStore.getStore().getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
            if (blockStateInfo == null)
            return;
            Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
            if (chunkRef == null || !chunkRef.isValid())
            return;
            WorldChunk worldChunk = (WorldChunk)chunkRef.getStore().getComponent(chunkRef, WorldChunk.getComponentType());
            if (worldChunk == null)
            return;
            String newState = isActive ? "active" : "default";
            BlockType blockType = worldChunk.getBlockType(blockX, blockY, blockZ);
            if (blockType != null) {
                String currentState = blockType.getStateForBlock(blockType);
                if (currentState == null || !currentState.equals(newState)) {
                    BlockType variantBlockType = blockType.getBlockForState(newState);
                    if (variantBlockType != null) {
                        worldChunk.setBlockInteractionState(blockX, blockY, blockZ, variantBlockType, newState, true);
                    }
                }

            }

        }
        catch (Exception exception) {}
    }


    private Teleporter getTeleporterComponent(World world, TeleporterInfo info) {
        try {
            ChunkStore chunkStore = world.getChunkStore();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(info.blockX(), info.blockZ());

            BlockComponentChunk blockChunk = (BlockComponentChunk)chunkStore.getChunkComponent(chunkIndex, BlockComponentChunk.getComponentType());
            if (blockChunk == null) return null;

            int blockIndex = ChunkUtil.indexBlockInColumn(info.blockX(), info.blockY(), info.blockZ());
            Ref<ChunkStore> blockRef = blockChunk.getEntityReference(blockIndex);

            if (blockRef == null || !blockRef.isValid()) return null;

            return (Teleporter)chunkStore.getStore().getComponent(blockRef, Teleporter.getComponentType());
        } catch (Exception e) {
            return null;
        }
    }


    public void onTeleporterRegistered(TeleporterInfo info, World world) {
        if (!info.isRestricted() || !info.hasOwner())
        return;
        String locationKey = info.locationKey();


        updateTeleporterState(world, info, false);
        this.teleporterActiveState.put(locationKey, Boolean.valueOf(false));
    }


    public void onRestrictionChanged(TeleporterInfo info, World world, boolean isNowRestricted) {
        String locationKey = info.locationKey();

        if (isNowRestricted) {

            updateTeleporterState(world, info, false);
            this.teleporterActiveState.put(locationKey, Boolean.valueOf(false));
        } else {

            this.teleporterActiveState.remove(locationKey);

            String warpToRestore = info.warpDestination();

            if (warpToRestore != null && !warpToRestore.isEmpty()) {
                String finalWarpToRestore = warpToRestore;
                int blockX = info.blockX();
                int blockY = info.blockY();
                int blockZ = info.blockZ();


                world.execute(() -> {
                    Teleporter teleporter = getTeleporterComponent(world, info);
                    if (teleporter != null) {
                        teleporter.setWarp(finalWarpToRestore);
                    }
                    updateBlockState(world, blockX, blockY, blockZ, true);
                });
            }
        }
    }


    public void onTeleporterRemoved(String locationKey) {
        this.teleporterActiveState.remove(locationKey);
    }


    public void onChunkUnloaded(long chunkIndex) {
        this.seenChunks.remove(Long.valueOf(chunkIndex));
    }


    @NullableDecl
    public Query<EntityStore> getQuery() {
        return (Query<EntityStore>)PlayerRef.getComponentType();
    }
}