package com.demo_system.service;

import com.demo_system.entity.KnowledgeBase;

import java.util.List;

public interface KnowledgeService {
    // 列表
    List<KnowledgeBase> listNotes(Long userId);
    // 创建笔记
    KnowledgeBase createNote(Long userId);
    // 修改笔记
    void updateNote(KnowledgeBase note);
    // 删除笔记
    void deleteNote(Long noteId);
}
