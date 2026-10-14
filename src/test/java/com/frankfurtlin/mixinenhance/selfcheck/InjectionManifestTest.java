package com.frankfurtlin.mixinenhance.selfcheck;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 注入清单必须随产物一起打包，且每行格式可被启动时的自检解析。
 *
 * <p>清单由 {@code tools/gen_injection_manifest.py} 生成并纳入版本控制；这里守住「构建产物里真的有它」
 * ——否则自检会静默变成空操作，失去意义。</p>
 *
 * @author Frankfurtlin
 * @date 2026/10/15
 */
class InjectionManifestTest {
    private static final String MANIFEST = "/mixinenhance/injections.txt";

    @Test
    void 清单存在且条目格式正确() throws Exception {
        List<String> lines = readManifest();
        List<String> entries = lines.stream()
            .map(String::trim)
            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
            .toList();

        assertTrue(entries.size() > 10, () -> "注入清单条目过少（" + entries.size() + "），可能没被生成");
        for (String entry : entries) {
            String[] parts = entry.split("\\|");
            assertEquals(4, parts.length, () -> "清单行格式应为 处理器|目标类|mixin|端：" + entry);
            assertTrue(parts[3].equals("common") || parts[3].equals("client"), () -> "未知的端：" + entry);
        }
    }

    @Test
    void 清单覆盖了客户端与通用两侧() throws Exception {
        List<String> entries = readManifest();
        assertTrue(entries.stream().anyMatch(line -> line.endsWith("|common")), "缺少通用（服务端也生效）的注入点");
        assertTrue(entries.stream().anyMatch(line -> line.endsWith("|client")), "缺少纯客户端的注入点");
    }

    private static List<String> readManifest() throws Exception {
        try (InputStream in = InjectionManifestTest.class.getResourceAsStream(MANIFEST)) {
            assertNotNull(in, "注入清单应随产物打包：" + MANIFEST);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().toList();
            }
        }
    }
}
