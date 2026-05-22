package com.hytale.extendedteleport.gui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hytale.extendedteleport.TeleporterManager;
import com.hytale.extendedteleport.data.TeleporterInfo;

public class TeleporterBlockSettingsGui extends InteractiveCustomUIPage<PageEventData> {
    private static final String UI_PATH = "Common/UI/Custom/Pages/ExtendedTeleport_TeleporterSettings";

    private final TeleporterInfo teleporterInfo;
    private final Ref<ChunkStore> blockRef;
    private final World world;

    public TeleporterBlockSettingsGui(PlayerRef playerRef, Ref<ChunkStore> blockRef, TeleporterInfo teleporterInfo, World world, String activeState) {
        super(playerRef, CustomPageLifetime.CantClose, PageEventData.CODEC);
        this.teleporterInfo = teleporterInfo;
        this.blockRef = blockRef;
        this.world = world;
    }

    public void build(Ref<EntityStore> ref, UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Store<EntityStore> store) {
        commandBuilder.append(UI_PATH);

        commandBuilder.set("#TeleporterSelectionSection", false);

        commandBuilder.set("#WarpNameField", this.teleporterInfo.warpName() != null ? this.teleporterInfo.warpName() : "");

        commandBuilder.set("#WarpDestinationDropdown", this.teleporterInfo.warpDestination() != null ? this.teleporterInfo.warpDestination() : "");

        commandBuilder.set("#PartyOnlySetting", this.teleporterInfo.isPartyOnly());
        commandBuilder.set("#RestrictedSetting", this.teleporterInfo.isRestricted());
        commandBuilder.set("#InteractionLockedSetting", this.teleporterInfo.isInteractionLocked());
        commandBuilder.set("#BreakLockedSetting", this.teleporterInfo.isBreakLocked());
        commandBuilder.set("#DisplayWorldSetting", this.teleporterInfo.displayWorld());
        commandBuilder.set("#DisplayCoordinatesSetting", this.teleporterInfo.displayCoordinates());
        commandBuilder.set("#HideMapWaypointSetting", this.teleporterInfo.hideMapWaypoint());
        commandBuilder.set("#SelfDestructSetting", this.teleporterInfo.isSelfDestruct());
        commandBuilder.set("#SingleUseSetting", this.teleporterInfo.isSingleUse());
        commandBuilder.set("#AllowPublicDestinationChangeSetting", this.teleporterInfo.allowPublicDestinationChange());

        String location = this.teleporterInfo.dimension() + " (" + this.teleporterInfo.blockX() + ", " + this.teleporterInfo.blockY() + ", " + this.teleporterInfo.blockZ() + ")";
        commandBuilder.set("#LocationLabel", location);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveChangesButton", EventData.of("action", "save"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ManageTrustButton", EventData.of("action", "manageTrust"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("action", "close"));
    }

    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, PageEventData data) {
        if (data.save != null) {
            TeleporterManager manager = TeleporterManager.getInstance();

            if (data.warpName != null) {
                manager.updateTeleporterComponentWarpName(this.teleporterInfo, data.warpName);
            }
            if (data.warpDestination != null) {
                manager.updateTeleporterWarpDestination(this.teleporterInfo, data.warpDestination);
            }
            if (data.partyOnlySetting != null) this.teleporterInfo.setPartyOnly(data.partyOnlySetting);
            if (data.restrictedSetting != null) this.teleporterInfo.setRestricted(data.restrictedSetting);
            if (data.interactionLockedSetting != null) this.teleporterInfo.setInteractionLocked(data.interactionLockedSetting);
            if (data.breakLockedSetting != null) this.teleporterInfo.setBreakLocked(data.breakLockedSetting);
            if (data.displayWorldSetting != null) this.teleporterInfo.setDisplayWorld(data.displayWorldSetting);
            if (data.displayCoordinatesSetting != null) this.teleporterInfo.setDisplayCoordinates(data.displayCoordinatesSetting);
            if (data.hideMapWaypointSetting != null) this.teleporterInfo.setHideMapWaypoint(data.hideMapWaypointSetting);
            if (data.singleUseSetting != null) this.teleporterInfo.setSingleUse(data.singleUseSetting);
            if (data.allowPublicDestinationChangeSetting != null) this.teleporterInfo.setAllowPublicDestinationChange(data.allowPublicDestinationChangeSetting);
            if (data.selfDestructSetting != null && data.selfDestructSetting && !this.teleporterInfo.isSelfDestruct()) {
                this.teleporterInfo.activateSelfDestruct();
            }

            manager.markDirty();
            close();
        } else if (data.manageTrust != null) {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            player.getPageManager().openCustomPage(ref, store, new SubownerManagementGui(this.playerRef, this.teleporterInfo));
            close();
        } else if (data.cancel != null) {
            close();
        }
    }
}