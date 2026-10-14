package com.frankfurtlin.mixinenhance;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.frankfurtlin.mixinenhance.config.ModMenuConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 配置文件版本迁移。
 *
 * <p>AutoConfig 只按「字段名」读写配置：字段被改名或删除后，旧配置里的值会被静默丢掉、悄悄回退到默认值，
 * 用户只会觉得「我的设置怎么没了」。这里在 AutoConfig 读文件<b>之前</b>直接对 json5 做迁移，
 * 并把「文件里有、但代码里已不存在的键」列出来提醒，避免静默丢失。</p>
 *
 * <p>新增迁移的写法见 {@code docs/开发规范.md}：把 {@link #CURRENT_VERSION} 加 1，并在 {@link #MIGRATIONS}
 * 里加一条 {@code fromVersion = 旧版本} 的迁移，负责把该版本的旧键搬到新键。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/15
 */
public final class ConfigMigrations {
    /** 当前配置结构版本；每次改动配置字段（改名/移动/删除）都应该 +1 并补一条迁移。 */
    public static final int CURRENT_VERSION = 1;

    /** 配置文件名（与 {@code @Config(name = "mixinenhance")} + Jankson 序列化器一致）。 */
    private static final String CONFIG_FILE = "mixinenhance.json5";

    /** 版本迁移表：{@code fromVersion} 表示「把 v{fromVersion} 的配置升级到 v{fromVersion+1}」。 */
    private static final List<Migration> MIGRATIONS = List.of(
        // 目前尚无实际迁移：版本 1 是首个启用版本号的版本，旧配置（无版本号）视为 0，仅补上版本号。
    );

    private ConfigMigrations() {
    }

    /** 迁移定义。 */
    public interface Migration {
        /** 本迁移处理的起始版本号。 */
        int fromVersion();

        /** 把旧结构改写为新结构。 */
        void apply(JsonObject root);
    }

    /** 在 AutoConfig 注册前调用：按需迁移配置并检查失效键。 */
    public static void run() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        if (!Files.exists(path)) {
            return;
        }
        JsonObject root;
        try {
            root = Jankson.builder().build().load(path.toFile());
        } catch (Exception e) {
            MixinEnhance.LOGGER.warn("读取配置文件失败，跳过迁移：{}", e.toString());
            return;
        }
        if (root == null) {
            return;
        }

        int version = readVersion(root);
        if (version > CURRENT_VERSION) {
            MixinEnhance.LOGGER.warn("配置文件版本（v{}）高于本模组支持的 v{}，可能来自更新版本，已保持原样；"
                + "若出现异常请删除 config/{} 后重进游戏", version, CURRENT_VERSION, CONFIG_FILE);
            return;
        }

        boolean migrated = false;
        for (Migration migration : MIGRATIONS) {
            if (migration.fromVersion() >= version && migration.fromVersion() < CURRENT_VERSION) {
                try {
                    migration.apply(root);
                    MixinEnhance.LOGGER.info("已执行配置迁移：v{} -> v{}", migration.fromVersion(), migration.fromVersion() + 1);
                    migrated = true;
                } catch (Exception e) {
                    MixinEnhance.LOGGER.error("配置迁移 v{} 失败，已跳过：{}", migration.fromVersion(), e.toString());
                }
            }
        }
        if (version < CURRENT_VERSION) {
            root.put("configVersion", new JsonPrimitive(CURRENT_VERSION));
            migrated = true;
            MixinEnhance.LOGGER.info("配置文件版本已标记为 v{}（原 v{}）", CURRENT_VERSION, version);
        }
        if (migrated) {
            write(path, root);
        } else {
            warnUnknownKeys(root);
        }
    }

    private static int readVersion(JsonObject root) {
        JsonElement element = root.get("configVersion");
        if (element instanceof JsonPrimitive primitive) {
            Object value = primitive.getValue();
            if (value instanceof Number number) {
                return number.intValue();
            }
        }
        return 0;
    }

    /** 列出「配置文件里有、但代码里没有」的键，这些键的值会在下次保存时丢失。 */
    private static void warnUnknownKeys(JsonObject root) {
        Set<String> known = new TreeSet<>();
        collectPaths(ModMenuConfig.class, "", known);
        Set<String> unknown = new LinkedHashSet<>();
        collectUnknown(root, "", known, unknown);
        if (!unknown.isEmpty()) {
            MixinEnhance.LOGGER.warn("配置文件中有 {} 个已失效的配置项（字段已改名或删除，其值将被忽略）：{}",
                unknown.size(), String.join(", ", unknown));
        }
    }

    /** 递归收集配置类里所有字段的路径，如 {@code playerConfig.enableXpMerge}。 */
    private static void collectPaths(Class<?> type, String prefix, Set<String> out) {
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            String path = prefix + field.getName();
            out.add(path);
            Class<?> fieldType = field.getType();
            if (!fieldType.isPrimitive() && !fieldType.getName().startsWith("java.")) {
                collectPaths(fieldType, path + ".", out);
            }
        }
    }

    /** 递归比对 json 的键与配置类的字段路径。 */
    private static void collectUnknown(JsonObject node, String prefix, Set<String> known, Set<String> unknown) {
        for (String key : node.keySet()) {
            String path = prefix + key;
            if (!known.contains(path)) {
                unknown.add(path);
                continue;
            }
            JsonElement child = node.get(key);
            if (child instanceof JsonObject object) {
                collectUnknown(object, path + ".", known, unknown);
            }
        }
    }

    private static void write(Path path, JsonObject root) {
        try {
            Files.writeString(path, root.toJson(true, true) + System.lineSeparator());
            MixinEnhance.LOGGER.info("配置文件已更新：{}", path);
        } catch (Exception e) {
            MixinEnhance.LOGGER.error("写回配置文件失败：{}", e.toString());
        }
    }
}
