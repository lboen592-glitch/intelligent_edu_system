package com.demo_system.mapper;

import com.demo_system.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeMapper {

    // 按用户查全部笔记
    List<KnowledgeBase> listByUser(@Param("userId") Long userId);

    // 新建笔记
    void insert(KnowledgeBase note);

    // 更新笔记
    void update(KnowledgeBase note);

    // 删除笔记
    void delete(@Param("id") Long noteId);
}
