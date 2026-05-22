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

public class TeleporterSelectForSubownersGui extends InteractiveCustomUIPage<SelectEventData> {
    private static final String UI_PATH = "Common/UI/Custom/Pages/ExtendedTeleport_TeleporterSelect";

    public TeleporterSelectForSubownersGui(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CantClose, SelectEventData.CODEC);
    }

    public void build(Ref<EntityStore> ref, UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Store<EntityStore> store) {
        commandBuilder.append(UI_PATH);

        TeleporterManager manager = TeleporterManager.getInstance();
        List<TeleporterInfo> teleporters = new ArrayList<>(manager.getPlayerTeleporters(this.playerRef.getUuid()));

        if (teleporters.isEmpty()) {
            commandBuilder.set("#TeleporterDropdown", false);
            commandBuilder.set("#EmptyLabel", true);
        } else {
            List<String> teleporterNames = teleporters.stream().map(TeleporterInfo::displayName).toList();
            commandBuilder.set("#TeleporterDropdown", teleporterNames);
            commandBuilder.set("#TeleporterDropdown", teleporterNames.getFirst());
        }

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ManageButton", EventData.of("action", "manage"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton", EventData.of("action", "cancel"));
    }

    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, SelectEventData data) {
        if (data.manage != null) {
            TeleporterManager manager = TeleporterManager.getInstance();
            TeleporterInfo info = findTeleporter(manager, data.selectedTeleporter);
            if (info != null) {
                Player player = (Player) store.getComponent(ref, Player.getComponentType());
                player.getPageManager().openCustomPage(ref, store, new SubownerManagementGui(this.playerRef, info));
            }
            close();
        } else if (data.cancel != null) {
            close();
        }
    }

    private TeleporterInfo findTeleporter(TeleporterManager manager, String displayName) {
        if (displayName == null) return null;
        return manager.getPlayerTeleporters(this.playerRef.getUuid()).stream()
        .filter(t -> t.displayName().equals(displayName))
        .findFirst().orElse(null);
    }
}