package com.hytale.extendedteleport;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.lookup.StringCodecMapCodec;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.util.Config;
import com.hytale.extendedteleport.commands.TeleporterCommand;
import com.hytale.extendedteleport.config.ExtendedTeleportConfig;
import com.hytale.extendedteleport.gui.TeleporterSettingsPageSupplier;
import com.hytale.extendedteleport.i18n.Translations;
import com.hytale.extendedteleport.interaction.ExtendedTeleporterInteraction;
import com.hytale.extendedteleport.interaction.UnlimitedPlacementConditionInteraction;
import com.hytale.extendedteleport.system.TeleporterBreakBlockEventSystem;
import com.hytale.extendedteleport.system.TeleporterComponentRemovalSystem;
import com.hytale.extendedteleport.system.TeleporterPlaceBlockEventSystem;
import com.hytale.extendedteleport.system.TeleporterRestrictionTickingSystem;
import com.hytale.extendedteleport.system.TeleporterSelfDestructTickingSystem;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;


public final class Main
extends JavaPlugin
{
    private static Main instance;
    public static Config<ExtendedTeleportConfig> CONFIG;

    public Main(@NonNullDecl JavaPluginInit init) {
        super(init);
        instance = this;
        CONFIG = withConfig("ExtendedTeleportHistory", ExtendedTeleportConfig.CODEC);
    }

    public static Main get() {
        return instance;
    }


    protected void setup() {
        super.setup();

        getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Initializing...");


        Translations.init();
        getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Translation system initialized");


        CONFIG.save();


        TeleporterManager manager = TeleporterManager.getInstance();


        TeleporterRestrictionTickingSystem restrictionSystem = new TeleporterRestrictionTickingSystem();
        getEntityStoreRegistry().registerSystem((ISystem)restrictionSystem);


        manager.setRestrictionSystem(restrictionSystem);


        getEntityStoreRegistry().registerSystem((ISystem)new TeleporterPlaceBlockEventSystem());
        getEntityStoreRegistry().registerSystem((ISystem)new TeleporterBreakBlockEventSystem());


        getEntityStoreRegistry().registerSystem((ISystem)new TeleporterSelfDestructTickingSystem());


        getChunkStoreRegistry().registerSystem((ISystem)new TeleporterComponentRemovalSystem());


        getCommandRegistry().registerCommand((AbstractCommand)new TeleporterCommand());


        getCodecRegistry(Interaction.CODEC).register("PlacementCountCondition", UnlimitedPlacementConditionInteraction.class, UnlimitedPlacementConditionInteraction.CODEC);


        getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Overrode PlacementCountCondition (unlimited teleporter placement)");


        getCodecRegistry(Interaction.CODEC).register("Teleporter", ExtendedTeleporterInteraction.class, ExtendedTeleporterInteraction.CODEC);


        getCodecRegistry((StringCodecMapCodec)OpenCustomUIInteraction.PAGE_CODEC).register("Teleporter", TeleporterSettingsPageSupplier.class, (Codec)TeleporterSettingsPageSupplier.CODEC);


        getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Overrode native teleporter interactions");


        getEventRegistry().registerGlobal(AddWorldEvent.class, event -> {
            World world = event.getWorld();

            manager.addWorld(world);

            getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Applied to world: " + world.getName());
        });
        getEventRegistry().registerGlobal(RemoveWorldEvent.class, event -> manager.removeWorld(event.getWorld().getName()));


        getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, event -> {
            manager.initializePermissionProvider();

            manager.ensureAllTeleporterWarpsExist();
            manager.syncPrivateWarpsWithRegistry();
            manager.registerAllCustomDestinationsAsWarps();
            getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Permission provider: " + manager.getPermissionProvider().getName());
            getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Synced private warps with game registry");
        });
        getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Teleporter block placement limit: " + manager.getNewLimit());
        getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Features: Private warps, Proximity-based restrictions");
        getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Successfully initialized!");
    }


    protected void shutdown() {
        TeleporterManager.getInstance().shutdown();
        getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Shutdown complete");
    }
}