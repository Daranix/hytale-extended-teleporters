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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hytale.extendedteleport.TeleporterManager;
import com.hytale.extendedteleport.data.TeleporterInfo;
import java.util.ArrayList;
import java.util.List;

public class TeleporterSettingsGui extends InteractiveCustomUIPage<TeleporterSettingsData> {
    private static final String UI_PATH = "Common/UI/Custom/Pages/ExtendedTeleport_TeleporterSettings";

    public TeleporterSettingsGui(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CantClose, TeleporterSettingsData.CODEC);
    }

    public void build(Ref<EntityStore> ref, UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Store<EntityStore> store) {
        commandBuilder.append(UI_PATH);

        commandBuilder.set("#TeleporterSelectionSection", true);

        TeleporterManager manager = TeleporterManager.getInstance();
        List<TeleporterInfo> teleporters = new ArrayList<>(manager.getPlayerTeleporters(this.playerRef.getUuid()));
        List<String> teleporterNames = teleporters.stream().map(TeleporterInfo::displayName).toList();
        commandBuilder.set("#TeleporterDropdown", teleporterNames);

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveChangesButton", EventData.of("action", "save"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ManageTrustButton", EventData.of("action", "manageTrust"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("action", "close"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TeleporterDropdown", EventData.of("action", "selectTeleporter"));

        if (!teleporterNames.isEmpty()) {
            commandBuilder.set("#TeleporterDropdown", teleporterNames.getFirst());
        }
    }

    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, TeleporterSettingsData data) {
        TeleporterManager manager = TeleporterManager.getInstance();

        if (data.save != null) {
            TeleporterInfo info = findTeleporter(manager, data.selectedTeleporter);
            if (info != null) {
                applySettings(info, data);
                manager.markDirty();
            }
            close();
        } else if (data.manageTrust != null) {
            TeleporterInfo info = findTeleporter(manager, data.selectedTeleporter);
            if (info != null) {
                Player player = (Player) store.getComponent(ref, Player.getComponentType());
                player.getPageManager().openCustomPage(ref, store, new SubownerManagementGui(this.playerRef, info));
            }
            close();
        } else if (data.cancel != null) {
            close();
        } else if (data.selectedTeleporter != null) {
            TeleporterInfo info = findTeleporter(manager, data.selectedTeleporter);
            if (info != null) {
                UICommandBuilder update = new UICommandBuilder();
                updateTeleporterFields(update, info);
                sendUpdate(update);
            }
        }
    }

    private TeleporterInfo findTeleporter(TeleporterManager manager, String displayName) {
        if (displayName == null) return null;
        return manager.getPlayerTeleporters(this.playerRef.getUuid()).stream()
        .filter(t -> t.displayName().equals(displayName))
        .findFirst().orElse(null);
    }

    private void applySettings(TeleporterInfo info, TeleporterSettingsData data) {
        if (data.warpName != null && !data.warpName.isEmpty()) {
            TeleporterManager.getInstance().updateTeleporterComponentWarpName(info, data.warpName);
        }
        if (data.warpDestination != null) {
            TeleporterManager.getInstance().updateTeleporterWarpDestination(info, data.warpDestination);
        }
        if (data.partyOnlySetting != null) info.setPartyOnly(data.partyOnlySetting);
        if (data.restrictedSetting != null) info.setRestricted(data.restrictedSetting);
        if (data.interactionLockedSetting != null) info.setInteractionLocked(data.interactionLockedSetting);
        if (data.breakLockedSetting != null) info.setBreakLocked(data.breakLockedSetting);
        if (data.displayWorldSetting != null) info.setDisplayWorld(data.displayWorldSetting);
        if (data.displayCoordinatesSetting != null) info.setDisplayCoordinates(data.displayCoordinatesSetting);
        if (data.hideMapWaypointSetting != null) info.setHideMapWaypoint(data.hideMapWaypointSetting);
        if (data.singleUseSetting != null) info.setSingleUse(data.singleUseSetting);
        if (data.allowPublicDestinationChangeSetting != null) info.setAllowPublicDestinationChange(data.allowPublicDestinationChangeSetting);
        if (data.selfDestructSetting != null && data.selfDestructSetting && !info.isSelfDestruct()) {
            info.activateSelfDestruct();
        }
    }

    private void updateTeleporterFields(UICommandBuilder commandBuilder, TeleporterInfo info) {
        commandBuilder.set("#WarpNameField", info.warpName() != null ? info.warpName() : "");
        commandBuilder.set("#WarpDestinationDropdown", info.warpDestination() != null ? info.warpDestination() : "");
        commandBuilder.set("#PartyOnlySetting", info.isPartyOnly());
        commandBuilder.set("#RestrictedSetting", info.isRestricted());
        commandBuilder.set("#InteractionLockedSetting", info.isInteractionLocked());
        commandBuilder.set("#BreakLockedSetting", info.isBreakLocked());
        commandBuilder.set("#DisplayWorldSetting", info.displayWorld());
        commandBuilder.set("#DisplayCoordinatesSetting", info.displayCoordinates());
        commandBuilder.set("#HideMapWaypointSetting", info.hideMapWaypoint());
        commandBuilder.set("#SelfDestructSetting", info.isSelfDestruct());
        commandBuilder.set("#SingleUseSetting", info.isSingleUse());
        commandBuilder.set("#AllowPublicDestinationChangeSetting", info.allowPublicDestinationChange());
        String location = info.dimension() + " (" + info.blockX() + ", " + info.blockY() + ", " + info.blockZ() + ")";
        commandBuilder.set("#LocationLabel", location);
    }
}