package com.demo_system.utils;

import com.demo_system.entity.KnowledgeBase;
import lombok.Data;

@Data
public class PptImportProgress {
    private int progress;           // 0~100
    private String status;          // INIT / PARSING / LLM / SAVING / DONE / ERROR
    private String message;         // 提示文案
    private KnowledgeBase note;     // 完成时返回生成的笔记
}
