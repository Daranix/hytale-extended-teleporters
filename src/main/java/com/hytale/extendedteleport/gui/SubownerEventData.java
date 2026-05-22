package com.hytale.extendedteleport.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public final class SubownerEventData
{
    public static final BuilderCodec<SubownerEventData> CODEC;
    public String selectedPlayer;
    public String addPlayer;
    public String remove0;
    public String remove1;
    public String remove2;
    public String remove3;
    public String remove4;
    public String remove5;
    public String remove6;
    public String remove7;
    public String remove8;
    public String remove9;
    public Boolean perm0Rename;
    public Boolean perm0Dest;
    public Boolean perm0Set;
    public Boolean perm0Brk;
    public Boolean perm1Rename;
    public Boolean perm1Dest;
    public Boolean perm1Set;
    public Boolean perm1Brk;
    public Boolean perm2Rename;
    public Boolean perm2Dest;
    public Boolean perm2Set;
    public Boolean perm2Brk;
    public Boolean perm3Rename;
    public Boolean perm3Dest;
    public Boolean perm3Set;
    public Boolean perm3Brk;
    public Boolean perm4Rename;
    public Boolean perm4Dest;
    public Boolean perm4Set;
    public Boolean perm4Brk;
    public Boolean perm5Rename;
    public Boolean perm5Dest;
    public Boolean perm5Set;
    public Boolean perm5Brk;
    public Boolean perm6Rename;
    public Boolean perm6Dest;
    public Boolean perm6Set;
    public Boolean perm6Brk;
    public Boolean perm7Rename;
    public Boolean perm7Dest;
    public Boolean perm7Set;
    public Boolean perm7Brk;
    public Boolean perm8Rename;
    public Boolean perm8Dest;
    public Boolean perm8Set;
    public Boolean perm8Brk;
    public Boolean perm9Rename;
    public Boolean perm9Dest;
    public Boolean perm9Set;
    public Boolean perm9Brk;
    public String save;
    public String cancel;

    static {
        BuilderCodec.Builder<SubownerEventData> builder = BuilderCodec.builder(SubownerEventData.class, SubownerEventData::new);
        builder
        .addField(new KeyedCodec<>("@SelectedPlayer", Codec.STRING), (SubownerEventData d, String s) -> d.selectedPlayer = s, (SubownerEventData d) -> d.selectedPlayer)
        .addField(new KeyedCodec<>("AddPlayer", Codec.STRING), (SubownerEventData d, String s) -> d.addPlayer = s, (SubownerEventData d) -> d.addPlayer)
        .addField(new KeyedCodec<>("Remove0", Codec.STRING), (SubownerEventData d, String s) -> d.remove0 = s, (SubownerEventData d) -> d.remove0)
        .addField(new KeyedCodec<>("Remove1", Codec.STRING), (SubownerEventData d, String s) -> d.remove1 = s, (SubownerEventData d) -> d.remove1)
        .addField(new KeyedCodec<>("Remove2", Codec.STRING), (SubownerEventData d, String s) -> d.remove2 = s, (SubownerEventData d) -> d.remove2)
        .addField(new KeyedCodec<>("Remove3", Codec.STRING), (SubownerEventData d, String s) -> d.remove3 = s, (SubownerEventData d) -> d.remove3)
        .addField(new KeyedCodec<>("Remove4", Codec.STRING), (SubownerEventData d, String s) -> d.remove4 = s, (SubownerEventData d) -> d.remove4)
        .addField(new KeyedCodec<>("Remove5", Codec.STRING), (SubownerEventData d, String s) -> d.remove5 = s, (SubownerEventData d) -> d.remove5)
        .addField(new KeyedCodec<>("Remove6", Codec.STRING), (SubownerEventData d, String s) -> d.remove6 = s, (SubownerEventData d) -> d.remove6)
        .addField(new KeyedCodec<>("Remove7", Codec.STRING), (SubownerEventData d, String s) -> d.remove7 = s, (SubownerEventData d) -> d.remove7)
        .addField(new KeyedCodec<>("Remove8", Codec.STRING), (SubownerEventData d, String s) -> d.remove8 = s, (SubownerEventData d) -> d.remove8)
        .addField(new KeyedCodec<>("Remove9", Codec.STRING), (SubownerEventData d, String s) -> d.remove9 = s, (SubownerEventData d) -> d.remove9)
        .addField(new KeyedCodec<>("Perm0Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm0Rename = v, (SubownerEventData d) -> d.perm0Rename)
        .addField(new KeyedCodec<>("Perm0Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm0Dest = v, (SubownerEventData d) -> d.perm0Dest)
        .addField(new KeyedCodec<>("Perm0Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm0Set = v, (SubownerEventData d) -> d.perm0Set)
        .addField(new KeyedCodec<>("Perm0Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm0Brk = v, (SubownerEventData d) -> d.perm0Brk)
        .addField(new KeyedCodec<>("Perm1Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm1Rename = v, (SubownerEventData d) -> d.perm1Rename)
        .addField(new KeyedCodec<>("Perm1Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm1Dest = v, (SubownerEventData d) -> d.perm1Dest)
        .addField(new KeyedCodec<>("Perm1Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm1Set = v, (SubownerEventData d) -> d.perm1Set)
        .addField(new KeyedCodec<>("Perm1Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm1Brk = v, (SubownerEventData d) -> d.perm1Brk)
        .addField(new KeyedCodec<>("Perm2Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm2Rename = v, (SubownerEventData d) -> d.perm2Rename)
        .addField(new KeyedCodec<>("Perm2Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm2Dest = v, (SubownerEventData d) -> d.perm2Dest)
        .addField(new KeyedCodec<>("Perm2Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm2Set = v, (SubownerEventData d) -> d.perm2Set)
        .addField(new KeyedCodec<>("Perm2Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm2Brk = v, (SubownerEventData d) -> d.perm2Brk)
        .addField(new KeyedCodec<>("Perm3Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm3Rename = v, (SubownerEventData d) -> d.perm3Rename)
        .addField(new KeyedCodec<>("Perm3Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm3Dest = v, (SubownerEventData d) -> d.perm3Dest)
        .addField(new KeyedCodec<>("Perm3Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm3Set = v, (SubownerEventData d) -> d.perm3Set)
        .addField(new KeyedCodec<>("Perm3Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm3Brk = v, (SubownerEventData d) -> d.perm3Brk)
        .addField(new KeyedCodec<>("Perm4Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm4Rename = v, (SubownerEventData d) -> d.perm4Rename)
        .addField(new KeyedCodec<>("Perm4Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm4Dest = v, (SubownerEventData d) -> d.perm4Dest)
        .addField(new KeyedCodec<>("Perm4Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm4Set = v, (SubownerEventData d) -> d.perm4Set)
        .addField(new KeyedCodec<>("Perm4Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm4Brk = v, (SubownerEventData d) -> d.perm4Brk)
        .addField(new KeyedCodec<>("Perm5Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm5Rename = v, (SubownerEventData d) -> d.perm5Rename)
        .addField(new KeyedCodec<>("Perm5Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm5Dest = v, (SubownerEventData d) -> d.perm5Dest)
        .addField(new KeyedCodec<>("Perm5Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm5Set = v, (SubownerEventData d) -> d.perm5Set)
        .addField(new KeyedCodec<>("Perm5Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm5Brk = v, (SubownerEventData d) -> d.perm5Brk)
        .addField(new KeyedCodec<>("Perm6Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm6Rename = v, (SubownerEventData d) -> d.perm6Rename)
        .addField(new KeyedCodec<>("Perm6Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm6Dest = v, (SubownerEventData d) -> d.perm6Dest)
        .addField(new KeyedCodec<>("Perm6Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm6Set = v, (SubownerEventData d) -> d.perm6Set)
        .addField(new KeyedCodec<>("Perm6Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm6Brk = v, (SubownerEventData d) -> d.perm6Brk)
        .addField(new KeyedCodec<>("Perm7Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm7Rename = v, (SubownerEventData d) -> d.perm7Rename)
        .addField(new KeyedCodec<>("Perm7Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm7Dest = v, (SubownerEventData d) -> d.perm7Dest)
        .addField(new KeyedCodec<>("Perm7Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm7Set = v, (SubownerEventData d) -> d.perm7Set)
        .addField(new KeyedCodec<>("Perm7Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm7Brk = v, (SubownerEventData d) -> d.perm7Brk)
        .addField(new KeyedCodec<>("Perm8Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm8Rename = v, (SubownerEventData d) -> d.perm8Rename)
        .addField(new KeyedCodec<>("Perm8Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm8Dest = v, (SubownerEventData d) -> d.perm8Dest)
        .addField(new KeyedCodec<>("Perm8Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm8Set = v, (SubownerEventData d) -> d.perm8Set)
        .addField(new KeyedCodec<>("Perm8Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm8Brk = v, (SubownerEventData d) -> d.perm8Brk)
        .addField(new KeyedCodec<>("Perm9Rename", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm9Rename = v, (SubownerEventData d) -> d.perm9Rename)
        .addField(new KeyedCodec<>("Perm9Dest", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm9Dest = v, (SubownerEventData d) -> d.perm9Dest)
        .addField(new KeyedCodec<>("Perm9Set", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm9Set = v, (SubownerEventData d) -> d.perm9Set)
        .addField(new KeyedCodec<>("Perm9Brk", Codec.BOOLEAN), (SubownerEventData d, Boolean v) -> d.perm9Brk = v, (SubownerEventData d) -> d.perm9Brk)
        .addField(new KeyedCodec<>("Save", Codec.STRING), (SubownerEventData d, String s) -> d.save = s, (SubownerEventData d) -> d.save)
        .addField(new KeyedCodec<>("Cancel", Codec.STRING), (SubownerEventData d, String s) -> d.cancel = s, (SubownerEventData d) -> d.cancel);
        CODEC = builder.build();
    }
}