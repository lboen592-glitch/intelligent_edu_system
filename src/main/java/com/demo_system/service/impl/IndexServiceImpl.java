package com.demo_system.service.impl;

import com.demo_system.entity.KnowledgeBase;
import com.demo_system.entity.StudyData;
import com.demo_system.mapper.DataMapper;
import com.demo_system.mapper.KnowledgeMapper;
import com.demo_system.service.AiChatService;
import com.demo_system.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import com.hankcs.hanlp.HanLP; // 引入 HanLP
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final DataMapper studyDataMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final AiChatService aiChatService;

    @Override
    public StudyData getDataToShow(Long userId, String date){
        Assert.notNull(userId, "用户ID不能为空");
        Assert.hasText(date, "日期不能为空");
        StudyData studyData = studyDataMapper.selectByUserAndDate(
                userId,
                date
        );
        if (studyData == null){
            studyData = new StudyData(userId,date,0,0,0);
        }
        return studyData;
    }

    /*自动推荐，功能实现
     *查询用户的笔记列表，
     * 将随机一个笔记内容使用NLP库进行关键词提取，提取4个关键词
     * 将4个关键词丢给搜索引擎，返回结果
     * */
    @Override
    public List<Map<String,String>> getAutoRec(Long userId){
        // 1. 查询用户的全部笔记
        List<KnowledgeBase> notes = knowledgeMapper.listByUser(userId);
        // 判空：如果用户没有笔记，直接返回空列表
        if (notes == null || notes.isEmpty()) {
            return new ArrayList<>();
        }
        // 2. 随机选择一个笔记
        Random random = new Random();
        KnowledgeBase randomNote = notes.get(random.nextInt(notes.size()));
        // 获取笔记内容，如果内容为空则使用标题，防止空指针
        String contentToAnalyze = randomNote.getNoteContent();
        if (contentToAnalyze == null || contentToAnalyze.trim().isEmpty()) {
            contentToAnalyze = randomNote.getNoteName();
        }
        // 如果还是为空，无法提取，返回空
        if (contentToAnalyze == null || contentToAnalyze.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // 3. 使用 NLP 库提取 4 个关键词
        // HanLP.extractKeyword(文本, 数量) 会自动分析文本权重并返回关键词列表
        List<String> keywords = HanLP.extractKeyword(contentToAnalyze, 4);
        // 如果提取不到关键词（比如笔记全是符号），直接返回空
        if (keywords.isEmpty()) {
            return new ArrayList<>();
        }
        // 4. 将关键词拼接成搜索 Query (用空格隔开)
        String searchQuery = String.join(" ", keywords);
        // 打印日志方便调试
        System.out.println("自动推荐 - 选中笔记ID: " + randomNote.getId());
        System.out.println("自动推荐 - 提取关键词: " + searchQuery);
        // 5. 丢给搜索引擎，返回结果 (调用你提供的 searchOnline 方法)
        return aiChatService.searchOnline(searchQuery);
    }

}
