package com.linguacraft.backend.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CSV 单词解析器测试(阶段 A6-①)。
 *
 * <p>重点是引号处理:释义列里必然出现逗号,朴素 split 会把一列切成两列 —— 这是本功能最可能的错法。</p>
 */
class CsvWordParserTest {

    private static final String HEADER = "spelling,phonetic,part_of_speech,meanings,level";

    @Test
    @DisplayName("引号内的逗号不切列")
    void keepsCommaInsideQuotes() {
        List<String> cells = CsvWordParser.splitLine("abandon,\"v. 离开, 放弃\",/əˈbændən/,\"放弃;抛弃\",core");

        assertThat(cells).containsExactly("abandon", "v. 离开, 放弃", "/əˈbændən/", "放弃;抛弃", "core");
    }

    @Test
    @DisplayName("双写引号表示一个引号字符")
    void unescapesDoubledQuotes() {
        List<String> cells = CsvWordParser.splitLine("\"say \"\"go\"\"\",core");

        assertThat(cells.get(0)).isEqualTo("say \"go\"");
    }

    @Test
    @DisplayName("表头列顺序任意,只要五个必需列齐")
    void acceptsReorderedHeader() {
        CsvWordParser.Result result = CsvWordParser.parse(
                "level,meanings,spelling,part_of_speech,phonetic\n"
                        + "core,放弃,abandon,v.,/əˈbændən/", 100);

        assertThat(result.errors()).isEmpty();
        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0).spelling()).isEqualTo("abandon");
        assertThat(result.rows().get(0).level()).isEqualTo("core");
    }

    @Test
    @DisplayName("缺必需列时整单拒绝并说明表头要求")
    void rejectsMissingHeaderColumn() {
        CsvWordParser.Result result = CsvWordParser.parse("spelling,phonetic,meanings,level\nabandon,/x/,放弃,core", 100);

        assertThat(result.rows()).isEmpty();
        assertThat(result.errors().get(0)).contains("CSV 表头必须包含");
    }

    @Test
    @DisplayName("释义按中英文分号拆分,等级归一为小写")
    void splitsMeaningsAndLowercasesLevel() {
        CsvWordParser.Result result = CsvWordParser.parse(
                HEADER + "\nabandon,/əˈbændən/,v.,\"放弃；抛弃;leave\",CORE", 100);

        CsvWordParser.Row row = result.rows().get(0);
        assertThat(row.meanings()).containsExactly("放弃", "抛弃", "leave");
        assertThat(row.level()).isEqualTo("core");
    }

    @Test
    @DisplayName("字段不完整的行进错误清单,但仍计入数据行总数")
    void recordsIncompleteRows() {
        CsvWordParser.Result result = CsvWordParser.parse(
                HEADER + "\nabandon,/x/,v.,放弃,core\nbroken,,,", 100);

        assertThat(result.rows()).hasSize(1);
        assertThat(result.dataRows()).isEqualTo(2);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).contains("第 3 行字段不完整");
    }

    @Test
    @DisplayName("超过行上限即截断并说明")
    void truncatesOverLimit() {
        StringBuilder csv = new StringBuilder(HEADER);
        for (int i = 0; i < 5; i++) {
            csv.append("\nw").append(i).append(",/x/,v.,放弃,core");
        }

        CsvWordParser.Result result = CsvWordParser.parse(csv.toString(), 2);

        assertThat(result.rows()).hasSize(2);
        assertThat(result.truncated()).isTrue();
        assertThat(result.errors().get(0)).contains("导入行数超过上限 2");
        // dataRows 报的是"看到的数据行",让调用方能说明还有多少没处理
        assertThat(result.dataRows()).isEqualTo(5);
    }

    @Test
    @DisplayName("BOM 与 CRLF 混排不影响解析")
    void toleratesBomAndCrlf() {
        CsvWordParser.Result result = CsvWordParser.parse(
                "﻿" + HEADER + "\r\nabandon,/x/,v.,放弃,core\r\n", 100);

        assertThat(result.errors()).isEmpty();
        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0).spelling()).isEqualTo("abandon");
    }

    @Test
    @DisplayName("空文件与全空行都只报错不抛异常")
    void handlesBlankInput() {
        assertThat(CsvWordParser.parse(null, 100).errors().get(0)).isEqualTo("文件为空");
        assertThat(CsvWordParser.parse("   \n\n  ", 100).errors().get(0)).isEqualTo("文件为空");
    }
}
