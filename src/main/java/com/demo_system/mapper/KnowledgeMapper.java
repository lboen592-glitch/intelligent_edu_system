package com.demo_system.mapper;

import com.demo_system.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeMapper {

    // 给 AI 用的知识库检索
    List<KnowledgeBase> searchByKeyword(@Param("userId") Long userId,
                                        @Param("keyword") String keyword,
                                        @Param("limit") int limit);

    // 按用户查全部笔记
    List<KnowledgeBase> listByUser(@Param("userId") Long userId);

    // 新建笔记
    void insert(KnowledgeBase note);

    // 更新笔记
    void update(KnowledgeBase note);

    // 删除笔记
    void delete(@Param("id") Long noteId);
}
