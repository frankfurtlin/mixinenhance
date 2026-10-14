package com.frankfurtlin.mixinenhance;

import com.frankfurtlin.mixinenhance.selfcheck.InjectionSelfCheck;
import com.frankfurtlin.mixinenhance.util.ConfigPaths;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 游戏内命令 {@code /mixinenhance}：查看注入自检、重载配置、列出/切换配置项。
 *
 * <p>配置界面之外的第二个人口：像「这次升级后哪些注入点失效了」这类想随时确认的信息，
 * 用命令比翻界面快得多。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/15
 */
public final class MixinEnhanceCommand {
    private static final SuggestionProvider<CommandSourceStack> SECTIONS = (context, builder) ->
        SharedSuggestionProvider.suggest(sectionNames(), builder);

    private static final SuggestionProvider<CommandSourceStack> OPTIONS = (context, builder) ->
        SharedSuggestionProvider.suggest(optionPaths(), builder);

    private MixinEnhanceCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("mixinenhance")
                .executes(context -> help(context.getSource()))
                .then(Commands.literal("status").executes(context -> status(context.getSource())))
                .then(Commands.literal("recheck")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(context -> recheck(context.getSource())))
                .then(Commands.literal("reload")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .executes(context -> reload(context.getSource())))
                .then(Commands.literal("list")
                    .executes(context -> list(context.getSource(), null))
                    .then(Commands.argument("section", StringArgumentType.word())
                        .suggests(SECTIONS)
                        .executes(context -> list(context.getSource(),
                            StringArgumentType.getString(context, "section")))))
                .then(Commands.literal("toggle")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.argument("option", StringArgumentType.string())
                        .suggests(OPTIONS)
                        .executes(context -> toggle(context.getSource(),
                            StringArgumentType.getString(context, "option")))))));
    }

    private static int help(CommandSourceStack source) {
        send(source, Component.translatable("command.mixinenhance.help"));
        return 1;
    }

    private static int status(CommandSourceStack source) {
        List<InjectionSelfCheck.Result> results = InjectionSelfCheck.results();
        if (results.isEmpty()) {
            results = InjectionSelfCheck.recheck();
        }
        List<InjectionSelfCheck.Result> failed = results.stream()
            .filter(result -> !result.ok()).toList();
        int ok = results.size() - failed.size();
        send(source, Component.translatable("command.mixinenhance.status.summary", ok, results.size()));
        if (!failed.isEmpty()) {
            send(source, Component.translatable("command.mixinenhance.status.failed", failed.size()));
            for (InjectionSelfCheck.Result result : failed) {
                send(source, Component.translatable("command.mixinenhance.status.line",
                    result.entry().handler(), result.entry().mixin(), result.detail()));
            }
        }
        return ok;
    }

    private static int recheck(CommandSourceStack source) {
        List<InjectionSelfCheck.Result> results = InjectionSelfCheck.recheck();
        long ok = results.stream().filter(InjectionSelfCheck.Result::ok).count();
        if (results.stream().anyMatch(result -> !result.ok())) {
            return status(source);
        }
        send(source, Component.translatable("command.mixinenhance.status.summary", ok, results.size()));
        return (int) ok;
    }

    private static int reload(CommandSourceStack source) {
        MixinEnhanceConfig.reload();
        send(source, Component.translatable("command.mixinenhance.reload.done"));
        return 1;
    }

    /** {@code list}：不带参数列出各板块的开关统计，带参数列出该板块的每一项（含嵌套分组）。 */
    private static int list(CommandSourceStack source, String section) {
        Map<String, Object> sections = sections();
        if (section == null) {
            for (Map.Entry<String, Object> entry : sections.entrySet()) {
                long total = 0;
                long enabled = 0;
                long numbers = 0;
                for (Map.Entry<String, Object> option : entriesOf(entry.getValue())) {
                    if (option.getValue() instanceof Boolean value) {
                        total++;
                        if (value) {
                            enabled++;
                        }
                    } else {
                        numbers++;
                    }
                }
                if (total > 0) {
                    send(source, Component.translatable("command.mixinenhance.list.section",
                        entry.getKey(), enabled, total));
                }
                if (numbers > 0) {
                    send(source, Component.translatable("command.mixinenhance.list.section.numbers",
                        entry.getKey(), numbers));
                }
            }
            return 1;
        }
        Object holder = sections.get(section);
        if (holder == null) {
            send(source, Component.translatable("command.mixinenhance.list.unknown", section));
            return 0;
        }
        for (Map.Entry<String, Object> option : entriesOf(holder)) {
            send(source, Component.translatable("command.mixinenhance.list.entry",
                shorten(section) + "." + option.getKey(), option.getValue()));
        }
        return 1;
    }

    private static int toggle(CommandSourceStack source, String path) {
        Object owner = MixinEnhanceConfig.get();
        Field field = null;
        String[] parts = path.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            field = findField(owner.getClass(), parts[i]);
            if (field == null) {
                send(source, Component.translatable("command.mixinenhance.toggle.unknown", path));
                return 0;
            }
            try {
                field.setAccessible(true);
                if (i < parts.length - 1) {
                    Object next = field.get(owner);
                    if (next == null) {
                        send(source, Component.translatable("command.mixinenhance.toggle.unknown", path));
                        return 0;
                    }
                    owner = next;
                }
            } catch (Exception e) {
                send(source, Component.translatable("command.mixinenhance.toggle.unknown", path));
                return 0;
            }
        }

        try {
            field.setAccessible(true);
            if (field.getType() == boolean.class) {
                boolean value = !field.getBoolean(owner);
                field.setBoolean(owner, value);
                MixinEnhanceConfig.save();
                send(source, Component.translatable("command.mixinenhance.toggle.done", path, value));
                return value ? 1 : 0;
            }
            send(source, Component.translatable("command.mixinenhance.toggle.notBoolean", path, field.get(owner)));
            return 0;
        } catch (Exception e) {
            send(source, Component.translatable("command.mixinenhance.toggle.failed", e.toString()));
            return 0;
        }
    }

    private static void send(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

    /** 配置里的 7 个板块：短名 -> 板块对象。 */
    private static Map<String, Object> sections() {
        return ConfigPaths.sections(MixinEnhanceConfig.get());
    }

    private static List<String> sectionNames() {
        return new ArrayList<>(sections().keySet());
    }

    /** 所有配置项路径（板块.…），供命令补全；嵌套分组会展开。 */
    private static List<String> optionPaths() {
        return ConfigPaths.allPaths(MixinEnhanceConfig.get());
    }

    /** 递归展开一个配置对象：嵌套分组继续下钻，叶子节点以「相对路径 -> 值」返回。 */
    private static List<Map.Entry<String, Object>> entriesOf(Object owner) {
        return ConfigPaths.asMapEntries(owner);
    }

    private static Field findField(Class<?> type, String name) {
        return ConfigPaths.find(type, name);
    }

    private static String shorten(String name) {
        return ConfigPaths.shorten(name);
    }
}
