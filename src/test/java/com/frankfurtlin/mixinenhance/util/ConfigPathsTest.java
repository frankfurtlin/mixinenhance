package com.frankfurtlin.mixinenhance.util;

import com.frankfurtlin.mixinenhance.config.ModMenuConfig;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 配置路径工具的行为约束。
 *
 * <p>这套逻辑是 {@code /mixinenhance list} 与配置迁移共用的基础：一旦漏掉嵌套分组，
 * 命令就只会显示「0/0 项」，升级时的迁移也会漏项 —— 所以用测试钉死。</p>
 *
 * @author Frankfurtlin
 * @date 2026/10/15
 */
class ConfigPathsTest {
    private final ModMenuConfig config = new ModMenuConfig();

    @Test
    void 七个板块按声明顺序暴露() {
        Map<String, Object> sections = ConfigPaths.sections(config);
        assertEquals(List.of("general", "player", "block", "item", "entity", "enchant", "fishing"),
            List.copyOf(sections.keySet()));
    }

    @Test
    void 嵌套分组会被递归展开() {
        Map<String, Object> sections = ConfigPaths.sections(config);

        List<String> block = pathsOf(sections.get("block"));
        assertTrue(block.contains("enableVeinMining"), () -> "block 缺少叶子项：" + block);
        assertTrue(block.contains("silkTouchConfig.enableSpawnerDropWithSilkTouch"), () -> "block 缺少嵌套项：" + block);
        assertTrue(block.contains("spawnerFarmConfig.spawnerFarmRate"), () -> "block 缺少嵌套项：" + block);

        List<String> entity = pathsOf(sections.get("entity"));
        assertTrue(entity.contains("equipmentConfig.armorFactor"), () -> "entity 缺少嵌套项：" + entity);
        assertTrue(entity.contains("mobConfig.dieWithSpawner"), () -> "entity 缺少嵌套项：" + entity);
        assertTrue(entity.contains("hostileMobConfig.witherSkeletonSkullDropRate"), () -> "entity 缺少嵌套项：" + entity);
    }

    @Test
    void 每个板块都能摊平出配置项() {
        for (Map.Entry<String, Object> section : ConfigPaths.sections(config).entrySet()) {
            long leaves = ConfigPaths.entries(section.getValue()).size();
            assertTrue(leaves > 0,
                () -> "板块 " + section.getKey() + " 一个配置项都没摊平出来，说明遍历漏了嵌套分组");
        }
    }

    @Test
    void configVersion不参与配置项枚举() {
        assertFalse(ConfigPaths.allPaths(config).stream().anyMatch(path -> path.endsWith("configVersion")),
            "configVersion 是内部字段，不应出现在命令/迁移的配置项里");
    }

    @Test
    void 字段查找兼容完整名与短名() {
        Field byShort = ConfigPaths.find(ModMenuConfig.class, "player");
        Field byFull = ConfigPaths.find(ModMenuConfig.class, "playerConfig");
        assertNotNull(byShort);
        assertNotNull(byFull);
        // 反射每次都会新建 Field 实例，比较名字而不是同一性
        assertEquals(byFull.getName(), byShort.getName());

        Object equipment = ConfigPaths.sections(config).get("entity");
        assertNotNull(ConfigPaths.find(equipment.getClass(), "equipment"));
        assertNotNull(ConfigPaths.find(equipment.getClass(), "equipmentConfig"));
    }

    private static List<String> pathsOf(Object section) {
        return ConfigPaths.entries(section).stream().map(ConfigPaths.Entry::path).toList();
    }
}
