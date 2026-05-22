package com.hytale.extendedteleport.system;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hytale.extendedteleport.TeleporterManager;
import com.hytale.extendedteleport.data.TeleporterInfo;
import java.util.UUID;
import javax.annotation.Nonnull;

public final class TeleporterBreakBlockEventSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public TeleporterBreakBlockEventSystem() {
        super(BreakBlockEvent.class);
    }

    public void handle(int entityIndex, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull BreakBlockEvent event) {
        PlayerRef playerRef = (PlayerRef) chunk.getComponent(entityIndex, PlayerRef.getComponentType());
        Player player = (Player) chunk.getComponent(entityIndex, Player.getComponentType());
        if (playerRef == null || player == null) return;

        Vector3i targetBlock = event.getTargetBlock();
        World world = player.getWorld();

        TeleporterManager manager = TeleporterManager.getInstance();
        UUID playerUuid = playerRef.getUuid();

        if (manager.isInBypassMode(playerUuid)) return;

        TeleporterInfo info = manager.getTeleporter(world.getName(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
        if (info == null) return;

        if (info.isOwner(playerUuid)) return;
        if (info.canSubownerBreak(playerUuid)) return;

        event.setCancelled(true);
    }

    public Query<EntityStore> getQuery() {
        return (Query<EntityStore>) PlayerRef.getComponentType();
    }
}
