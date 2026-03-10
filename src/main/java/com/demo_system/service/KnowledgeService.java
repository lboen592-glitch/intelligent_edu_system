package com.demo_system.service;

import com.demo_system.entity.KnowledgeBase;
import com.demo_system.entity.Response;
import com.demo_system.utils.PptImportProgress;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeService {
    List<KnowledgeBase> searchRelevantNotes(Long userId, String question);
    // 列表
    List<KnowledgeBase> listNotes(Long userId);
    // 创建笔记
    KnowledgeBase createNote(Long userId);
    // 修改笔记
    void updateNote(KnowledgeBase note);
    // 删除笔记
    void deleteNote(Long noteId);
    // 开始异步导入 PPT，返回一个 taskId（Controller 已经生成好传进来）
    void importPptAsync(MultipartFile file, Long userId, String taskId);
    // 前端轮询用：根据 taskId 查询进度
    PptImportProgress getPptProgress(String taskId);
}
