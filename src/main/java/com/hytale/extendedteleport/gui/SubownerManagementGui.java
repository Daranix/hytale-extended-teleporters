package com.hytale.extendedteleport.gui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hytale.extendedteleport.data.TeleporterInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubownerManagementGui extends InteractiveCustomUIPage<SubownerEventData> {
    private static final String UI_PATH = "Common/UI/Custom/Pages/ExtendedTeleport_SubownerManagement";

    private final TeleporterInfo teleporterInfo;

    public SubownerManagementGui(PlayerRef playerRef, TeleporterInfo teleporterInfo) {
        super(playerRef, CustomPageLifetime.CantClose, SubownerEventData.CODEC);
        this.teleporterInfo = teleporterInfo;
    }

    public void build(Ref<EntityStore> ref, UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, Store<EntityStore> store) {
        commandBuilder.append(UI_PATH);

        commandBuilder.set("#TeleporterName", this.teleporterInfo.displayName());

        String location = this.teleporterInfo.dimension() + " (" + this.teleporterInfo.blockX() + ", " + this.teleporterInfo.blockY() + ", " + this.teleporterInfo.blockZ() + ")";
        commandBuilder.set("#TeleporterLocation", location);

        List<String> onlinePlayerNames = new ArrayList<>();
        Universe.get().getPlayers().forEach(p -> onlinePlayerNames.add(p.getUsername()));
        commandBuilder.set("#OnlinePlayersDropdown", onlinePlayerNames);

        int count = 0;
        for (UUID trustedUuid : this.teleporterInfo.getTrustedPlayers()) {
            if (count >= 10) break;
            String entryId = "#Entry" + count;
            String nameId = "#Name" + count;
            String removeId = "#Remove" + count;

            String playerName = trustedUuid.toString().substring(0, 8) + "...";
            for (PlayerRef p : Universe.get().getPlayers()) {
                if (p.getUuid().equals(trustedUuid)) {
                    playerName = p.getUsername();
                    break;
                }
            }

            commandBuilder.set(entryId, true);
            commandBuilder.set(nameId, playerName);

            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, removeId, EventData.of("action", "remove"));

            count++;
        }

        if (count == 0) {
            commandBuilder.set("#EmptyListLabel", true);
        }

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#AddButton", EventData.of("action", "addPlayer"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton", EventData.of("action", "save"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton", EventData.of("action", "cancel"));

        eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#OnlinePlayersDropdown", EventData.of("action", "playerSelected"));
    }

    public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, SubownerEventData data) {
        if (data.addPlayer != null) {
            String selectedUsername = data.selectedPlayer;
            if (selectedUsername != null && !selectedUsername.isEmpty()) {
                for (PlayerRef p : Universe.get().getPlayers()) {
                    if (p.getUsername().equals(selectedUsername)) {
                        this.teleporterInfo.addTrustedPlayer(p.getUuid());
                        break;
                    }
                }
            }
            rebuild();
        } else if (data.save != null) {
            close();
        } else if (data.cancel != null) {
            close();
        } else {
            int removeIndex = findRemoveIndex(data);
            if (removeIndex >= 0) {
                List<UUID> trustedList = new ArrayList<>(this.teleporterInfo.getTrustedPlayers());
                if (removeIndex < trustedList.size()) {
                    this.teleporterInfo.removeTrustedPlayer(trustedList.get(removeIndex));
                }
                rebuild();
            }
        }
    }

    private int findRemoveIndex(SubownerEventData data) {
        if (data.remove0 != null) return 0;
        if (data.remove1 != null) return 1;
        if (data.remove2 != null) return 2;
        if (data.remove3 != null) return 3;
        if (data.remove4 != null) return 4;
        if (data.remove5 != null) return 5;
        if (data.remove6 != null) return 6;
        if (data.remove7 != null) return 7;
        if (data.remove8 != null) return 8;
        if (data.remove9 != null) return 9;
        return -1;
    }
}