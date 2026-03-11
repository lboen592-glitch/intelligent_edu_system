package com.demo_system.service.impl;

import com.demo_system.entity.StudyData;
import com.demo_system.mapper.DataMapper;
import com.demo_system.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {
    private final DataMapper studyDataMapper;
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

}
