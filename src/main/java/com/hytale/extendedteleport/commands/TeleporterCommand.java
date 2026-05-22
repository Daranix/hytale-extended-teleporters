package com.hytale.extendedteleport.commands;

import com.hypixel.hytale.builtin.teleport.TeleportPlugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hytale.extendedteleport.TeleporterManager;
import com.hytale.extendedteleport.data.CustomDestination;
import com.hytale.extendedteleport.data.TeleporterInfo;
import com.hytale.extendedteleport.gui.SubownerManagementGui;
import com.hytale.extendedteleport.gui.TeleporterSelectForSubownersGui;
import com.hytale.extendedteleport.gui.TeleporterSettingsGui;
import com.hytale.extendedteleport.i18n.Translations;
import com.hytale.extendedteleport.util.TeleporterLookup;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;


public final class TeleporterCommand
extends AbstractAsyncCommand
{
    private static final HytaleLogger logger = HytaleLogger.getLogger().getSubLogger("ExtendedTeleport-Commands");

    private static final int ITEMS_PER_PAGE = 8;

    private static final double TELEPORT_HEIGHT_OFFSET = 6.0D;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    private static final Color COLOR_HEADER = new Color(85, 255, 85);
    private static final Color COLOR_PLAYER = new Color(255, 255, 85);
    private static final Color COLOR_NAME = new Color(255, 255, 255);
    private static final Color COLOR_COORDS = new Color(85, 255, 255);
    private static final Color COLOR_DATE = new Color(170, 170, 170);
    private static final Color COLOR_FLAG_PRIVATE = new Color(255, 85, 85);
    private static final Color COLOR_FLAG_RESTRICTED = new Color(255, 170, 0);
    private static final Color COLOR_FLAG_LOCKED = new Color(170, 85, 255);
    private static final Color COLOR_FLAG_SELF_DESTRUCT = new Color(255, 85, 255);
    private static final Color COLOR_PAGE_INFO = new Color(170, 170, 170);

    public TeleporterCommand() {
        super("teleporter", "Manage teleporter settings");
        addAliases(new String[] { "tp-settings", "tpsettings" });
        setPermissionGroup(GameMode.Adventure);
        addSubCommand((AbstractCommand)new ListTeleportersSubCommand());
        addSubCommand((AbstractCommand)new TrustGuiSubCommand());
        addSubCommand((AbstractCommand)new TrustSubCommand());
        addSubCommand((AbstractCommand)new UntrustSubCommand());
        addSubCommand((AbstractCommand)new TrustListSubCommand());
        addSubCommand((AbstractCommand)new BypassSubCommand());
        addSubCommand((AbstractCommand)new GoSubCommand());
        addSubCommand((AbstractCommand)new DestinationSubCommand());
        addSubCommand((AbstractCommand)new ServerSubCommand());
        addSubCommand((AbstractCommand)new ReloadSubCommand());
    }

    @NonNullDecl
    protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
        Player player;
        CommandSender sender = ctx.sender();

        if (sender instanceof Player) { player = (Player)sender; }
        else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
        return CompletableFuture.completedFuture(null); }


        Ref<EntityStore> ref = player.getReference();
        if (ref == null || !ref.isValid()) {
            ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore)store.getExternalData()).getWorld();

        return CompletableFuture.runAsync(() -> { PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType()); if (playerRef == null) return;  List<TeleporterInfo> playerTeleporters = TeleporterManager.getInstance().getPlayerTeleportersSynced(playerRef.getUuid()); if (playerTeleporters.isEmpty()) { ctx.sendMessage(Translations.msgWarning("cmd.list.empty", new Object[0])); return; }  player.getPageManager().openCustomPage(ref, store, (CustomUIPage)new TeleporterSettingsGui(playerRef)); }, (Executor)world);
    }


    private static Map<UUID, String> getOnlinePlayerNames() {
        Map<UUID, String> names = new ConcurrentHashMap<>();
        try {
            for (PlayerRef playerRef : Universe.get().getPlayers()) {
                names.put(playerRef.getUuid(), playerRef.getUsername());
            }
        } catch (Exception exception) {}


        return names;
    }


    private static String getPlayerName(UUID uuid, Map<UUID, String> onlineNames) {
        if (uuid == null) return Translations.tr("common.player.unknown", new Object[0]);
        String name = onlineNames.get(uuid);
        if (name != null) return name;

        return Translations.tr("common.player.uuidShort", new Object[] { "uuid", uuid.toString().substring(0, 8) });
    }


    private static final class ListTeleportersSubCommand
    extends AbstractAsyncCommand
    {
        private final OptionalArg<Integer> pageArg = withOptionalArg("page", "Page number", (ArgumentType)ArgTypes.INTEGER);

        ListTeleportersSubCommand() {
            super("list", "List your teleporters in chat (use /teleporter list <page> for pagination)");
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();


            int page = this.pageArg.provided(ctx) ? ((Integer)this.pageArg.get(ctx)).intValue() : 1;
            if (page < 1) page = 1;
            int requestedPage = page;

            return CompletableFuture.runAsync(() -> { List<TeleporterInfo> teleporters; String headerTitle; PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType()); if (playerRef == null) return;  UUID playerUuid = playerRef.getUuid(); TeleporterManager manager = TeleporterManager.getInstance(); boolean isBypassing = manager.isInBypassMode(playerUuid); if (isBypassing) { teleporters = new ArrayList<>(manager.getAllTeleporters()); headerTitle = Translations.tr("cmd.list.header.all", new Object[0]); } else { teleporters = new ArrayList<>(manager.getPlayerTeleportersSynced(playerUuid)); headerTitle = Translations.tr("cmd.list.header.yours", new Object[0]); }  if (teleporters.isEmpty()) { ctx.sendMessage(Translations.msgWarning("cmd.list.empty", new Object[0])); return; }  teleporters.sort((a, b) -> a.displayName().compareToIgnoreCase(b.displayName())); int totalItems = teleporters.size(); int totalPages = (int)Math.ceil(totalItems / 8.0D); int currentPage = Math.min(requestedPage, totalPages); int startIndex = (currentPage - 1) * 8; int endIndex = Math.min(startIndex + 8, totalItems); Map<UUID, String> onlineNames = isBypassing ? TeleporterCommand.getOnlinePlayerNames() : Collections.<UUID, String>emptyMap(); ctx.sendMessage(Message.raw(Translations.tr("cmd.list.header.format", new Object[] { "title", headerTitle, "total", Integer.valueOf(totalItems) })).color(TeleporterCommand.COLOR_HEADER).bold(true)); for (int i = startIndex; i < endIndex; i++) { TeleporterInfo info = teleporters.get(i); if (isBypassing) { displayAdminTeleporterInfo(ctx, info, onlineNames); } else { displayPlayerTeleporterInfo(ctx, info); }  }  if (totalPages > 1) ctx.sendMessage(Message.raw(Translations.tr("cmd.list.pagination", new Object[] { "current", Integer.valueOf(currentPage), "total", Integer.valueOf(totalPages) })).color(TeleporterCommand.COLOR_PAGE_INFO));  }, (Executor)world);
        }


        private void displayAdminTeleporterInfo(CommandContext ctx, TeleporterInfo info, Map<UUID, String> onlineNames) {
            String playerName = TeleporterCommand.getPlayerName(info.ownerUuid(), onlineNames);


            String portalName = (info.warpName() != null && !info.warpName().isEmpty()) ? info.warpName() : Translations.tr("cmd.list.unnamed", new Object[0]);


            ctx.sendMessage(Message.raw(playerName + " - ").color(TeleporterCommand.COLOR_PLAYER));
            ctx.sendMessage(Message.raw("  " + portalName).color(TeleporterCommand.COLOR_NAME).bold(true));


            String coords = "  %s (%d, %d, %d)".formatted(new Object[] { info
                .dimension(),
                Integer.valueOf(info.blockX()),
                Integer.valueOf(info.blockY()),
            Integer.valueOf(info.blockZ()) });

            String date = TeleporterCommand.DATE_FORMAT.format(new Date(info.placedTimestamp()));
            ctx.sendMessage(Message.raw(coords + " | " + coords).color(TeleporterCommand.COLOR_COORDS));


            List<String> flags = new ArrayList<>();
            if (info.isPrivate()) {
                flags.add(Translations.tr("cmd.list.flag.private", new Object[0]));
            }
            if (info.isRestricted()) {
                flags.add(Translations.tr("cmd.list.flag.restricted", new Object[0]));
            }
            if (info.isInteractionLocked()) {
                flags.add(Translations.tr("cmd.list.flag.settingsLocked", new Object[0]));
            }
            if (info.isBreakLocked()) {
                flags.add(Translations.tr("cmd.list.flag.breakLocked", new Object[0]));
            }
            if (!info.displayWorld()) {
                flags.add(Translations.tr("cmd.list.flag.hiddenWorld", new Object[0]));
            }
            if (!info.displayCoordinates()) {
                flags.add(Translations.tr("cmd.list.flag.hiddenCoords", new Object[0]));
            }
            if (info.isSelfDestruct()) {
                long remainingMs = info.getSelfDestructRemainingMs();


                String timeStr = (remainingMs > 0L) ? Translations.tr("cmd.list.flag.selfDestruct.time", new Object[] { "minutes", Long.valueOf(remainingMs / 60000L), "seconds", Long.valueOf(remainingMs % 60000L / 1000L) }) : Translations.tr("cmd.list.flag.selfDestruct.expired", new Object[0]);
                flags.add(Translations.tr("cmd.list.flag.selfDestruct.format", new Object[] { "time", timeStr }));
            }
            if (info.isServerTeleporter()) {
                flags.add(Translations.tr("cmd.list.flag.server", new Object[0]));
            }
            if (info.isSingleUse()) {
                flags.add(Translations.tr("cmd.list.flag.singleUse", new Object[0]));
            }

            if (!flags.isEmpty())
            {
                for (String flag : flags) {
                    Color flagColor = getFlagColor(flag);
                    ctx.sendMessage(Message.raw("  " + flag).color(flagColor));
                }
            }
        }

        private Color getFlagColor(String flag) {
            if (flag.contains("Private")) return TeleporterCommand.COLOR_FLAG_PRIVATE;
            if (flag.contains("Restricted")) return TeleporterCommand.COLOR_FLAG_RESTRICTED;
            if (flag.contains("Locked")) return TeleporterCommand.COLOR_FLAG_LOCKED;
            if (flag.contains("SelfDestruct")) return TeleporterCommand.COLOR_FLAG_SELF_DESTRUCT;
            if (flag.contains("Server")) return new Color(0, 255, 255);
            if (flag.contains("SingleUse")) return new Color(255, 165, 0);
            return TeleporterCommand.COLOR_DATE;
        }
        private void displayPlayerTeleporterInfo(CommandContext ctx, TeleporterInfo info) {
            String coords;
            TeleporterManager manager = TeleporterManager.getInstance();


            String portalName = (info.warpName() != null && !info.warpName().isEmpty()) ? info.warpName() : Translations.tr("cmd.list.unnamed", new Object[0]);

            String destination = info.warpDestination();


            String destDisplay = (destination != null && !destination.isEmpty()) ? manager.formatWarpDisplayName(destination) : Translations.tr("cmd.list.noDestination", new Object[0]);

            ctx.sendMessage(Message.raw(portalName).color(TeleporterCommand.COLOR_NAME).bold(true));
            ctx.sendMessage(Message.raw("  -> " + destDisplay).color(TeleporterCommand.COLOR_PLAYER));


            if (info.displayWorld()) {
                if (info.displayCoordinates()) {
                    coords = "%s (%d, %d, %d)".formatted(new Object[] { info
                        .dimension(),
                        Integer.valueOf(info.blockX()),
                        Integer.valueOf(info.blockY()),
                    Integer.valueOf(info.blockZ()) });
                } else {

                    coords = info.dimension();
                }
            } else {
                coords = Translations.tr("cmd.list.locationHidden", new Object[0]);
            }
            ctx.sendMessage(Message.raw("  " + coords).color(TeleporterCommand.COLOR_COORDS));


            String date = TeleporterCommand.DATE_FORMAT.format(new Date(info.placedTimestamp()));
            ctx.sendMessage(Message.raw("  " + date).color(TeleporterCommand.COLOR_DATE));


            StringBuilder flagsLine = new StringBuilder("  ");
            boolean hasFlags = false;

            if (info.isPrivate()) {
                flagsLine.append(Translations.tr("cmd.list.flag.private", new Object[0])).append(" ");
                hasFlags = true;
            }
            if (info.isRestricted()) {
                flagsLine.append(Translations.tr("cmd.list.flag.restricted", new Object[0])).append(" ");
                hasFlags = true;
            }
            if (info.isInteractionLocked()) {
                flagsLine.append(Translations.tr("cmd.list.flag.settingsLocked", new Object[0])).append(" ");
                hasFlags = true;
            }
            if (info.isBreakLocked()) {
                flagsLine.append(Translations.tr("cmd.list.flag.breakLocked", new Object[0])).append(" ");
                hasFlags = true;
            }
            if (info.isSelfDestruct()) {
                long remainingMs = info.getSelfDestructRemainingMs();


                String timeStr = (remainingMs > 0L) ? Translations.tr("cmd.list.flag.selfDestruct.time", new Object[] { "minutes", Long.valueOf(remainingMs / 60000L), "seconds", Long.valueOf(remainingMs % 60000L / 1000L) }) : Translations.tr("cmd.list.flag.selfDestruct.expired", new Object[0]);
                flagsLine.append(Translations.tr("cmd.list.flag.selfDestruct.format", new Object[] { "time", timeStr })).append(" ");
                hasFlags = true;
            }
            if (info.isServerTeleporter()) {
                flagsLine.append(Translations.tr("cmd.list.flag.server", new Object[0])).append(" ");
                hasFlags = true;
            }
            if (info.isSingleUse()) {
                flagsLine.append(Translations.tr("cmd.list.flag.singleUse", new Object[0])).append(" ");
                hasFlags = true;
            }

            if (hasFlags) {

                Color primaryColor = TeleporterCommand.COLOR_DATE;
                if (info.isSelfDestruct()) { primaryColor = TeleporterCommand.COLOR_FLAG_SELF_DESTRUCT; }
                else if (info.isServerTeleporter()) { primaryColor = new Color(0, 255, 255); }
                else if (info.isSingleUse()) { primaryColor = new Color(255, 165, 0); }
                else if (info.isPrivate()) { primaryColor = TeleporterCommand.COLOR_FLAG_PRIVATE; }
                else if (info.isRestricted()) { primaryColor = TeleporterCommand.COLOR_FLAG_RESTRICTED; }
                else if (info.isInteractionLocked() || info.isBreakLocked()) { primaryColor = TeleporterCommand.COLOR_FLAG_LOCKED; }

                ctx.sendMessage(Message.raw(flagsLine.toString().trim()).color(primaryColor));
            }
        }
    }


    private static final class TrustGuiSubCommand
    extends AbstractAsyncCommand
    {
        TrustGuiSubCommand() {
            super("trustgui", "Open the Trust Management GUI");
            setPermissionGroup(GameMode.Adventure);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType()); if (playerRef == null) return;  UUID playerUuid = playerRef.getUuid(); TeleporterManager manager = TeleporterManager.getInstance(); List<TeleporterInfo> playerTeleporters = manager.getPlayerTeleportersSynced(playerUuid); boolean isBypassing = manager.isInBypassMode(playerUuid); if (playerTeleporters.isEmpty() && !isBypassing) { ctx.sendMessage(Translations.msgWarning("cmd.trustgui.noTeleporters", new Object[0])); return; }  if (playerTeleporters.size() == 1 && !isBypassing) { player.getPageManager().openCustomPage(ref, store, (CustomUIPage)new SubownerManagementGui(playerRef, playerTeleporters.getFirst())); } else { player.getPageManager().openCustomPage(ref, store, (CustomUIPage)new TeleporterSelectForSubownersGui(playerRef)); }  }, (Executor)world);
        }
    }


    private static final class TrustSubCommand
    extends AbstractAsyncCommand
    {
        private final RequiredArg<String> playerArg = withRequiredArg("player", "Player name to trust", (ArgumentType)ArgTypes.STRING);
        private final RequiredArg<String> teleporterArg = withRequiredArg("teleporter", "Teleporter name (must use quotes)", (ArgumentType)ArgTypes.STRING);

        TrustSubCommand() {
            super("trust", "Trust a player on your teleporter (use: trust <player> \"<teleporter>\")");

            setPermissionGroup(GameMode.Adventure);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            String targetPlayerName = (String)this.playerArg.get(ctx);
            String teleporterName = (String)this.teleporterArg.get(ctx);

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType()); if (playerRef == null) return;  UUID playerUuid = playerRef.getUuid(); TeleporterManager manager = TeleporterManager.getInstance(); TeleporterInfo teleporter = TeleporterLookup.findExact(manager, teleporterName); if (teleporter == null) { ctx.sendMessage(Translations.msgError("cmd.trust.teleporterNotFound", new Object[] { "name", teleporterName })); ctx.sendMessage(Translations.msgInfo("cmd.trust.useQuotes", new Object[0])); ctx.sendMessage(Translations.msgInfo("cmd.trust.example", new Object[0])); return; }  if (!teleporter.isOwner(playerUuid) && !manager.isInBypassMode(playerUuid)) { ctx.sendMessage(Translations.msgError("cmd.trust.notOwner", new Object[0])); return; }  UUID targetUuid = null; String resolvedName = null; for (PlayerRef onlinePlayer : Universe.get().getPlayers()) { if (onlinePlayer.getUsername().equalsIgnoreCase(targetPlayerName)) { targetUuid = onlinePlayer.getUuid(); resolvedName = onlinePlayer.getUsername(); break; }  }  if (targetUuid == null) { ctx.sendMessage(Translations.msgError("cmd.trust.playerNotFound", new Object[] { "name", targetPlayerName })); ctx.sendMessage(Translations.msgInfo("cmd.trust.playerMustBeOnline", new Object[0])); return; }  if (targetUuid.equals(playerUuid)) { ctx.sendMessage(Translations.msgWarning("cmd.trust.cannotTrustSelf", new Object[0])); return; }  if (teleporter.isTrusted(targetUuid)) { ctx.sendMessage(Translations.msgWarning("cmd.trust.alreadyTrusted", new Object[] { "name", resolvedName })); return; }  teleporter.addTrustedPlayer(targetUuid); manager.markDirty(); ctx.sendMessage(Translations.msgSuccess("cmd.trust.success", new Object[] { "name", resolvedName, "teleporter", teleporter.displayName() })); ctx.sendMessage(Translations.msgInfo("cmd.trust.successInfo", new Object[0])); }, (Executor)world);
        }
    }


    private static final class UntrustSubCommand
    extends AbstractAsyncCommand
    {
        private final RequiredArg<String> playerArg = withRequiredArg("player", "Player name to untrust", (ArgumentType)ArgTypes.STRING);
        private final RequiredArg<String> teleporterArg = withRequiredArg("teleporter", "Teleporter name (must use quotes)", (ArgumentType)ArgTypes.STRING);

        UntrustSubCommand() {
            super("untrust", "Remove trust from a player on your teleporter (use: untrust <player> \"<teleporter>\")");

            setPermissionGroup(GameMode.Adventure);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            String targetPlayerName = (String)this.playerArg.get(ctx);
            String teleporterName = (String)this.teleporterArg.get(ctx);

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType()); if (playerRef == null) return;  UUID playerUuid = playerRef.getUuid(); TeleporterManager manager = TeleporterManager.getInstance(); TeleporterInfo teleporter = TeleporterLookup.findExact(manager, teleporterName); if (teleporter == null) { ctx.sendMessage(Translations.msgError("cmd.trust.teleporterNotFound", new Object[] { "name", teleporterName })); ctx.sendMessage(Translations.msgInfo("cmd.trust.useQuotes", new Object[0])); return; }  if (!teleporter.isOwner(playerUuid) && !manager.isInBypassMode(playerUuid)) { ctx.sendMessage(Translations.msgError("cmd.untrust.notOwner", new Object[0])); return; }  UUID targetUuid = null; String resolvedName = targetPlayerName; for (PlayerRef onlinePlayer : Universe.get().getPlayers()) { if (onlinePlayer.getUsername().equalsIgnoreCase(targetPlayerName)) { targetUuid = onlinePlayer.getUuid(); resolvedName = onlinePlayer.getUsername(); break; }  }  if (targetUuid == null) for (UUID trusted : teleporter.getTrustedPlayers()) { if (trusted.toString().toLowerCase().startsWith(targetPlayerName.toLowerCase())) { targetUuid = trusted; resolvedName = trusted.toString().substring(0, 8) + "..."; break; }  }   if (targetUuid == null) { ctx.sendMessage(Translations.msgError("cmd.untrust.playerNotFound", new Object[] { "name", targetPlayerName })); ctx.sendMessage(Translations.msgInfo("cmd.untrust.useTrustlist", new Object[0])); return; }  if (!teleporter.isTrusted(targetUuid)) { ctx.sendMessage(Translations.msgWarning("cmd.untrust.notTrusted", new Object[] { "name", resolvedName })); return; }  teleporter.removeTrustedPlayer(targetUuid); manager.markDirty(); ctx.sendMessage(Translations.msgSuccess("cmd.untrust.success", new Object[] { "name", resolvedName, "teleporter", teleporter.displayName() })); }, (Executor)world);
        }
    }


    private static final class TrustListSubCommand
    extends AbstractAsyncCommand
    {
        private final RequiredArg<String> teleporterArg = withRequiredArg("teleporter", "Teleporter name (must use quotes)", (ArgumentType)ArgTypes.STRING);

        TrustListSubCommand() {
            super("trustlist", "List trusted players on your teleporter (use: trustlist \"<teleporter>\")");

            setPermissionGroup(GameMode.Adventure);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            String teleporterName = (String)this.teleporterArg.get(ctx);

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType()); if (playerRef == null) return;  UUID playerUuid = playerRef.getUuid(); TeleporterManager manager = TeleporterManager.getInstance(); TeleporterInfo teleporter = TeleporterLookup.findExact(manager, teleporterName); if (teleporter == null) { ctx.sendMessage(Translations.msgError("cmd.trust.teleporterNotFound", new Object[] { "name", teleporterName })); ctx.sendMessage(Translations.msgInfo("cmd.trust.useQuotes", new Object[0])); return; }  if (!teleporter.isOwnerOrTrusted(playerUuid) && !manager.isInBypassMode(playerUuid)) { ctx.sendMessage(Translations.msgError("cmd.trustlist.notOwnerOrTrusted", new Object[0])); return; }  Set<UUID> trusted = teleporter.getTrustedPlayers(); if (trusted.isEmpty()) { ctx.sendMessage(Translations.msgWarning("cmd.trustlist.empty", new Object[] { "teleporter", teleporter.displayName() })); ctx.sendMessage(Translations.msgInfo("cmd.trustlist.addHint", new Object[0])); return; }  Map<UUID, String> onlineNames = TeleporterCommand.getOnlinePlayerNames(); ctx.sendMessage(Message.raw(Translations.tr("cmd.trustlist.header", new Object[] { "teleporter", teleporter.displayName() })).color(TeleporterCommand.COLOR_HEADER).bold(true)); for (UUID trustedUuid : trusted) { String name = TeleporterCommand.getPlayerName(trustedUuid, onlineNames); ctx.sendMessage(Message.raw("  - " + name).color(TeleporterCommand.COLOR_PLAYER)); }  ctx.sendMessage(Message.raw(Translations.tr("cmd.trustlist.total", new Object[] { "count", Integer.valueOf(trusted.size()) })).color(TeleporterCommand.COLOR_PAGE_INFO)); }, (Executor)world);
        }
    }


    private static final class ReloadSubCommand
    extends AbstractAsyncCommand
    {
        ReloadSubCommand() {
            super("reload", "Reload teleporter and warp data from files");

            setPermissionGroup(null);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();


            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            ctx.sendMessage(Translations.msgWarning("cmd.reload.description", new Object[0]));


            return CompletableFuture.runAsync(() -> {
                try {
                    TeleporterManager manager = TeleporterManager.getInstance();


                    manager.reloadAll();

                    ctx.sendMessage(Translations.msgSuccess("cmd.reload.success", new Object[0]));
                } catch (Exception e) {
                    ctx.sendMessage(Translations.msgError("cmd.reload.error", new Object[] { "error", e.getMessage() }));
                    ((HytaleLogger.Api)TeleporterCommand.logger.at(Level.SEVERE).withCause(e)).log("Failed to reload teleporter data");
                }
            }, (Executor)world);
        }
    }


    private static final class BypassSubCommand
    extends AbstractAsyncCommand
    {
        BypassSubCommand() {
            super("bypass", "Toggle bypass mode to access all teleporters");

            setPermissionGroup(null);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType()); if (playerRef == null) return;  UUID playerUuid = playerRef.getUuid(); TeleporterManager manager = TeleporterManager.getInstance(); boolean nowBypassing = manager.toggleBypassMode(playerUuid); if (nowBypassing) { ctx.sendMessage(Translations.msgSuccess("cmd.bypass.enabled", new Object[0])); } else { ctx.sendMessage(Translations.msgWarning("cmd.bypass.disabled", new Object[0])); }  }, (Executor)world);
        }
    }


    private static final class GoSubCommand
    extends AbstractAsyncCommand
    {
        private final RequiredArg<String> nameArg = withRequiredArg("name", "Teleporter name (use quotes for names with spaces)", (ArgumentType)ArgTypes.STRING);

        GoSubCommand() {
            super("go", "Teleport to a teleporter by name (use quotes: \"name\")");

            setPermissionGroup(null);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }


            String targetName = (String)this.nameArg.get(ctx);

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType()); if (playerRef == null) return;  UUID playerUuid = playerRef.getUuid(); TeleporterManager manager = TeleporterManager.getInstance(); if (!manager.isInBypassMode(playerUuid)) { ctx.sendMessage(Translations.msgError("cmd.go.bypassRequired", new Object[0])); return; }  TeleporterInfo targetTeleporter = TeleporterLookup.findFlexible(manager, targetName); if (targetTeleporter == null) { ctx.sendMessage(Translations.msgError("cmd.go.notFound", new Object[] { "name", targetName })); ctx.sendMessage(Translations.msgInfo("cmd.go.useListHint", new Object[0])); return; }  String targetDimension = targetTeleporter.dimension(); World targetWorld = manager.getWorld(targetDimension); if (targetWorld == null) { ctx.sendMessage(Translations.msgError("cmd.go.worldNotLoaded", new Object[] { "world", targetDimension })); return; }  double targetX = targetTeleporter.blockX() + 0.5D; double targetY = targetTeleporter.blockY() + 6.0D; double targetZ = targetTeleporter.blockZ() + 0.5D; if (world.getName().equals(targetDimension)) { performTeleport(store, ref, targetX, targetY, targetZ); ctx.sendMessage(Translations.msgSuccess("cmd.go.success", new Object[] { "name", targetTeleporter.displayName() })); } else { targetWorld.execute(() -> { performTeleport(store, ref, targetX, targetY, targetZ); ctx.sendMessage(Translations.msgSuccess("cmd.go.success", new Object[] { "name", targetTeleporter.displayName() })); }); }  }, (Executor)world);
        }


        private void performTeleport(Store<EntityStore> store, Ref<EntityStore> ref, double x, double y, double z) {
            TransformComponent transform = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());
            if (transform != null) {
                Vector3f currentRotation = transform.getRotation();
                Teleport teleport = Teleport.createExact(new Vector3d(x, y, z), currentRotation, currentRotation);


                store.addComponent(ref, Teleport.getComponentType(), teleport);
            }
        }
    }


    private static final class DestinationSubCommand
    extends AbstractAsyncCommand
    {
        DestinationSubCommand() {
            super("destination", "Manage custom destinations");

            setPermissionGroup(null);
            addSubCommand((AbstractCommand)new TeleporterCommand.DestinationCreateSubCommand());
            addSubCommand((AbstractCommand)new TeleporterCommand.DestinationListSubCommand());
            addSubCommand((AbstractCommand)new TeleporterCommand.DestinationRemoveSubCommand());
            addSubCommand((AbstractCommand)new TeleporterCommand.DestinationSetSubCommand());
        }


        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.header", new Object[0])).color(TeleporterCommand.COLOR_HEADER).bold(true));
            ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.help.create", new Object[0])).color(Color.GRAY));
            ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.help.list", new Object[0])).color(Color.GRAY));
            ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.help.remove", new Object[0])).color(Color.GRAY));
            ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.help.set", new Object[0])).color(Color.GRAY));
            return CompletableFuture.completedFuture(null);
        }
    }


    private static final class DestinationCreateSubCommand
    extends AbstractAsyncCommand
    {
        private final RequiredArg<String> nameArg = withRequiredArg("name", "Destination name (use quotes for names with spaces)", (ArgumentType)ArgTypes.STRING);
        private final RequiredArg<Double> xArg = withRequiredArg("x", "X coordinate", (ArgumentType)ArgTypes.DOUBLE);
        private final RequiredArg<Double> yArg = withRequiredArg("y", "Y coordinate", (ArgumentType)ArgTypes.DOUBLE);
        private final RequiredArg<Double> zArg = withRequiredArg("z", "Z coordinate", (ArgumentType)ArgTypes.DOUBLE);

        DestinationCreateSubCommand() {
            super("create", "Create custom destination (use quotes: \"name\" x y z)");

            setPermissionGroup(null);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }


            String destName = (String)this.nameArg.get(ctx);
            double x = ((Double)this.xArg.get(ctx)).doubleValue();
            double y = ((Double)this.yArg.get(ctx)).doubleValue();
            double z = ((Double)this.zArg.get(ctx)).doubleValue();

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { PlayerRef playerRef = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType()); String creatorUuid = (playerRef != null) ? playerRef.getUuid().toString() : null; TeleporterManager manager = TeleporterManager.getInstance(); if (destName == null || destName.isEmpty()) { ctx.sendMessage(Translations.msgError("cmd.destination.create.nameEmpty", new Object[0])); return; }  if (manager.customDestinationExists(destName)) { ctx.sendMessage(Translations.msgError("cmd.destination.create.alreadyExists", new Object[] { "name", destName })); ctx.sendMessage(Translations.msgInfo("cmd.destination.create.removeHint", new Object[0])); return; }  if (TeleportPlugin.get().getWarps().containsKey(destName.toLowerCase())) { ctx.sendMessage(Translations.msgError("cmd.destination.create.warpExists", new Object[] { "name", destName })); return; }  boolean success = manager.createCustomDestination(destName, world.getName(), x, y, z, creatorUuid); if (success) { ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.create.success", new Object[] { "name", destName })).color(Color.GREEN).bold(true)); ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.create.location", new Object[] { "dimension", world.getName(), "x", Double.valueOf(x), "y", Double.valueOf(y), "z", Double.valueOf(z) })).color(TeleporterCommand.COLOR_COORDS)); ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.create.linkHint", new Object[] { "name", destName })).color(Color.GRAY)); } else { ctx.sendMessage(Translations.msgError("cmd.destination.create.failed", new Object[0])); }  }, (Executor)world);
        }
    }


    private static final class DestinationListSubCommand
    extends AbstractAsyncCommand
    {
        DestinationListSubCommand() {
            super("list", "List all custom destinations");

            setPermissionGroup(null);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { TeleporterManager manager = TeleporterManager.getInstance(); Collection<CustomDestination> destinations = manager.getAllCustomDestinations(); if (destinations.isEmpty()) { ctx.sendMessage(Translations.msgWarning("cmd.destination.list.empty", new Object[0])); ctx.sendMessage(Translations.msgInfo("cmd.destination.list.createHint", new Object[0])); return; }  ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.list.header", new Object[] { "count", Integer.valueOf(destinations.size()) })).color(TeleporterCommand.COLOR_HEADER).bold(true)); List<CustomDestination> sorted = new ArrayList<>(destinations); sorted.sort((a, b) -> a.name().compareToIgnoreCase(b.name())); for (CustomDestination dest : sorted) { ctx.sendMessage(Message.raw(dest.name()).color(TeleporterCommand.COLOR_NAME).bold(true)); ctx.sendMessage(Message.raw("  %s (%.1f, %.1f, %.1f)".formatted(new Object[] { dest.dimension(), Double.valueOf(dest.x()), Double.valueOf(dest.y()), Double.valueOf(dest.z()) })).color(TeleporterCommand.COLOR_COORDS)); }  }, (Executor)world);
        }
    }


    private static final class DestinationRemoveSubCommand
    extends AbstractAsyncCommand
    {
        private final RequiredArg<String> nameArg = withRequiredArg("name", "Destination name (use quotes for names with spaces)", (ArgumentType)ArgTypes.STRING);

        DestinationRemoveSubCommand() {
            super("remove", "Remove custom destination (use quotes: \"name\")");

            setPermissionGroup(null);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            String destName = (String)this.nameArg.get(ctx);

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { TeleporterManager manager = TeleporterManager.getInstance(); if (!manager.customDestinationExists(destName)) { ctx.sendMessage(Translations.msgError("cmd.destination.remove.notFound", new Object[] { "name", destName })); ctx.sendMessage(Translations.msgInfo("cmd.destination.remove.listHint", new Object[0])); return; }  boolean success = manager.removeCustomDestination(destName); if (success) { ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.remove.success", new Object[] { "name", destName })).color(Color.GREEN).bold(true)); ctx.sendMessage(Translations.msgWarning("cmd.destination.remove.warning", new Object[0])); } else { ctx.sendMessage(Translations.msgError("cmd.destination.remove.failed", new Object[0])); }  }, (Executor)world);
        }
    }


    private static final class DestinationSetSubCommand
    extends AbstractAsyncCommand
    {
        private final RequiredArg<String> teleporterArg = withRequiredArg("teleporter", "Teleporter name (use quotes for spaces)", (ArgumentType)ArgTypes.STRING);
        private final RequiredArg<String> destinationArg = withRequiredArg("destination", "Destination name (use quotes for spaces)", (ArgumentType)ArgTypes.STRING);

        DestinationSetSubCommand() {
            super("set", "Set teleporter destination (use quotes: \"teleporter\" \"destination\")");

            setPermissionGroup(null);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }

            String teleporterName = (String)this.teleporterArg.get(ctx);
            String destinationName = (String)this.destinationArg.get(ctx);

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { TeleporterManager manager = TeleporterManager.getInstance(); TeleporterInfo teleporter = TeleporterLookup.findFlexible(manager, teleporterName); if (teleporter == null) { ctx.sendMessage(Translations.msgError("cmd.destination.set.teleporterNotFound", new Object[] { "name", teleporterName })); ctx.sendMessage(Translations.msgInfo("cmd.destination.set.teleporterListHint", new Object[0])); return; }  CustomDestination destination = manager.getCustomDestination(destinationName); if (destination == null) { ctx.sendMessage(Translations.msgError("cmd.destination.set.destinationNotFound", new Object[] { "name", destinationName })); ctx.sendMessage(Translations.msgInfo("cmd.destination.set.destinationListHint", new Object[0])); return; }  boolean success = manager.updateTeleporterWarpDestination(teleporter, destinationName.toLowerCase()); if (success) { teleporter.setWarpDestination(destinationName.toLowerCase()); manager.markDirty(); ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.set.success", new Object[] { "teleporter", teleporter.displayName(), "destination", destination.name() })).color(Color.GREEN).bold(true)); ctx.sendMessage(Message.raw(Translations.tr("cmd.destination.set.location", new Object[] { "dimension", destination.dimension(), "x", Double.valueOf(destination.x()), "y", Double.valueOf(destination.y()), "z", Double.valueOf(destination.z()) })).color(TeleporterCommand.COLOR_COORDS)); } else { ctx.sendMessage(Translations.msgError("cmd.destination.set.failed", new Object[0])); }  }, (Executor)world);
        }
    }


    private static final class ServerSubCommand
    extends AbstractAsyncCommand
    {
        private final RequiredArg<String> nameArg = withRequiredArg("name", "Teleporter name (use quotes for spaces)", (ArgumentType)ArgTypes.STRING);

        ServerSubCommand() {
            super("server", "Toggle server teleporter mode (use quotes: \"name\")");

            setPermissionGroup(null);
        }

        @NonNullDecl
        protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
            Player player;
            CommandSender sender = ctx.sender();

            if (sender instanceof Player) { player = (Player)sender; }
            else { ctx.sendMessage(Translations.msgError("cmd.playerOnly", new Object[0]));
            return CompletableFuture.completedFuture(null); }


            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                ctx.sendMessage(Translations.msgError("cmd.notInWorld", new Object[0]));
                return CompletableFuture.completedFuture(null);
            }


            String targetName = (String)this.nameArg.get(ctx);

            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();

            return CompletableFuture.runAsync(() -> { TeleporterManager manager = TeleporterManager.getInstance(); TeleporterInfo targetTeleporter = TeleporterLookup.findFlexible(manager, targetName); if (targetTeleporter == null) { ctx.sendMessage(Translations.msgError("cmd.server.notFound", new Object[] { "name", targetName })); ctx.sendMessage(Translations.msgInfo("cmd.server.listHint", new Object[0])); return; }  boolean nowServer = !targetTeleporter.isServerTeleporter(); targetTeleporter.setServerTeleporter(nowServer); manager.markDirty(); if (nowServer) { ctx.sendMessage(Message.raw(Translations.tr("cmd.server.enabled", new Object[] { "name", targetTeleporter.displayName() })).color(Color.GREEN).bold(true)); ctx.sendMessage(Translations.msgInfo("cmd.server.enabled.info1", new Object[0])); ctx.sendMessage(Translations.msgInfo("cmd.server.enabled.info2", new Object[0])); } else { ctx.sendMessage(Message.raw(Translations.tr("cmd.server.disabled", new Object[] { "name", targetTeleporter.displayName() })).color(Color.YELLOW).bold(true)); ctx.sendMessage(Translations.msgInfo("cmd.server.disabled.info", new Object[0])); }  }, (Executor)world);
        }
    }
}