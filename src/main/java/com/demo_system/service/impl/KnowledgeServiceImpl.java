package com.demo_system.service.impl;

import com.demo_system.entity.KnowledgeBase;
import com.demo_system.mapper.KnowledgeMapper;
import com.demo_system.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;

    //知识库列表
    @Override
    public List<KnowledgeBase> listNotes(Long userId) {
        if (userId == null) return Collections.emptyList();
        return knowledgeMapper.listByUser(userId);
    }

     //新建笔记
    @Override
    public KnowledgeBase createNote(Long userId) {;
        KnowledgeBase note = new KnowledgeBase();
        note.setUserId(userId);
        note.setNoteName("新建未命名笔记");
        knowledgeMapper.insert(note);
        return note;
    }
    //更新笔记
    @Override
    public void updateNote(KnowledgeBase note) {
        knowledgeMapper.update(note);
    }

    //删除笔记
    @Override
    public void deleteNote(Long noteId) {
        knowledgeMapper.delete(noteId);
    }

}
