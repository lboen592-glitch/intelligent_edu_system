package com.demo_system.service;

import com.demo_system.entity.StudyData;

import java.util.List;
import java.util.Map;

public interface IndexService {
    public StudyData getDataToShow(Long userId,String date);

    public List<Map<String,String>> getAutoRec(Long userId);
}
