package com.hytale.extendedteleport.interaction;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec.Builder;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule.BlockStateInfo;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.PendingTeleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import java.util.List;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hytale.extendedteleport.TeleporterManager;
import com.hytale.extendedteleport.data.TeleporterInfo;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExtendedTeleporterInteraction extends SimpleBlockInteraction {
   @Nonnull
   public static final BuilderCodec<ExtendedTeleporterInteraction> CODEC = ((Builder)BuilderCodec.builder(
            ExtendedTeleporterInteraction.class, ExtendedTeleporterInteraction::new, SimpleBlockInteraction.CODEC
         )
         .appendInherited(
            new KeyedCodec("Particle", Codec.STRING),
            (interaction, s) -> interaction.particle = s,
            interaction -> interaction.particle,
            (interaction, parent) -> interaction.particle = parent.particle
         )
         .documentation("The particle to play on the entity when teleporting.")
         .add())
      .build();
   @Nullable
   private String particle;

   @Nonnull
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(world, targetBlock.x, targetBlock.y, targetBlock.z);
      if (blockRef != null && blockRef.isValid()) {
         BlockStateInfo blockStateInfoComponent = (BlockStateInfo)blockRef.getStore().getComponent(blockRef, BlockStateInfo.getComponentType());
         if (blockStateInfoComponent != null) {
            Ref<ChunkStore> sectionRef = blockStateInfoComponent.getSectionRef();
            if (sectionRef != null && sectionRef.isValid()) {
               Teleporter teleporter = (Teleporter)world.getChunkStore().getStore().getComponent(blockRef, Teleporter.getComponentType());
               if (teleporter != null) {
                     Ref<EntityStore> ref = context.getEntity();
                     Player playerComponent = (Player)commandBuffer.getComponent(ref, Player.getComponentType());
                     if (playerComponent == null || !playerComponent.isWaitingForClientReady()) {
                        Archetype<EntityStore> archetype = commandBuffer.getArchetype(ref);
                        if (!archetype.contains(Teleport.getComponentType()) && !archetype.contains(PendingTeleport.getComponentType())) {
                           TeleporterManager manager = TeleporterManager.getInstance();
                           TeleporterInfo info = manager.getTeleporter(world.getName(), targetBlock.x, targetBlock.y, targetBlock.z);
                           PlayerRef playerRef = (PlayerRef)commandBuffer.getComponent(ref, PlayerRef.getComponentType());
                           UUID playerUuid = playerRef != null ? playerRef.getUuid() : null;
                           if (info == null
                              || info.isSelfDestruct()
                              || playerUuid == null
                              || info.canPlayerUse(playerUuid)
                              || manager.isInBypassMode(playerUuid)) {
                              String warpName = teleporter.getWarp();
                              if ((warpName == null || warpName.isEmpty()) && info != null) {
                                 String infoDestination = info.warpDestination();
                                 if (infoDestination != null && !infoDestination.isEmpty()) {
                                    warpName = infoDestination;
                                    String warpToRestore = warpName;
                                    world.execute(
                                       () -> {
                                          teleporter.setWarp(warpToRestore);
                                          BlockStateInfo blockStateInfo = (BlockStateInfo)blockRef.getStore()
                                             .getComponent(blockRef, BlockStateInfo.getComponentType());
                                          if (blockStateInfo != null) {
                                             blockStateInfo.markNeedsSaving();
                                          }
                                       }
                                    );
                                 }
                              }

                              boolean hasValidDestination = this.isValidDestination(teleporter, warpName);
                              if (!hasValidDestination) {
                                 BlockSection blockSection = (BlockSection)sectionRef.getStore().getComponent(sectionRef, BlockSection.getComponentType());
                                 if (blockSection != null) {
                                    int blockId = blockSection.get(blockStateInfoComponent.getIndex());
                                    BlockType blockType = (BlockType)BlockType.getAssetMap().getAsset(blockId);
                                    if (blockType != null) {
                                       String currentState = blockType.getStateForBlock(blockType);
                                       if (!"default".equals(currentState)) {
                                          BlockType variantBlockType = blockType.getBlockForState("default");
                                          if (variantBlockType != null) {
                                             BlockOperations.setBlockInteractionState(
                                                world.getChunkStore(), sectionRef, targetBlock.x, targetBlock.y, targetBlock.z, variantBlockType, "default", true
                                             );
                                          }
                                       }
                                    }
                                 }
                              } else {
                                 TransformComponent transformComponent = (TransformComponent)commandBuffer.getComponent(
                                    ref, TransformComponent.getComponentType()
                                 );
                                 if (transformComponent != null) {
                                    Teleport teleportComponent = this.toTeleportWithHiddenWarps(
                                       teleporter, warpName, transformComponent.getPosition(), transformComponent.getRotation(), targetBlock, world, playerUuid
                                    );
                                    if (teleportComponent != null) {
                                       commandBuffer.addComponent(ref, Teleport.getComponentType(), teleportComponent);
                                       if (this.particle != null) {
                                          Vector3d particlePosition = transformComponent.getPosition();
                                          SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = (SpatialResource<Ref<EntityStore>, EntityStore>)commandBuffer.getResource(
                                             EntityModule.get().getPlayerSpatialResourceType()
                                          );
                                           List<Ref<EntityStore>> results = SpatialResource.<EntityStore>getThreadLocalReferenceList();
                                          playerSpatialResource.getSpatialStructure().collect(particlePosition, 75.0, results);
                                          ParticleUtil.spawnParticleEffect(this.particle, particlePosition, results, commandBuffer);
                                       }

                                       if (info != null && info.isSingleUse()) {
                                          this.handleSingleUseTeleporter(world, info, teleporter, blockRef, sectionRef);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
   }

   private void handleSingleUseTeleporter(
      @Nonnull World world, @Nonnull TeleporterInfo info, @Nonnull Teleporter teleporter, @Nonnull Ref<ChunkStore> blockRef, @Nonnull Ref<ChunkStore> sectionRef
   ) {
      TeleporterManager manager = TeleporterManager.getInstance();
      info.setWarpDestination(null);
      manager.markDirty();
      int blockX = info.blockX();
      int blockY = info.blockY();
      int blockZ = info.blockZ();
      world.execute(() -> {
         teleporter.setWarp(null);
         BlockSection blockSection = (BlockSection)sectionRef.getStore().getComponent(sectionRef, BlockSection.getComponentType());
         if (blockSection != null) {
            BlockStateInfo blockStateInfo = (BlockStateInfo)blockRef.getStore().getComponent(blockRef, BlockStateInfo.getComponentType());
            if (blockStateInfo != null) {
               int blockId = blockSection.get(blockStateInfo.getIndex());
               BlockType blockType = (BlockType)BlockType.getAssetMap().getAsset(blockId);
               if (blockType != null) {
                  BlockType variantBlockType = blockType.getBlockForState("default");
                  if (variantBlockType != null) {
                     BlockOperations.setBlockInteractionState(world.getChunkStore(), sectionRef, blockX, blockY, blockZ, variantBlockType, "default", true);
                  }
               }
               blockStateInfo.markNeedsSaving();
            }
         }
      });
   }

   private boolean isValidDestination(Teleporter teleporter, @Nullable String warpName) {
      if (warpName != null && !warpName.isEmpty()) {
         if (TeleportPlugin.get().getWarps().get(warpName.toLowerCase()) != null) {
            return true;
         }

         Warp hiddenWarp = TeleporterManager.getInstance().getWarp(warpName);
         return hiddenWarp != null;
      } else if (teleporter.getTransform() != null) {
         return teleporter.getWorldUuid() != null ? Universe.get().getWorld(teleporter.getWorldUuid()) != null : true;
      } else {
         return false;
      }
   }

   @Nullable
   private Teleport toTeleportWithHiddenWarps(
      Teleporter teleporter,
      @Nullable String warpName,
      @Nonnull Vector3d currentPosition,
      @Nonnull Rotation3f currentRotation,
      @Nonnull Vector3i blockPosition,
      @Nonnull World currentWorld,
      @Nullable UUID playerUuid
   ) {
      if (warpName != null && !warpName.isEmpty()) {
         Warp targetWarp = (Warp)TeleportPlugin.get().getWarps().get(warpName.toLowerCase());
         boolean isInMainRegistry = targetWarp != null;
         if (targetWarp == null) {
            targetWarp = TeleporterManager.getInstance().getWarp(warpName);
         }

         if (targetWarp == null) {
            return null;
         }

         String targetWorldName = targetWarp.getWorld();
         String currentWorldName = currentWorld.getName();
         boolean isCrossWorld = targetWorldName != null && !targetWorldName.equals(currentWorldName);
         if (isCrossWorld) {
            TeleporterManager manager = TeleporterManager.getInstance();
            if (playerUuid != null && manager.isOnCrossWorldCooldown(playerUuid)) {
               return null;
            }

            boolean wasTemporarilyAdded = false;
            if (!isInMainRegistry) {
               TeleportPlugin.get().getWarps().put(warpName.toLowerCase(), targetWarp);
               wasTemporarilyAdded = true;
            }

            try {
               if (playerUuid != null) {
                  manager.recordCrossWorldTeleport(playerUuid);
               }

               return teleporter.toTeleport(currentPosition, currentRotation, blockPosition);
            } finally {
               if (wasTemporarilyAdded) {
                  TeleportPlugin.get().getWarps().remove(warpName.toLowerCase());
               }
            }
         } else {
            Vector3d targetPosition = targetWarp.getTransform().getPosition();
            return Teleport.createExact(targetPosition, currentRotation, currentRotation);
         }
      } else {
         return teleporter.toTeleport(currentPosition, currentRotation, blockPosition);
      }
   }

   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }
}
