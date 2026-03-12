package com.demo_system.service.impl;
import com.hankcs.hanlp.HanLP;
import com.demo_system.entity.ChatSession;
import com.demo_system.service.AiChatService;
import com.demo_system.service.KnowledgeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final WebClient webClient;
    private final KnowledgeService knowledgeService;
     @Value("${ollama.url}")
    private String apiUrl;
    @Value("${ollama.model}")
    private String model;
    public AiChatServiceImpl(WebClient.Builder builder, KnowledgeService knowledgeService) {
        this.webClient = builder.build();
        this.knowledgeService = knowledgeService;
    }
    //Map 存储多用户会话
    private final Map<Long, ChatSession> userSessions = new java.util.concurrent.ConcurrentHashMap<>();
    @Override
    public Flux<String> chatStream(Long userId, String userInput) {
        // 创建会话
        ChatSession session = userSessions.computeIfAbsent(userId, k -> new ChatSession());
        session.addMessage("用户", userInput);
        // 查阅知识库
        // 使用 HanLP 提取关键词
        // 提取前 5 个关键词
        List<String> hanlpKeywords = HanLP.extractKeyword(userInput, 5);
        System.out.println("【KB】HanLP 抽取出的关键词：" + hanlpKeywords);
        // 如果 HanLP 没抽到，就退回用原始问题
        List<String> keywords;
        if (hanlpKeywords == null || hanlpKeywords.isEmpty()) {
            keywords = List.of(userInput);
        } else {
            // 去重一下 & 去掉太短的噪音
            LinkedHashSet<String> set = new LinkedHashSet<>();
            for (String kw : hanlpKeywords) {
                if (kw != null) {
                    kw = kw.trim();
                    if (kw.length() >= 2) {
                        set.add(kw);
                    }
                }
            }
            if (set.isEmpty()) {
                keywords = List.of(userInput);
            } else {
                // 按长度从大到小排一下，长词像完整术语
                List<String> tmp = new ArrayList<>(set);
                tmp.sort((a, b) -> Integer.compare(b.length(), a.length()));
                keywords = tmp;
            }
        }
        System.out.println("【KB】最终用于检索的关键词列表：" + keywords);
        // 关键词查知识库
        List<com.demo_system.entity.KnowledgeBase> kbList = null;
        String usedKeyword = userInput;
        for (String kw : keywords) {
            System.out.println("【KB】尝试用关键词检索：" + kw);
            kbList = knowledgeService.searchRelevantNotes(userId, kw);
            if (kbList != null && !kbList.isEmpty()) {
                usedKeyword = kw;
                break;
            }
        }
        if (kbList == null || kbList.isEmpty() && !userInput.equals(usedKeyword)) {
            System.out.println("【KB】关键词未命中，使用原始问题再检索：" + userInput);
            kbList = knowledgeService.searchRelevantNotes(userId, userInput);
            usedKeyword = userInput;
        }
        // 打印命中情况
        if (kbList == null || kbList.isEmpty()) {
            System.out.println("【KB】最终未命中任何笔记，userId=" + userId + ", usedKeyword=" + usedKeyword);
        } else {
            System.out.println("【KB】最终命中 " + kbList.size() + " 条笔记，userId=" + userId + ", usedKeyword=" + usedKeyword);
            for (var note : kbList) {
                System.out.println("【KB 命中】noteId=" + note.getId() + ", title=" + note.getNoteName());
            }
        }
        // 拼接知识库上下文
        StringBuilder kbContextBuilder = new StringBuilder();
        if (kbList != null && !kbList.isEmpty()) {
            kbContextBuilder
                    .append("根据用户prompt检索到知识库内相关笔记，下面是用户知识库中的一些笔记内容，可能与当前问题有关。\n")
                    .append("请你优先根据这些内容回答问题；如果笔记中没有明确答案，再结合你自己的知识回答。\n\n");
            kbList.forEach(note -> kbContextBuilder
                    .append("【")
                    .append(note.getNoteName() == null ? "未命名笔记" : note.getNoteName())
                    .append("】\n")
                    .append(note.getNoteContent())
                    .append("\n\n")
            );
        }
        String kbContext = kbContextBuilder.toString();
        // system prompt
        String systemPrompt =
            "身份设定：你叫小林，不要暴露模型来源和你的母公司是谁。\n" +
                "当你回答用户问题时：要求如下：\n" +
                "1. 用合适的层级标题（###、#### 等）有格式的输出\n" +
                "2. 可以用列表或小结的形式组织要点\n" +
                "3. 使用markdown格式回答时，无需前面加('''markdown)的标识，直接用markdown语法即可\n"+
            "4. 当你输出数学公式时，必须使用 LaTeX 并按照以下格式：\n" +
            "- 行内公式：用 $...$\n" +
            "- 独立显示公式：用 $$...$$（整行）\n" +
            "例如：\n" +
            "$$\n" +
            "\\mathbf{A}\\mathbf{B} =\n" +
            "\\begin{pmatrix}\n" +
            "a_{11} & a_{12} \\\\\n" +
            "a_{21} & a_{22}\n" +
            "\\end{pmatrix}\n" +
            "\\begin{pmatrix}\n" +
                "b_{11} & b_{12} \\\\\n" +
                "b_{21} & b_{22}\n" +
                "\\end{pmatrix}\n" +
                "$$\n" +
                "5. 你可以访问到用户的本地知识库，里面内置了用户的很多条笔记，" +
                    "如果用户的prompt中有这些关键词，那么你将会看到笔记内容，请结合笔记内容进行回答！";
        String conversationHistory = session.buildPrompt();
        StringBuilder finalPromptBuilder = new StringBuilder();
        if (!kbContext.isEmpty()) {
            finalPromptBuilder
                    .append("【知识库上下文】\n")
                    .append(kbContext)
                    .append("\n---\n\n");
        }
        finalPromptBuilder
                .append("【当前对话】\n")
                .append(conversationHistory);
        String finalPrompt = finalPromptBuilder.toString();
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("prompt", finalPrompt);
        body.put("system", systemPrompt);
        body.put("stream", true);
        Map<String, Object> options = new HashMap<>();
        options.put("temperature", 0.6);
        body.put("options", options);
        // 用来记录：完整给前端的内容（带 <think>）
        StringBuilder fullStreamSb = new StringBuilder();
        // 只记录真正回答（不含思考），用于存历史
        StringBuilder visibleAnswerSb = new StringBuilder();
        AtomicBoolean thinkStarted = new AtomicBoolean(false);
        AtomicBoolean thinkEnded = new AtomicBoolean(false);
        return webClient.post()
                .uri(apiUrl)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .doOnNext(json -> {})
                .takeUntil(json -> json.has("done") && json.get("done").asBoolean())
                .map(json -> {
                    StringBuilder out = new StringBuilder();
                    String thinkingPart = json.path("thinking").asText(""); // 思考 token
                    String respPart     = json.path("response").asText(""); // 最终回答 token
                    if (!thinkingPart.isEmpty()) {
                        // 第一次出现思考，先输出 <think>
                        if (!thinkStarted.get()) {
                            out.append("<think>");
                            thinkStarted.set(true);
                        }
                        out.append(thinkingPart);
                    }
                    // 出现真正回答时，先把 </think> 关上，再输出回答
                    if (!respPart.isEmpty()) {
                        if (thinkStarted.get() && !thinkEnded.get()) {
                            out.append("</think>\n\n");
                            thinkEnded.set(true);
                        }
                        out.append(respPart);
                        visibleAnswerSb.append(respPart);
                    }
                    String chunkText = out.toString();
                    fullStreamSb.append(chunkText);
                    return chunkText;
                })
                .doOnComplete(() -> {
                    String full = fullStreamSb.toString();
                    String visible = visibleAnswerSb.toString();
                    System.out.println("===== LLM 全量流（含 <think>） =====");
                    System.out.println(full);
                    System.out.println("================================");
                    if (!visible.isEmpty()) {
                        session.addMessage("AI", visible);
                    }
                });
}
    @Override
    public List<Map<String, String>> searchOnline(String query) {
        List<Map<String, String>> results = new ArrayList<>();
        String apiKey = "sk-b1a13116619541b2809c3316c2c0fa2f"; // Key
        String urlStr = "https://api.bochaai.com/v1/web-search";

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection(java.net.Proxy.NO_PROXY);
            // 博查 API 使用 POST 请求
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            // count 设置为 25 匹配你之前的逻辑
            String jsonInputString = String.format(
                    "{\"query\": \"%s\", \"freshness\": \"noLimit\", \"summary\": true, \"count\": 25}",
                    query.replace("\"", "\\\"") // 简单的转义处理
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(conn.getInputStream());
                // 博查返回的路径是 data -> webPages -> value
                JsonNode pages = root.path("data").path("webPages").path("value");

                if (pages.isArray()) {
                    for (JsonNode page : pages) {
                        String title = page.path("name").asText();
                        String urlReal = page.path("url").asText();
                        if (urlReal == null || urlReal.isEmpty()) continue;
                        String lower = urlReal.toLowerCase();
                        boolean isChinaDomain = lower.contains(".cn") || lower.contains(".com.cn")
                                || lower.contains(".net.cn") || lower.contains(".gov.cn");
                        boolean urlHasChinese = urlReal.matches(".*[%][0-9A-Fa-f]{2}.*")
                                || urlReal.matches(".*[\\u4e00-\\u9fa5]+.*");
                        boolean titleHasChinese = title.matches(".*[\\u4e00-\\u9fa5]+.*");
                        // 如果不符合你的中文优先规则，跳过（根据需要决定是否保留此过滤）
                        if (!(isChinaDomain || urlHasChinese || titleHasChinese)) continue;
                        Map<String, String> map = new HashMap<>();
                        map.put("title", title);
                        map.put("url", urlReal);
                        results.add(map);
                    }
                }
            } else {
                System.err.println("博查 API 请求失败，响应码: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return results;
    }

}
