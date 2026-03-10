package com.demo_system.service;

import com.demo_system.entity.LearnRecord;
import com.demo_system.entity.QuestionBase;
import com.demo_system.entity.QuestionPackage;

import java.util.List;

public interface PracticeService {
    List<QuestionPackage> getPracticePackages(Long userId);
    List<QuestionBase> getQuestions(Long packageId, String order, String count);
    List<LearnRecord> getRecord(long userId);

    List<QuestionBase> getRecordDetails(String questionIdSeq);

    void submitRecord(Long userId,LearnRecord record);
}
