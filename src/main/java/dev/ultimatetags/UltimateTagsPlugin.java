package dev.ultimatetags;

import dev.ultimatetags.animation.AnimationService;
import dev.ultimatetags.command.TagCommand;
import dev.ultimatetags.config.MessageService;
import dev.ultimatetags.config.TagLoader;
import dev.ultimatetags.gui.TagGuiListener;
import dev.ultimatetags.gui.TagGuiService;
import dev.ultimatetags.item.UnlockItemListener;
import dev.ultimatetags.item.UnlockItemService;
import dev.ultimatetags.permission.PermissionService;
import dev.ultimatetags.render.MiniMessageRenderer;
import dev.ultimatetags.storage.PlayerTagRepository;
import dev.ultimatetags.storage.SqlPlayerTagRepository;
import dev.ultimatetags.storage.YamlPlayerTagRepository;
import dev.ultimatetags.tag.TagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class UltimateTagsPlugin extends JavaPlugin {
    private TagRegistry tagRegistry;
    private MiniMessageRenderer renderer;
    private MessageService messages;
    private PlayerTagRepository repository;
    private PermissionService permissionService;
    private UnlockItemService unlockItemService;
    private TagGuiService guiService;
    private AnimationService animationService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("messages/pt_br.yml");
        saveResourceIfMissing("messages/en_us.yml");
        saveResourceIfMissing("tags/vip.yml");

        this.renderer = new MiniMessageRenderer(this);
        this.messages = new MessageService(this, renderer);
        this.tagRegistry = new TagRegistry();
        this.repository = createRepository();
        this.permissionService = new PermissionService(this);
        this.unlockItemService = new UnlockItemService(this, renderer);
        this.guiService = new TagGuiService(this, renderer, messages, tagRegistry, repository);
        this.animationService = new AnimationService(this, tagRegistry);

        Bukkit.getPluginManager().registerEvents(new UnlockItemListener(this, tagRegistry, repository, permissionService, unlockItemService, messages), this);
        Bukkit.getPluginManager().registerEvents(new TagGuiListener(guiService), this);

        registerCommands();
        reloadAll().join();
        animationService.start();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            registerPlaceholderApiExpansion();
        }

        getLogger().info("UltimateTags enabled with " + tagRegistry.size() + " loaded tags.");
    }

    @Override
    public void onDisable() {
        if (animationService != null) {
            animationService.stop();
        }
        if (repository != null) {
            repository.close();
        }
    }

    public CompletableFuture<Void> reloadAll() {
        long started = System.currentTimeMillis();
        return CompletableFuture.supplyAsync(() -> {
            reloadConfig();
            messages.reload();
            var loadResult = new TagLoader(this).loadAll();
            tagRegistry.replace(loadResult.tags(), loadResult.errors());
            renderer.clear();
            guiService.clear();
            getLogger().info("Loaded " + loadResult.tags().size() + " tags in " + (System.currentTimeMillis() - started) + "ms.");
            loadResult.errors().forEach(error -> getLogger().warning(error));
            return null;
        });
    }

    public CompletableFuture<Void> reloadFile(String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            var result = new TagLoader(this).loadFile(Path.of(fileName));
            tagRegistry.replaceFile(fileName, result.tags(), result.errors());
            renderer.clear();
            guiService.clear();
            result.errors().forEach(error -> getLogger().warning(error));
            return null;
        });
    }

    private PlayerTagRepository createRepository() {
        String type = getConfig().getString("storage.type", "YAML").toUpperCase(Locale.ROOT);
        try {
            if (type.equals("SQLITE") || type.equals("MYSQL") || type.equals("MARIADB")) {
                return new SqlPlayerTagRepository(this, type);
            }
        } catch (Exception exception) {
            getLogger().log(Level.WARNING, "SQL storage failed, falling back to YAML.", exception);
        }
        return new YamlPlayerTagRepository(this);
    }

    private void registerCommands() {
        TagCommand executor = new TagCommand(this, tagRegistry, repository, permissionService, unlockItemService, guiService, messages, renderer);
        Command command = new RuntimeTagCommand(executor);
        command.setPermission("ultimatetags.use");
        command.setAliases(List.of("tags"));
        command.setDescription("Main UltimateTags command");
        command.setUsage("/tag");
        Bukkit.getCommandMap().register("ultimatetags", command);
    }

    private void registerPlaceholderApiExpansion() {
        try {
            Class<?> expansionClass = Class.forName("dev.ultimatetags.hook.PlaceholderApiExpansion", true, getClassLoader());
            Object expansion = expansionClass
                    .getConstructor(UltimateTagsPlugin.class, TagRegistry.class, PlayerTagRepository.class, MiniMessageRenderer.class)
                    .newInstance(this, tagRegistry, repository, renderer);
            expansionClass.getMethod("register").invoke(expansion);
            getLogger().info("PlaceholderAPI expansion registered.");
        } catch (Throwable throwable) {
            getLogger().warning("PlaceholderAPI was detected, but the expansion could not be registered: " + throwable.getMessage());
        }
    }

    private void saveResourceIfMissing(String path) {
        if (!getDataFolder().toPath().resolve(path).toFile().exists()) {
            saveResource(path, false);
        }
    }

    public TagRegistry tags() {
        return tagRegistry;
    }

    public PlayerTagRepository repository() {
        return repository;
    }

    public MiniMessageRenderer renderer() {
        return renderer;
    }

    public MessageService messages() {
        return messages;
    }

    private static final class RuntimeTagCommand extends Command {
        private final TagCommand delegate;

        private RuntimeTagCommand(TagCommand delegate) {
            super("tag");
            this.delegate = delegate;
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            return delegate.onCommand(sender, this, commandLabel, args);
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
            List<String> completions = delegate.onTabComplete(sender, this, alias, args);
            return completions == null ? List.of() : completions;
        }
    }
}
