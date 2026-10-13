package com.linguacraft.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 种子真题卷与真题卷闸门的一致性测试(阶段 A11)。
 *
 * <p>A6-③ 写下 {@link ExamPaperValidator} 时,测试只造了单部分卷(偏移量恒为 0),
 * 于是"卷面题号==答案编号"这条错规则一路通过,而仓库里唯一的种子卷 {@code exam-2026}
 * 从没能被后台保存过。这里直接把种子 SQL 里的两份 JSON 取出来喂给闸门,
 * 让"闸门"与"种子"互相咬死:任何一边改了编号口径,这条测试即红。</p>
 */
class ExamPaperSeedConsistencyTest {

    /** 种子文件位于 main/resources,测试类路径可直接读 */
    private static final String SEED_RESOURCE = "/db/S03__seed_exam_2026.sql";

    private final ObjectMapper mapper = new ObjectMapper();

    private final ExamPaperValidator validator = new ExamPaperValidator();

    @Test
    @DisplayName("种子卷必须过自己的闸门")
    void seedPaperPassesValidator() {
        List<String> json = jsonLiterals(readSeed());
        assertThat(json).as("种子 INSERT 应含卷面与答案两段 JSON").hasSize(2);

        JsonNode content = mapper.readTree(json.get(0));
        JsonNode answerKey = mapper.readTree(json.get(1));

        assertThatCode(() -> validator.validate(content, answerKey, new BigDecimal("100.0")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("种子卷的答案编号按任务书 11.7 全局累加:1-15/16-20/21-35/36-45")
    void seedAnswerKeyUsesGlobalCardNumbering() {
        JsonNode answerKey = mapper.readTree(jsonLiterals(readSeed()).get(1));
        List<String> ranges = new ArrayList<>();
        for (JsonNode section : answerKey.path("sections")) {
            JsonNode items = section.path("items");
            if (!items.isArray() || items.isEmpty()) {
                continue;
            }
            ranges.add(section.path("part").asInt() + ":" + items.get(0).path("id").asInt()
                    + "-" + items.get(items.size() - 1).path("id").asInt() + "x" + items.size());
        }
        assertThat(ranges).containsExactly("1:1-15x15", "2:16-20x5", "3:21-35x15", "4:36-45x10");
    }

    /**
     * 读种子 SQL 文本。
     *
     * @return SQL 内容
     */
    private String readSeed() {
        try (InputStream in = getClass().getResourceAsStream(SEED_RESOURCE)) {
            assertThat(in).as("种子文件应在类路径上:" + SEED_RESOURCE).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("读取种子文件失败:" + e.getMessage(), e);
        }
    }

    /**
     * 从 SQL 里取出 {@code '{"sections":...}'} 两段字符串字面量,并按 MySQL 规则反转义。
     *
     * <p>不能按大括号配对直接切:种子里的引号写作 {@code \\"}(MySQL 反转义后才是 JSON 的 {@code \"}),
     * 直接扫会把字符串边界判错。也不能按 {@code ','} 切分:正文里的单引号在 SQL 中写作 {@code ''},
     * 拼起来恰好可能是 {@code ','}。所以先按 SQL 字面量找闭引号,再反转义交给 Jackson。</p>
     *
     * @param sql 种子 SQL 全文
     * @return 依次的 JSON 文本(第 1 段卷面,第 2 段答案)
     */
    private List<String> jsonLiterals(String sql) {
        List<String> found = new ArrayList<>();
        int pos = 0;
        while (true) {
            int open = sql.indexOf("'{\"sections\"", pos);
            if (open < 0) {
                return found;
            }
            int close = sqlLiteralEnd(sql, open);
            found.add(unescapeSqlLiteral(sql.substring(open + 1, close)));
            pos = close + 1;
        }
    }

    /**
     * 定位 MySQL 单引号字面量的闭引号。
     *
     * @param sql       SQL 全文
     * @param openQuote 开引号下标
     * @return 闭引号下标
     */
    private int sqlLiteralEnd(String sql, int openQuote) {
        for (int i = openQuote + 1; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '\'') {
                if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    i++;
                } else {
                    return i;
                }
            }
        }
        throw new IllegalStateException("种子 SQL 字符串字面量未闭合(下标 " + openQuote + ")");
    }

    /**
     * MySQL 字面量反转义:{@code \\}->{@code \},{@code \'}->{@code '},{@code ''}->{@code '}。
     *
     * @param raw 引号内的原始文本
     * @return 入库后的实际 JSON 文本
     */
    private String unescapeSqlLiteral(String raw) {
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\\' && i + 1 < raw.length()) {
                sb.append(raw.charAt(++i));
            } else if (c == '\'' && i + 1 < raw.length() && raw.charAt(i + 1) == '\'') {
                sb.append('\'');
                i++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
