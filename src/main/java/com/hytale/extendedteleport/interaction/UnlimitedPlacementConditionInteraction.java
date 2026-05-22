package com.hytale.extendedteleport.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.blocktrack.BlockCounter;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class UnlimitedPlacementConditionInteraction
extends SimpleInstantInteraction
{
    private static final Set<String> UNLIMITED_BLOCKS = Set.of("Teleporter");

    public static final BuilderCodec<UnlimitedPlacementConditionInteraction> CODEC;

    private String blockType;

    private int value = 0;

    private boolean lessThan = true;

    static {
        BuilderCodec.Builder<UnlimitedPlacementConditionInteraction> builder = BuilderCodec.builder(UnlimitedPlacementConditionInteraction.class, UnlimitedPlacementConditionInteraction::new, SimpleInstantInteraction.CODEC);
        builder
        .appendInherited(new KeyedCodec<>("Block", Codec.STRING), (UnlimitedPlacementConditionInteraction o, String v) -> o.blockType = v, (UnlimitedPlacementConditionInteraction o) -> o.blockType, (UnlimitedPlacementConditionInteraction o, UnlimitedPlacementConditionInteraction p) -> o.blockType = p.blockType)
        .addValidator(Validators.nonNull())
        .add()
        .appendInherited(new KeyedCodec<>("Value", Codec.INTEGER), (UnlimitedPlacementConditionInteraction o, Integer v) -> o.value = v, (UnlimitedPlacementConditionInteraction o) -> o.value, (UnlimitedPlacementConditionInteraction o, UnlimitedPlacementConditionInteraction p) -> o.value = p.value)
        .add()
        .appendInherited(new KeyedCodec<>("LessThan", Codec.BOOLEAN), (UnlimitedPlacementConditionInteraction o, Boolean v) -> o.lessThan = v, (UnlimitedPlacementConditionInteraction o) -> o.lessThan, (UnlimitedPlacementConditionInteraction o, UnlimitedPlacementConditionInteraction p) -> o.lessThan = p.lessThan)
        .add();
        CODEC = builder.build();
    }

    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        if (UNLIMITED_BLOCKS.contains(this.blockType)) {
            (context.getState()).state = InteractionState.Finished;

            return;
        }

        BlockCounter counter = (BlockCounter)((EntityStore)context.getCommandBuffer().getExternalData()).getWorld().getChunkStore().getStore().getResource(BlockCounter.getResourceType());

        int blockCount = counter.getBlockPlacementCount(this.blockType);

        if (this.lessThan) {
            if (blockCount < this.value) {
                (context.getState()).state = InteractionState.Finished;
            } else {
                (context.getState()).state = InteractionState.Failed;
            }
        } else if (blockCount > this.value) {
            (context.getState()).state = InteractionState.Finished;
        } else {
            (context.getState()).state = InteractionState.Failed;
        }
    }

    @Nonnull
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }
}