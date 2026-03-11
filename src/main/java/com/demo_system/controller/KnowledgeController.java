package com.demo_system.controller;
import com.demo_system.entity.KnowledgeBase;
import com.demo_system.entity.Response;
import com.demo_system.service.KnowledgeService;
import com.demo_system.utils.PptImportProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeService knowledgeService;
    @GetMapping("/list")
    public Response listNotes(@RequestAttribute("userId") Long userId) {
        System.out.println("用户id " + userId + " 请求知识库列表");
        try
        {
            List<KnowledgeBase> list = knowledgeService.listNotes(userId);
            return Response.ok("获取笔记列表成功", list);
        } catch (RuntimeException e) {
            return Response.fail("获取笔记列表失败: " + e.getMessage());
        }
    }

    /**
     * 新建自定义笔记
     * 前端传：title -> noteName
     */
    @PostMapping("/create")
    public Response createNote(@RequestAttribute("userId") Long userId) {
        System.out.println("用户id " + userId + " 请求创建笔记");
        try {
            KnowledgeBase saved = knowledgeService.createNote(userId);
            return Response.ok("创建笔记成功", saved);
        } catch (Exception e) {
            return Response.fail("创建笔记失败: " + e.getMessage());
        }
    }
    /**
     * 更新笔记内容
     * 需要传 id, noteName, noteContent
     */
    @PutMapping("/update")
    public Response updateNote(@RequestAttribute("userId") Long userId, @RequestBody KnowledgeBase note) {
        System.out.println("用户id " + userId + " 请求更新笔记 id=" + note.getId());
        try {
            knowledgeService.updateNote(note);
            return Response.ok("更新笔记成功", null);
        } catch (Exception e) {
            return Response.fail("更新笔记失败: " + e.getMessage());
        }
    }
    /**
     * 删除笔记
     */
    @DeleteMapping("/delete/{noteId}")
    public Response deleteNote(@RequestAttribute("userId") Long userId, @PathVariable Long noteId) {
        System.out.println("用户id " + userId + " 请求删除笔记 id=" + noteId);
        try {
            knowledgeService.deleteNote(noteId);
            return Response.ok("删除笔记成功", null);
        } catch (Exception e) {
            return Response.fail("删除笔记失败: " + e.getMessage());
        }
    }

    //上传ppt文件并请求解析
    @PostMapping("/upload-ppt")
    public Response uploadPpt(@RequestAttribute("userId") Long userId, @RequestPart("file") MultipartFile file) {
        System.out.println("用户id " + userId + " 上传PPT生成笔记，文件名=" + file.getOriginalFilename());
        try {
            String taskId = java.util.UUID.randomUUID().toString();
            // 启动异步任务
            knowledgeService.importPptAsync(file, userId, taskId);
            // 立即返回 taskId，前端拿着它去轮询进度
            Map<String, Object> data = new HashMap<>();
            data.put("taskId", taskId);
            return Response.ok("PPT已上传，正在后台解析", data);
        } catch (Exception e) {
            return Response.fail("PPT解析任务启动失败: " + e.getMessage());
        }
    }

    // 查询解析任务的进度
    @GetMapping("/upload-ppt/progress/{taskId}")
    public Response getPptProgress(@RequestAttribute("userId") Long userId, @PathVariable String taskId) {
        PptImportProgress progress = knowledgeService.getPptProgress(taskId);
        if (progress == null) {
            return Response.fail("任务不存在或已过期");
        }
        return Response.ok("查询成功", progress);
    }
}
