package com.hytale.extendedteleport.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hytale.extendedteleport.TeleporterManager;
import java.util.Set;
import javax.annotation.Nonnull;

public final class TeleporterPlaceBlockEventSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private static final Set<String> TELEPORTER_BLOCKS = Set.of("Teleporter");

    public TeleporterPlaceBlockEventSystem() {
        super(PlaceBlockEvent.class);
    }

    public void handle(int entityIndex, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PlaceBlockEvent event) {
        PlayerRef playerRef = (PlayerRef) chunk.getComponent(entityIndex, PlayerRef.getComponentType());
        Player player = (Player) chunk.getComponent(entityIndex, Player.getComponentType());
        if (playerRef == null || player == null) return;

        String blockKey = event.getItemInHand().getBlockKey();
        if (blockKey == null || !TELEPORTER_BLOCKS.contains(blockKey)) return;

        Vector3i targetBlock = event.getTargetBlock();
        World world = player.getWorld();

        TeleporterManager.getInstance().onTeleporterPlaced(playerRef.getUuid(), world, targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
    }

    public Query<EntityStore> getQuery() {
        return (Query<EntityStore>) PlayerRef.getComponentType();
    }
}
