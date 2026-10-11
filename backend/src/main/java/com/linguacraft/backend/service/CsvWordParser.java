package com.linguacraft.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CSV 单词解析器(阶段 A6-①):把上传的文本按 RFC 4180 规则切成单词行。
 *
 * <p>不引入 Apache Commons CSV / OpenCSV,自己实现<b>带引号状态机</b>的单行切分,
 * 因为词义里必然出现逗号(如 {@code "放弃;抛弃"} 之外还有 {@code "v. 离开, 放弃"}),
 * 用 {@code String.split(",")} 会把一列切成两列 —— 这是本功能最容易出的错。</p>
 *
 * <p>约定:第一行是表头,必须含 {@code spelling,phonetic,part_of_speech,meanings,level} 五列(可多不可少,
 * 顺序任意,大小写不敏感);{@code meanings} 列内部用分号(中英文皆可)分隔多条释义。</p>
 */
public final class CsvWordParser {

    /** 必需表头列 */
    private static final List<String> REQUIRED_COLUMNS =
            List.of("spelling", "phonetic", "part_of_speech", "meanings", "level");

    private CsvWordParser() {
    }

    /**
     * 一行解析结果。
     *
     * @param line         CSV 里的行号(从 1 开始,含表头)
     * @param spelling     拼写
     * @param phonetic     音标
     * @param partOfSpeech 词性
     * @param meanings     释义列表
     * @param level        等级
     */
    public record Row(int line, String spelling, String phonetic, String partOfSpeech, List<String> meanings,
                      String level) {
    }

    /**
     * 整份文件的解析结果。
     *
     * @param rows         结构合规的数据行
     * @param errors       行级错误说明(截断与整单拒绝的说明也写在这里,便于只读报告)
     * @param truncated    是否因超过行数上限而被截断
     * @param dataRows     读到的数据行总数(含被拒的行,不含表头)
     * @param rejectedRows 因字段不完整被丢掉的行数(计入调用方的 skippedInvalid)
     * @param fileError    整单级错误(文件为空 / 表头缺列);非空时一行都不会入库,须由调用方直接拒绝
     */
    public record Result(List<Row> rows, List<String> errors, boolean truncated, int dataRows,
                         int rejectedRows, String fileError) {
    }

    /**
     * 解析 CSV 文本。
     *
     * @param text    文件全文(UTF-8 解码后)
     * @param maxRows 数据行上限(超出即截断并在错误里说明)
     * @return 解析结果;表头缺列时 rows 为空、errors 首条为表头错误、fileError 同值
     */
    public static Result parse(String text, int maxRows) {
        List<Row> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        if (text == null || text.isBlank()) {
            errors.add("文件为空");
            return new Result(rows, errors, false, 0, 0, "文件为空");
        }

        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        if (normalized.startsWith("﻿")) {
            normalized = normalized.substring(1);
        }
        List<List<String>> lines = new ArrayList<>();
        for (String raw : normalized.split("\n", -1)) {
            if (raw.isBlank()) {
                continue;
            }
            lines.add(splitLine(raw));
        }
        if (lines.isEmpty()) {
            errors.add("文件为空");
            return new Result(rows, errors, false, 0, 0, "文件为空");
        }

        int[] columnIndexes = mapHeader(lines.get(0));
        if (columnIndexes == null) {
            String reason = "CSV 表头必须包含 spelling,phonetic,part_of_speech,meanings,level";
            errors.add(reason);
            return new Result(rows, errors, false, 0, 0, reason);
        }

        boolean truncated = false;
        int rejected = 0;
        int dataRows = Math.max(0, lines.size() - 1);
        // 表头占第 1 行,数据行从第 2 行起
        for (int i = 1; i < lines.size(); i++) {
            if (rows.size() >= maxRows) {
                errors.add("导入行数超过上限 " + maxRows + ",已截断,请拆分文件后重试");
                truncated = true;
                break;
            }
            List<String> cells = lines.get(i);
            String spelling = cellAt(cells, columnIndexes[0]);
            String phonetic = cellAt(cells, columnIndexes[1]);
            String partOfSpeech = cellAt(cells, columnIndexes[2]);
            String meaningsRaw = cellAt(cells, columnIndexes[3]);
            String level = cellAt(cells, columnIndexes[4]);

            List<String> meanings = splitMeanings(meaningsRaw);
            if (spelling.isEmpty() || phonetic.isEmpty() || partOfSpeech.isEmpty()
                    || meanings.isEmpty() || level.isEmpty()) {
                rejected++;
                errors.add("第 " + (i + 1) + " 行字段不完整(拼写/音标/词性/释义/等级都不可为空)");
                continue;
            }
            rows.add(new Row(i + 1, spelling, phonetic, partOfSpeech, meanings, level.toLowerCase(Locale.ROOT)));
        }
        return new Result(rows, errors, truncated, dataRows, rejected, null);
    }

    /**
     * 单行切分:支持双引号包裹、转义双引号("")与引号内的逗号。
     *
     * @param line 原始行
     * @return 单元格列表
     */
    static List<String> splitLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    // "" 表示一个引号字符;否则退出引号态
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                cells.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cells.add(current.toString().trim());
        return cells;
    }

    /**
     * 把表头映射到必需列的下标;缺任一必需列返回 null。
     *
     * @param header 表头单元格
     * @return 五个必需列的下标,或 null
     */
    private static int[] mapHeader(List<String> header) {
        List<String> normalized = new ArrayList<>();
        for (String cell : header) {
            normalized.add(cell.toLowerCase(Locale.ROOT).replace("\"", "").trim());
        }
        int[] indexes = new int[REQUIRED_COLUMNS.size()];
        for (int i = 0; i < REQUIRED_COLUMNS.size(); i++) {
            indexes[i] = normalized.indexOf(REQUIRED_COLUMNS.get(i));
            if (indexes[i] < 0) {
                return null;
            }
        }
        return indexes;
    }

    /**
     * 取单元格(越界返回空串)。
     *
     * @param cells 单元格列表
     * @param index 下标
     * @return 值
     */
    private static String cellAt(List<String> cells, int index) {
        return index < cells.size() ? cells.get(index) : "";
    }

    /**
     * 释义列拆分:支持中英文分号,单个逗号分隔也接受(常见手工编辑习惯)。
     *
     * @param raw 原始单元格
     * @return 释义列表
     */
    private static List<String> splitMeanings(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        for (String part : raw.split("[;；]")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
