package com.hytale.extendedteleport;

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

public final class Main extends JavaPlugin {
   private static Main instance;
   public static Config<ExtendedTeleportConfig> CONFIG;

   public Main(@NonNullDecl JavaPluginInit init) {
      super(init);
      instance = this;
      CONFIG = this.withConfig("ExtendedTeleportHistory", ExtendedTeleportConfig.CODEC);
   }

   public static Main get() {
      return instance;
   }

   protected void setup() {
      super.setup();
      this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Initializing...");
      Translations.init();
      this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Translation system initialized");
      CONFIG.save();
      TeleporterManager manager = TeleporterManager.getInstance();
      TeleporterRestrictionTickingSystem restrictionSystem = new TeleporterRestrictionTickingSystem();
      this.getEntityStoreRegistry().registerSystem(restrictionSystem);
      manager.setRestrictionSystem(restrictionSystem);
      this.getEntityStoreRegistry().registerSystem(new TeleporterPlaceBlockEventSystem());
      this.getEntityStoreRegistry().registerSystem(new TeleporterBreakBlockEventSystem());
      this.getEntityStoreRegistry().registerSystem(new TeleporterSelfDestructTickingSystem());
      // TODO: ChunkStore system registration requires Teleporter component type to be initialized first.
      //       Registering via getChunkStoreRegistry() fails because the query from Teleporter.getComponentType()
      //       is not yet available during setup. For now, block removal cleanup is handled by other mechanisms.
      // this.getChunkStoreRegistry().registerSystem(new TeleporterComponentRemovalSystem());
      this.getCommandRegistry().registerCommand(new TeleporterCommand());
      this.getCodecRegistry(Interaction.CODEC)
         .register("PlacementCountCondition", UnlimitedPlacementConditionInteraction.class, UnlimitedPlacementConditionInteraction.CODEC);
      this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Overrode PlacementCountCondition (unlimited teleporter placement)");
      this.getCodecRegistry(Interaction.CODEC).register("Teleporter", ExtendedTeleporterInteraction.class, ExtendedTeleporterInteraction.CODEC);
      this.getCodecRegistry(OpenCustomUIInteraction.PAGE_CODEC)
         .register("Teleporter", TeleporterSettingsPageSupplier.class, TeleporterSettingsPageSupplier.CODEC);
      this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Overrode native teleporter interactions");
      this.getEventRegistry().registerGlobal(AddWorldEvent.class, event -> {
         World world = event.getWorld();
         manager.addWorld(world);
         this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Applied to world: " + world.getName());
      });
      this.getEventRegistry().registerGlobal(RemoveWorldEvent.class, event -> manager.removeWorld(event.getWorld().getName()));
      this.getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, event -> {
         manager.initializePermissionProvider();
         manager.ensureAllTeleporterWarpsExist();
         manager.syncPrivateWarpsWithRegistry();
         manager.registerAllCustomDestinationsAsWarps();
         this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Permission provider: " + manager.getPermissionProvider().getName());
         this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Synced private warps with game registry");
      });
      this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Teleporter block placement limit: " + manager.getNewLimit());
      this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Features: Private warps, Proximity-based restrictions");
      this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Successfully initialized!");
   }

   protected void shutdown() {
      TeleporterManager.getInstance().shutdown();
      this.getLogger().at(Level.INFO).log("ExtendedTeleportHistory - Shutdown complete");
   }
}
