package com.frankfurtlin.mixinenhance.util;

import com.frankfurtlin.mixinenhance.config.ModMenuConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置项的反射视图：把「板块 -> 配置项路径 -> 值」摊平，供命令与自检使用。
 *
 * <p>配置里存在嵌套分组（如 {@code blockConfig.silkTouchConfig}、{@code entityConfig.equipmentConfig}），
 * 这里统一递归展开成点分路径（{@code block.silkTouchConfig.enableSpawnerDropWithSilkTouch}），
 * 避免"漏掉嵌套分组"这类低级错误。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/15
 */
public final class ConfigPaths {
    /** 一条配置项：相对路径 + 当前值。 */
    public record Entry(String path, Object value) {
    }

    private ConfigPaths() {
    }

    /** 配置里的板块：短名（general / player / …）-> 板块对象。 */
    public static Map<String, Object> sections(ModMenuConfig root) {
        Map<String, Object> sections = new LinkedHashMap<>();
        for (Field field : root.getClass().getDeclaredFields()) {
            if (!isConfigField(field) || !isNestedConfig(field.getType())) {
                continue;
            }
            Object value = read(field, root);
            if (value != null) {
                sections.put(shorten(field.getName()), value);
            }
        }
        return sections;
    }

    /** 递归摊平一个板块：嵌套分组继续下钻，叶子以「相对路径 -> 值」返回。 */
    public static List<Entry> entries(Object section) {
        List<Entry> entries = new ArrayList<>();
        collect(section, "", entries);
        return entries;
    }

    /** 所有配置项的全路径（板块.…），供命令补全。 */
    public static List<String> allPaths(ModMenuConfig root) {
        List<String> paths = new ArrayList<>();
        for (Map.Entry<String, Object> section : sections(root).entrySet()) {
            for (Entry entry : entries(section.getValue())) {
                paths.add(section.getKey() + "." + entry.path());
            }
        }
        return paths;
    }

    /**
     * 按名字（或短名）找字段：同时接受完整名与去掉 {@code Config} 后缀的短名，
     * 因此 {@code entity}、{@code entityConfig}、{@code equipment} 都能解析。
     */
    public static Field find(Class<?> type, String name) {
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getName().equals(name) || field.getName().equals(name + "Config")) {
                return field;
            }
        }
        return null;
    }

    /** 去掉字段名末尾的 {@code Config}（playerConfig -> player）。 */
    public static String shorten(String name) {
        return name.endsWith("Config") ? name.substring(0, name.length() - "Config".length()) : name;
    }

    /** 计入配置项的字段：非静态、非 transient、且不是内部的 configVersion。 */
    public static boolean isConfigField(Field field) {
        return !Modifier.isStatic(field.getModifiers())
            && !Modifier.isTransient(field.getModifiers())
            && !field.getName().equals("configVersion");
    }

    /** 只有本模组的配置分组才算「嵌套分组」（区别于 boolean/int/double 等叶子）。 */
    private static boolean isNestedConfig(Class<?> type) {
        return !type.isPrimitive() && !type.isEnum() && !type.isArray()
            && type.getPackageName().startsWith("com.frankfurtlin.mixinenhance.config.section");
    }

    private static void collect(Object owner, String prefix, List<Entry> out) {
        for (Field field : owner.getClass().getDeclaredFields()) {
            if (!isConfigField(field)) {
                continue;
            }
            String path = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();
            if (isNestedConfig(field.getType())) {
                Object nested = read(field, owner);
                if (nested != null) {
                    collect(nested, path, out);
                }
                continue;
            }
            out.add(new Entry(path, read(field, owner)));
        }
    }

    private static Object read(Field field, Object owner) {
        try {
            field.setAccessible(true);
            return field.get(owner);
        } catch (Exception e) {
            return "?";
        }
    }

    /** 便于测试与命令：把 Map 风格的键值对转成路径字符串。 */
    public static List<Map.Entry<String, Object>> asMapEntries(Object section) {
        List<Map.Entry<String, Object>> result = new ArrayList<>();
        for (Entry entry : entries(section)) {
            result.add(new AbstractMap.SimpleEntry<>(entry.path(), entry.value()));
        }
        return result;
    }
}
