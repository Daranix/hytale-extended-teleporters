package com.hytale.extendedteleport.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class PageEventData
{
    public static final BuilderCodec<PageEventData> CODEC;
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
        BuilderCodec.Builder<PageEventData> builder = BuilderCodec.builder(PageEventData.class, PageEventData::new);
        builder
        .addField(new KeyedCodec<>("@WarpName", Codec.STRING), (PageEventData d, String s) -> d.warpName = s, (PageEventData d) -> d.warpName)
        .addField(new KeyedCodec<>("@WarpDestination", Codec.STRING), (PageEventData d, String s) -> d.warpDestination = s, (PageEventData d) -> d.warpDestination)
        .addField(new KeyedCodec<>("@ShowOnlyMyWarpsFilter", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.showOnlyMyWarpsFilter = s, (PageEventData d) -> d.showOnlyMyWarpsFilter)
        .addField(new KeyedCodec<>("@VisibilitySetting", Codec.STRING), (PageEventData d, String s) -> d.visibilitySetting = s, (PageEventData d) -> d.visibilitySetting)
        .addField(new KeyedCodec<>("@PartyOnlySetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.partyOnlySetting = s, (PageEventData d) -> d.partyOnlySetting)
        .addField(new KeyedCodec<>("@RestrictedSetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.restrictedSetting = s, (PageEventData d) -> d.restrictedSetting)
        .addField(new KeyedCodec<>("@InteractionLockedSetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.interactionLockedSetting = s, (PageEventData d) -> d.interactionLockedSetting)
        .addField(new KeyedCodec<>("@BreakLockedSetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.breakLockedSetting = s, (PageEventData d) -> d.breakLockedSetting)
        .addField(new KeyedCodec<>("@DisplayWorldSetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.displayWorldSetting = s, (PageEventData d) -> d.displayWorldSetting)
        .addField(new KeyedCodec<>("@DisplayCoordinatesSetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.displayCoordinatesSetting = s, (PageEventData d) -> d.displayCoordinatesSetting)
        .addField(new KeyedCodec<>("@SelfDestructSetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.selfDestructSetting = s, (PageEventData d) -> d.selfDestructSetting)
        .addField(new KeyedCodec<>("@SingleUseSetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.singleUseSetting = s, (PageEventData d) -> d.singleUseSetting)
        .addField(new KeyedCodec<>("@HideMapWaypointSetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.hideMapWaypointSetting = s, (PageEventData d) -> d.hideMapWaypointSetting)
        .addField(new KeyedCodec<>("@AllowPublicDestinationChangeSetting", Codec.BOOLEAN), (PageEventData d, Boolean s) -> d.allowPublicDestinationChangeSetting = s, (PageEventData d) -> d.allowPublicDestinationChangeSetting)
        .addField(new KeyedCodec<>("@ManageTrust", Codec.STRING), (PageEventData d, String s) -> d.manageTrust = s, (PageEventData d) -> d.manageTrust)
        .addField(new KeyedCodec<>("@Save", Codec.STRING), (PageEventData d, String s) -> d.save = s, (PageEventData d) -> d.save)
        .addField(new KeyedCodec<>("@Cancel", Codec.STRING), (PageEventData d, String s) -> d.cancel = s, (PageEventData d) -> d.cancel);
        CODEC = builder.build();
    }
}