package com.frankfurtlin.mixinenhance.selfcheck;

import com.frankfurtlin.mixinenhance.MixinEnhance;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 注入自检：回答「这次版本升级后，哪些注入点静默失效了」。
 *
 * <p>项目里大量注入点带 {@code require = 0}（匹配不上就静默跳过，而不是启动崩溃）——好处是不炸，
 * 代价是功能会安静地消失，且没有任何信号。这里改为「可观测」：</p>
 * <ol>
 *   <li>{@code tools/gen_injection_manifest.py} 扫描 mixin 源码，把每个 {@code require = 0} 的
 *       「处理器方法名 + 目标类」写进 {@code resources/mixinenhance/injections.txt}（CI 会校验它是否过期）；</li>
 *   <li>启动时按清单逐个检查：处理器方法是否真的被 Mixin 注入了目标类。Mixin 会把回调方法复制进目标类
 *       （名字带前缀，但保留原方法名），所以「在目标类的声明方法里找得到该名字」即说明注入成功。</li>
 * </ol>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/15
 */
public final class InjectionSelfCheck {
    /** 清单由 tools/gen_injection_manifest.py 生成，格式：处理器|目标类|mixin|端 */
    private static final String MANIFEST = "/mixinenhance/injections.txt";

    private static volatile List<Result> results = List.of();

    private InjectionSelfCheck() {
    }

    /** 清单里的一条注入点。 */
    public record Entry(String handler, String target, String mixin, String side) {
    }

    /** 一条注入点的检查结果。 */
    public record Result(Entry entry, boolean ok, String detail) {
    }

    /** 启动时执行自检并输出汇总（全通过是 INFO，有失效项是 WARN 列表）。 */
    public static void init() {
        results = check();
        if (results.isEmpty()) {
            MixinEnhance.LOGGER.error("注入自检清单为空：{} 缺失或为空（构建产物不完整？）", MANIFEST);
            return;
        }
        List<Result> failed = failed();
        if (failed.isEmpty()) {
            MixinEnhance.LOGGER.info("注入自检：{}/{} 项全部生效", results.size(), results.size());
            return;
        }
        MixinEnhance.LOGGER.warn("注入自检：{}/{} 项生效，{} 项已失效（require = 0 会静默跳过，对应功能将无效）：",
            results.size() - failed.size(), results.size(), failed.size());
        for (Result result : failed) {
            MixinEnhance.LOGGER.warn("  ✗ {} ← {}（{}）：{}",
                result.entry().handler(), result.entry().mixin(), result.entry().side(), result.detail());
        }
    }

    /** 重新执行自检。 */
    public static List<Result> recheck() {
        results = check();
        return results;
    }

    public static List<Result> results() {
        return results;
    }

    public static List<Result> failed() {
        return results.stream().filter(result -> !result.ok()).toList();
    }

    /** 按清单逐项检查（专用服务端会跳过纯客户端注入点，它们本就不会被应用）。 */
    private static List<Result> check() {
        List<Result> checked = new ArrayList<>();
        boolean dedicatedServer = FabricLoader.getInstance().getEnvironmentType()
            == net.fabricmc.api.EnvType.SERVER;
        ClassLoader loader = InjectionSelfCheck.class.getClassLoader();
        for (Entry entry : readManifest()) {
            if (dedicatedServer && "client".equals(entry.side())) {
                continue;
            }
            checked.add(verify(entry, loader));
        }
        return List.copyOf(checked);
    }

    private static Result verify(Entry entry, ClassLoader loader) {
        try {
            Class<?> target = Class.forName(entry.target(), false, loader);
            for (Method method : target.getDeclaredMethods()) {
                if (method.getName().contains(entry.handler())) {
                    return new Result(entry, true, "ok");
                }
            }
            // 方法签名随版本变化时，注入点匹配不上，处理器不会被复制进目标类
            return new Result(entry, false, "目标类里没有该注入的处理器方法");
        } catch (Throwable e) {
            return new Result(entry, false, "检查失败：" + e);
        }
    }

    private static List<Entry> readManifest() {
        List<Entry> entries = new ArrayList<>();
        try (InputStream in = InjectionSelfCheck.class.getResourceAsStream(MANIFEST)) {
            if (in == null) {
                return List.of();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    String[] parts = trimmed.split("\\|");
                    if (parts.length >= 4) {
                        entries.add(new Entry(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim()));
                    }
                }
            }
        } catch (Exception e) {
            MixinEnhance.LOGGER.error("读取注入清单失败：{}", e.toString());
            return List.of();
        }
        return entries;
    }
}
