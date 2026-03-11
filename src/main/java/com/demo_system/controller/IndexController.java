package com.demo_system.controller;

import com.demo_system.entity.Response;
import com.demo_system.entity.StudyData;
import com.demo_system.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index")
public class IndexController {

    private final IndexService indexService;
    @GetMapping("/data_show")
    public Map<String,Object> getDataToShow(@RequestAttribute("userId") Long userId){
        Map <String,Object> result = new HashMap<>();
        StudyData studyData = indexService.getDataToShow(userId, LocalDate.now().toString());
        System.out.println("用户id"+ userId + "在首页请求数据获取");
        try{
            //请求今日数据
            result.put("success",true);
            result.put("message","获取成功");
            result.put("totalQuesNum",studyData.getTotalQuesNum());
            result.put("totalStudyTime",studyData.getTotalStudyTime());
            result.put("wrongQuesNum",studyData.getWrongQuesNum());
            result.put("date",studyData.getDate());
            double tempacc = studyData.getWrongQuesNum() == 0
                    ? 0.0
                    : 1.0 - (studyData.getWrongQuesNum() * 1.0 / studyData.getTotalQuesNum());
            result.put("accuracy",tempacc);
            System.out.println("用户"+ userId + "在首页请求今日数据成功");
            // 过去 7 天数据，复用getDataToShow方法
            List<Map<String, Object>> weekList = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate date = LocalDate.now().minusDays(i);
                StudyData d = indexService.getDataToShow(userId, date.toString());
                if (d != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", d.getDate());
                    map.put("totalQuesNum", d.getTotalQuesNum());
                    map.put("totalStudyTime", d.getTotalStudyTime());
                    map.put("wrongQuesNum", d.getWrongQuesNum());
                    tempacc = d.getWrongQuesNum() == 0
                            ? 0.0
                            : 1.0 - (d.getWrongQuesNum() * 1.0 / d.getTotalQuesNum());
                    map.put("accuracy",tempacc);
                    weekList.add(map);
                }
            }
            result.put("weekDataList", weekList);
        } catch (RuntimeException e){
            result.put("success",false);
            result.put("message","数据请求失败:" + e.getMessage());
        }
        return result;
    }
}
