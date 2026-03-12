package com.demo_system.controller;
import com.demo_system.entity.KnowledgeBase;
import com.demo_system.entity.Response;
import com.demo_system.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeService knowledgeService;
    @GetMapping("/list")
    public Response listNotes(@RequestHeader("X-User-Id") Long userId) {
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
    public Response createNote(@RequestHeader("X-User-Id") Long userId) {
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
    public Response updateNote(@RequestHeader("X-User-Id") Long userId, @RequestBody KnowledgeBase note) {
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
    public Response deleteNote(@RequestHeader("X-User-Id") Long userId, @PathVariable Long noteId) {
        System.out.println("用户id " + userId + " 请求删除笔记 id=" + noteId);
        try {
            knowledgeService.deleteNote(noteId);
            return Response.ok("删除笔记成功", null);
        } catch (Exception e) {
            return Response.fail("删除笔记失败: " + e.getMessage());
        }
    }


}
