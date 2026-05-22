package com.hytale.extendedteleport.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class TeleporterSettingsData
{
    public static final BuilderCodec<TeleporterSettingsData> CODEC;
    public String selectedTeleporter;
    public String warpName;
    public String warpDestination;
    public Boolean showOnlyMyWarpsFilter;
    public String visibilitySetting;
    public Boolean partyOnlySetting;
    public Boolean restrictedSetting;
    public Boolean interactionLockedSetting;
    public Boolean breakLockedSetting;
    public Boolean displayWorldSetting;
    public Boolean displayCoordinatesSetting;
    public Boolean selfDestructSetting;
    public Boolean singleUseSetting;
    public Boolean hideMapWaypointSetting;
    public Boolean allowPublicDestinationChangeSetting;
    public String manageTrust;
    public String save;
    public String cancel;

    static {
        BuilderCodec.Builder<TeleporterSettingsData> builder = BuilderCodec.builder(TeleporterSettingsData.class, TeleporterSettingsData::new);
        builder
        .addField(new KeyedCodec<>("@SelectedTeleporter", Codec.STRING), (TeleporterSettingsData d, String s) -> d.selectedTeleporter = s, (TeleporterSettingsData d) -> d.selectedTeleporter)
        .addField(new KeyedCodec<>("@WarpName", Codec.STRING), (TeleporterSettingsData d, String s) -> d.warpName = s, (TeleporterSettingsData d) -> d.warpName)
        .addField(new KeyedCodec<>("@WarpDestination", Codec.STRING), (TeleporterSettingsData d, String s) -> d.warpDestination = s, (TeleporterSettingsData d) -> d.warpDestination)
        .addField(new KeyedCodec<>("@ShowOnlyMyWarpsFilter", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.showOnlyMyWarpsFilter = s, (TeleporterSettingsData d) -> d.showOnlyMyWarpsFilter)
        .addField(new KeyedCodec<>("@VisibilitySetting", Codec.STRING), (TeleporterSettingsData d, String s) -> d.visibilitySetting = s, (TeleporterSettingsData d) -> d.visibilitySetting)
        .addField(new KeyedCodec<>("@PartyOnlySetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.partyOnlySetting = s, (TeleporterSettingsData d) -> d.partyOnlySetting)
        .addField(new KeyedCodec<>("@RestrictedSetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.restrictedSetting = s, (TeleporterSettingsData d) -> d.restrictedSetting)
        .addField(new KeyedCodec<>("@InteractionLockedSetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.interactionLockedSetting = s, (TeleporterSettingsData d) -> d.interactionLockedSetting)
        .addField(new KeyedCodec<>("@BreakLockedSetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.breakLockedSetting = s, (TeleporterSettingsData d) -> d.breakLockedSetting)
        .addField(new KeyedCodec<>("@DisplayWorldSetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.displayWorldSetting = s, (TeleporterSettingsData d) -> d.displayWorldSetting)
        .addField(new KeyedCodec<>("@DisplayCoordinatesSetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.displayCoordinatesSetting = s, (TeleporterSettingsData d) -> d.displayCoordinatesSetting)
        .addField(new KeyedCodec<>("@SelfDestructSetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.selfDestructSetting = s, (TeleporterSettingsData d) -> d.selfDestructSetting)
        .addField(new KeyedCodec<>("@SingleUseSetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.singleUseSetting = s, (TeleporterSettingsData d) -> d.singleUseSetting)
        .addField(new KeyedCodec<>("@HideMapWaypointSetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.hideMapWaypointSetting = s, (TeleporterSettingsData d) -> d.hideMapWaypointSetting)
        .addField(new KeyedCodec<>("@AllowPublicDestinationChangeSetting", Codec.BOOLEAN), (TeleporterSettingsData d, Boolean s) -> d.allowPublicDestinationChangeSetting = s, (TeleporterSettingsData d) -> d.allowPublicDestinationChangeSetting)
        .addField(new KeyedCodec<>("@ManageTrust", Codec.STRING), (TeleporterSettingsData d, String s) -> d.manageTrust = s, (TeleporterSettingsData d) -> d.manageTrust)
        .addField(new KeyedCodec<>("@Save", Codec.STRING), (TeleporterSettingsData d, String s) -> d.save = s, (TeleporterSettingsData d) -> d.save)
        .addField(new KeyedCodec<>("@Cancel", Codec.STRING), (TeleporterSettingsData d, String s) -> d.cancel = s, (TeleporterSettingsData d) -> d.cancel);
        CODEC = builder.build();
    }
}