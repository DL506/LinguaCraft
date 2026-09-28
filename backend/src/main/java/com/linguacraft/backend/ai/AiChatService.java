package com.linguacraft.backend.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.linguacraft.backend.common.BizException;
import com.linguacraft.backend.common.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * AI 对话服务:Spring AI ChatClient 的统一封装。
 *
 * <p>能力:单轮对话(带超时)与"输出 JSON"调用(自动剥离代码块围栏并解析)。
 * 超时/网络/服务错误统一抛业务异常(code 500),由上层决定降级策略
 * (作文评分失败降级 pending;出题失败返回可重试错误)。</p>
 *
 * <p>超时实现说明:ChatClient 同步调用运行在独立任务中,主流程限时等待;
 * 超时后底层请求无法强制中断(HTTP 客户端自行超时释放),对 MVP 可接受。</p>
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final ChatClient chatClient;

    private final ObjectMapper objectMapper;

    /** 调用超时(秒),lc.ai.timeout-seconds */
    @Value("${lc.ai.timeout-seconds:60}")
    private long timeoutSeconds;

    public AiChatService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * 单轮对话(带超时控制)。
     *
     * @param prompt 提示词
     * @return 模型返回文本
     * @throws BizException 超时或调用失败(code 500)
     */
    public String chat(String prompt) {
        try {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                    chatClient.prompt().user(prompt).call().content());
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("AI 调用超时(>{} 秒)", timeoutSeconds);
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "AI 服务响应超时");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 调用失败: {}", e.getMessage());
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "AI 服务暂时不可用");
        }
    }

    /**
     * 单轮对话并解析 JSON(自动剥离代码块围栏与多余文字)。
     *
     * @param prompt 提示词(要求模型只输出 JSON)
     * @return JSON 节点
     * @throws BizException 调用失败或返回非法 JSON(code 500)
     */
    public JsonNode chatForJson(String prompt) {
        String text = chat(prompt);
        String json = PromptTemplates.extractJson(text);
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.warn("AI 返回非合法 JSON(前 200 字符): {}",
                    text.length() > 200 ? text.substring(0, 200) : text);
            throw new BizException(ResultCode.SERVER_ERROR.getCode(), "AI 返回数据格式异常");
        }
    }
}