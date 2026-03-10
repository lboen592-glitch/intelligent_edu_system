package com.demo_system.entity;

import lombok.Data;

import java.time.LocalDateTime;


/**
 * 知识库笔记实体，对应 knowledge_base 表
 */
@Data
public class KnowledgeBase {
    private Long id;
    private Long userId;
    private String noteName;
    private String noteContent;
    private LocalDateTime creatTime;

}
