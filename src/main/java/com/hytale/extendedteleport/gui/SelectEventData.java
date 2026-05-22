package com.hytale.extendedteleport.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class SelectEventData
{
    public static final BuilderCodec<SelectEventData> CODEC;
    public String selectedTeleporter;
    public String manage;
    public String cancel;

    static {
        BuilderCodec.Builder<SelectEventData> builder = BuilderCodec.builder(SelectEventData.class, SelectEventData::new);
        builder
        .addField(new KeyedCodec<>("@SelectedTeleporter", Codec.STRING), (SelectEventData d, String s) -> d.selectedTeleporter = s, (SelectEventData d) -> d.selectedTeleporter)
        .addField(new KeyedCodec<>("Manage", Codec.STRING), (SelectEventData d, String s) -> d.manage = s, (SelectEventData d) -> d.manage)
        .addField(new KeyedCodec<>("Cancel", Codec.STRING), (SelectEventData d, String s) -> d.cancel = s, (SelectEventData d) -> d.cancel);
        CODEC = builder.build();
    }
}