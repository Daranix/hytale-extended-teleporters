package com.hytale.extendedteleport.system;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hytale.extendedteleport.TeleporterManager;
import javax.annotation.Nonnull;

public final class TeleporterComponentRemovalSystem extends RefSystem<ChunkStore> {
   public void onEntityAdded(
      @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
   }

   public void onEntityRemove(
      @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      if (reason == RemoveReason.REMOVE) {
         Teleporter teleporter = (Teleporter)commandBuffer.getComponent(ref, Teleporter.getComponentType());
         if (teleporter != null) {
            BlockStateInfo blockStateInfo = (BlockStateInfo)commandBuffer.getComponent(ref, BlockStateInfo.getComponentType());
            if (blockStateInfo != null) {
               Vector3i pos = new Vector3i();
               if (blockStateInfo.fillWorldPos(commandBuffer, pos)) {
                  ChunkStore chunkStore = (ChunkStore)store.getExternalData();
                  if (chunkStore != null && chunkStore.getWorld() != null) {
                     String worldName = chunkStore.getWorld().getName();
                     TeleporterManager.getInstance().onTeleporterRemoved(worldName, pos.x, pos.y, pos.z);
                  }
               }
            }
         }
      }
   }

   public Query<ChunkStore> getQuery() {
      return Teleporter.getComponentType();
   }
}
