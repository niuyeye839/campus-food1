/** 豆包 AI 服务，调用大模型接口实现美食推荐、笔记润色等功能 */
package com.campus.foodplatform.service;

import com.campus.foodplatform.dto.AiChatDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AiService {

    @Value("${app.ai.doubao-api-key}")
    private String apiKey;

    @Value("${app.ai.doubao-api-url}")
    private String apiUrl;

    @Value("${app.ai.doubao-model}")
    private String model;

    private final RestTemplate restTemplate;

    public AiService() {
        // 豆包推理模型响应较慢，设置足够长的超时
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(120000); // 2分钟
        this.restTemplate = new RestTemplate(factory);
    }

    public String chat(AiChatDTO dto) {
        String systemPrompt = buildSystemPrompt(dto.getAction());
        return callDoubao(systemPrompt, dto.getContent());
    }

    private String buildSystemPrompt(String action) {
        switch (action) {
            case "recommend":     return "你是武汉工程大学流芳校区的校园美食推荐助手。校区周边有泰塑公寓美食街、青春荟美食街、校内食堂等区域，涵盖正餐、火锅、小吃、饮品、烧烤等品类。请根据用户的需求，用简洁友好的语气推荐2-3个具体的店铺或美食类型，并说明推荐理由。回复控制在100字以内。";
            case "polish_note":   return "你是一个专业的美食探店文案写手，请帮用户润色以下探店笔记，使其更生动有趣，保留原意。";
            case "gen_title":     return "你是一个标题创作专家，根据以下探店笔记内容，生成5个吸引人的标题，每行一个。";
            case "find_shop":     return "你是一个校园美食推荐助手，根据用户描述的需求，推荐合适的美食类型和关键词，用于搜索店铺。";
            case "sort_discount": return "你是一个优惠信息整理助手，请将以下优惠信息按照折扣力度从大到小整理，并用简洁的格式输出。";
            default:              return "你是武汉工程大学流芳校区校园美食平台的智能助手，请回答用户关于校园美食的问题。";
        }
    }

    private String callDoubao(String systemPrompt, String userContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        // doubao-seed 推理模型不支持 system 角色，合并到 user 消息
        List<Map<String, Object>> input = new ArrayList<>();
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        List<Map<String, Object>> contentList = new ArrayList<>();
        Map<String, Object> textItem = new HashMap<>();
        textItem.put("type", "input_text");
        textItem.put("text", systemPrompt + "\n\n" + userContent);
        contentList.add(textItem);
        userMsg.put("content", contentList);
        input.add(userMsg);
        body.put("input", input);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            Map<?, ?> respBody = response.getBody();
            log.info("豆包API响应: {}", respBody);
            if (respBody != null) {
                List<?> output = (List<?>) respBody.get("output");
                if (output != null) {
                    for (Object item : output) {
                        Map<?, ?> entry = (Map<?, ?>) item;
                        if ("message".equals(entry.get("type"))) {
                            List<?> outContent = (List<?>) entry.get("content");
                            if (outContent != null && !outContent.isEmpty()) {
                                Map<?, ?> first = (Map<?, ?>) outContent.get(0);
                                Object text = first.get("text");
                                if (text != null) return text.toString();
                            }
                        }
                    }
                }
            }
            return "AI服务暂时不可用，请稍后重试";
        } catch (Exception e) {
            log.error("调用豆包AI失败: {}", e.getMessage(), e);
            return "调用失败：" + e.getMessage();
        }
    }
}
