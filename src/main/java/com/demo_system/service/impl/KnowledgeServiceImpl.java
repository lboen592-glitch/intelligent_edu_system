package com.demo_system.service.impl;

import com.demo_system.entity.KnowledgeBase;
import com.demo_system.entity.Response;
import com.demo_system.mapper.KnowledgeMapper;
import com.demo_system.service.KnowledgeService;
import com.demo_system.utils.PptImportProgress;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    private final KnowledgeMapper knowledgeMapper;
    private final WebClient webClient;   // 用来调 Ollama
    @Value("${ollama.url}")
    private String apiUrl;
    @Value("${ollama.model}")
    private String ollamaModel;  // 注意这里ollamaModel，避免和别的冲突
    // 新增：保存所有 PPT 导入任务进度
    private final Map<String, PptImportProgress> pptTaskMap = new java.util.concurrent.ConcurrentHashMap<>();

    public KnowledgeServiceImpl(KnowledgeMapper knowledgeMapper,
                                WebClient.Builder builder) {
        this.knowledgeMapper = knowledgeMapper;
        this.webClient = builder.build();
    }
    /**
     * 给 AI 用的关键字搜索
     */
    @Override
    public List<KnowledgeBase> searchRelevantNotes(Long userId, String question) {
        if (userId == null || question == null || question.isBlank()) {
            return Collections.emptyList();
        }
        return knowledgeMapper.searchByKeyword(userId, question, 5);
    }

    /**
     * 知识库列表（按类型筛选）
     */
    @Override
    public List<KnowledgeBase> listNotes(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return knowledgeMapper.listByUser(userId);
    }

    /**
     * 新建笔记
     */
    @Override
    public KnowledgeBase createNote(Long userId) {;
        KnowledgeBase note = new KnowledgeBase();
        note.setUserId(userId);
        note.setNoteName("新建未命名笔记");
        knowledgeMapper.insert(note);
        return note;
    }
    /**
     * 更新笔记
     */
    @Override
    public void updateNote(KnowledgeBase note) {
        knowledgeMapper.update(note);
    }

    /**
     * 删除笔记
     */
    @Override
    public void deleteNote(Long noteId) {
        knowledgeMapper.delete(noteId);
    }
    /**
     * 上传 PPT，解析并生成笔记：
     * 1）用 Apache POI 提取 PPT 中的所有文字
     * 2）调用本地大模型，总结为结构化 Markdown 笔记
     * 3）保存到 knowledge_base 表
     */
    @Override
    public void importPptAsync(MultipartFile file, Long userId, String taskId) {
        // 创建并初始化任务进度
        PptImportProgress progress = new PptImportProgress();
        progress.setProgress(0);
        progress.setStatus("INIT");
        progress.setMessage("任务已创建，等待开始解析");
        pptTaskMap.put(taskId, progress);
        // 用新线程来跑耗时任务
        new Thread(() -> {
            try {
                progress.setStatus("PARSING");
                progress.setMessage("正在解析 PPT 文本...");
                progress.setProgress(5);  // 5% 起步
                String originalName = file.getOriginalFilename();
                String title;
                if (originalName != null) {
                    title = originalName.replaceAll("\\.(pptx|ppt)$", "");
                } else {
                    title = "PPT笔记";
                }
                // PPT 提取文字（进度：按“当前页 / 总页数”更新）
                String rawText = extractTextFromPptWithProgress(file, progress);
                if (rawText == null || rawText.isBlank()) {
                    progress.setStatus("ERROR");
                    progress.setMessage("未能从 PPT 中提取到任何文字");
                    progress.setProgress(100);
                    return;
                }
                System.out.println("【PPT导入】原始文本长度 = " + rawText.length());
                progress.setProgress(40);
                progress.setMessage("PPT 文本解析完成，准备调用大模型整理...");
                // 流式调用大模型总结（按收到的 chunk 实时更新进度）
                progress.setStatus("LLM");
                progress.setMessage("正在整理学习笔记...");
                String noteContent = summarizePptTextStreamWithProgress(rawText, progress);
                if (noteContent == null || noteContent.isBlank()) {
                    progress.setStatus("ERROR");
                    progress.setMessage("大模型未返回有效的笔记内容");
                    progress.setProgress(100);
                    return;
                }
                System.out.println("【PPT导入】LLM 笔记长度 = " + noteContent.length());
                progress.setProgress(90);
                progress.setMessage("笔记内容已生成，正在保存...");
                // 保存到数据库
                progress.setStatus("SAVING");
                LocalDateTime now = LocalDateTime.now();
                KnowledgeBase note = new KnowledgeBase();
                note.setUserId(userId);
                note.setNoteName(title);
                note.setNoteContent(noteContent);
                note.setCreatTime(now);
                knowledgeMapper.insert(note);
                // 完成
                progress.setStatus("DONE");
                progress.setMessage("解析完成，笔记已生成");
                progress.setProgress(100);
                progress.setNote(note);
                System.out.println("【PPT导入】任务完成，noteId=" + note.getId());
            } catch (Exception e) {
                e.printStackTrace();
                progress.setStatus("ERROR");
                progress.setMessage("任务失败：" + e.getMessage());
                progress.setProgress(100);
            }
        }).start();
    }
    @Override
    public PptImportProgress getPptProgress(String taskId) {
        return pptTaskMap.get(taskId);
    }

    /**
     * 带进度的 PPT 文本提取：进度区间 5% ~ 40%
     */
    private String extractTextFromPptWithProgress(MultipartFile file,
                                                  PptImportProgress progress) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null) fileName = "";
        String lower = fileName.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        int start = 5;
        int end = 40;
        if (lower.endsWith(".pptx")) {
            // Office 2007+ PPTX
            try (InputStream is = file.getInputStream();
                 XMLSlideShow slideShow = new XMLSlideShow(is)) {
                List<XSLFSlide> slides = slideShow.getSlides();
                int total = slides.isEmpty() ? 1 : slides.size();
                for (int i = 0; i < total; i++) {
                    XSLFSlide slide = slides.get(i);
                    sb.append("【幻灯片 ").append(i + 1).append("】\n");
                    for (XSLFShape shape : slide.getShapes()) {
                        if (shape instanceof XSLFTextShape textShape) {
                            String text = textShape.getText();
                            if (text != null && !text.isBlank()) {
                                sb.append(text.trim()).append("\n");
                            }
                        }
                    }
                    sb.append("\n");
                    // 当前页对应的解析进度
                    int p = start + (int) (((i + 1) / (double) total) * (end - start));
                    progress.setProgress(p);
                    progress.setMessage("正在解析 PPT 文本，第 " + (i + 1) + "/" + total + " 页...");
                }
            }
        } else if (lower.endsWith(".ppt")) {
            // 老版本 PPT
            try (InputStream is = file.getInputStream();
                 HSLFSlideShow slideShow = new HSLFSlideShow(is)) {
                List<HSLFSlide> slides = slideShow.getSlides();
                int total = slides.isEmpty() ? 1 : slides.size();
                for (int i = 0; i < total; i++) {
                    HSLFSlide slide = slides.get(i);
                    sb.append("【幻灯片 ").append(i + 1).append("】\n");
                    for (HSLFShape shape : slide.getShapes()) {
                        if (shape instanceof HSLFTextShape textShape) {
                            String text = textShape.getText();
                            if (text != null && !text.isBlank()) {
                                sb.append(text.trim()).append("\n");
                            }
                        }
                    }
                    sb.append("\n");
                    int p = start + (int) (((i + 1) / (double) total) * (end - start));
                    progress.setProgress(p);
                    progress.setMessage("正在解析 PPT 文本，第 " + (i + 1) + "/" + total + " 页...");
                }
            }
        } else {
            throw new IllegalArgumentException("暂时只支持 .ppt 和 .pptx 文件");
        }
        // 解析结束，兜底一下
        progress.setProgress(end);
        progress.setMessage("PPT 文本解析完成，准备自动整理笔记...");
        return sb.toString();
    }
    /**
     * 流式调用本地大模型，把 PPT 文本整理成 Markdown 笔记
     * 进度规则：
     * - 进入时假定解析阶段已经到 40%
     * - 每收到一个 response chunk，就把 progress+1，最多加到 90%
     * - 完成后由调用方把 progress 提到 90/100
     */
    private String summarizePptTextStreamWithProgress(String pptText,
                                                      PptImportProgress progress) {
        // 为了避免一次性给模型太长的上下文，这里简单做一个截断
        int maxLen = 15000;
        String clipped = pptText.length() > maxLen
                ? pptText.substring(0, maxLen)
                : pptText;

        String systemPrompt = "你是一个擅长整理课件的智能助教，名字叫小智。\n" +
                "现在给你的是从 PPT 幻灯片中直接提取出来的文本，可能顺序和排版有点乱。\n" +
                "请你根据这些内容，整理成一篇结构清晰、适合复习的中文的详细的Markdown格式学习笔记。\n" +
                "要求：\n" +
                "1. 使用中文回答。\n" +
                "2. 用合适的层级标题（###、#### 等）拆分知识点。\n" +
                "3. 用列表或小结的形式组织要点，不要简单复制粘贴原文。\n" +
                "4. 可以适当补充必要的过渡说明，但不要胡编乱造与 PPT 完全无关的内容。\n" +
                "5. 使用markdown格式回答时，无需前面加('''markdown)的标识，直接用markdown语法即可";

        String userPrompt = "下面是从 PPT 中提取出的全部文字，请按照上面的要求整理成学习笔记：\n\n" + clipped;
        Map<String, Object> body = new HashMap<>();
        body.put("model", ollamaModel);
        body.put("system", systemPrompt);
        body.put("prompt", userPrompt);
        body.put("stream", true); // 流式
        // 用来拼接最终的可见回答
        StringBuilder visibleAnswerSb = new StringBuilder();
        // ==== 进度相关：从 40% ~ 90%，按生成字数线性推进 ====
        // 预估整篇笔记的字数：一般在 1200 字左右
        final int estimatedTotalChars = 1200;
        // 起始进度，兼容外面可能已经有的进度
        final int baseProgressStart = Math.max(progress.getProgress(), 40);
        final int baseProgressEnd   = 90;
        // 记录当前已经设置到多少进度了
        AtomicInteger llmProgress = new AtomicInteger(baseProgressStart);
        AtomicReference<Throwable> errorRef = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);
        webClient.post()
                .uri(apiUrl)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .doOnNext(json -> {
                    String thinkingPart = json.path("thinking").asText("");
                    String respPart     = json.path("response").asText("");
                    if (!respPart.isEmpty()) {
                        visibleAnswerSb.append(respPart);
                        // 当前已生成的总字数
                        int generatedLen = visibleAnswerSb.length();
                        // 生成进度比例：[0,1]
                        double ratio = (double) generatedLen / estimatedTotalChars;
                        if (ratio > 1.0) {
                            ratio = 1.0;   // 最多到 100%
                        }
                        // 把比例映射到 [baseProgressStart, baseProgressEnd]
                        int targetProgress = baseProgressStart +
                                (int) Math.round((baseProgressEnd - baseProgressStart) * ratio);
                        int prev = llmProgress.get();
                        // 只允许单向递增
                        if (targetProgress > prev && targetProgress <= baseProgressEnd) {
                            if (llmProgress.compareAndSet(prev, targetProgress)) {
                                progress.setProgress(targetProgress);
                                progress.setMessage("正在整理学习笔记（已生成约 "
                                        + generatedLen + " 字）...");
                            }
                        }
                    }
                })
                .doOnError(e -> {
                    errorRef.set(e);
                    latch.countDown();
                })
                .doOnComplete(() -> {
                    latch.countDown();
                })
                .subscribe();
        try {
            boolean finished = latch.await(10, TimeUnit.MINUTES);
            if (!finished) {
                throw new RuntimeException("请求超时，请重试");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("请求异常中断", e);
        }
        if (errorRef.get() != null) {
            throw new RuntimeException("总结 PPT 失败：" + errorRef.get().getMessage(), errorRef.get());
        }
        // 如果进度还没到 90%，提一下
        if (progress.getProgress() < 90) {
            progress.setProgress(90);
        }
        progress.setMessage("正在生成笔记内容...");
        return visibleAnswerSb.toString();
    }

}
