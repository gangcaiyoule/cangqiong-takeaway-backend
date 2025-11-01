package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sky.entity.Orders.COMPLETED;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>(); // 日期列表
        List<Double> turnoverList = new ArrayList<>(); //营业额列表
        LocalDate date = begin;
        //构造日期
        while (!date.isAfter(end)) {
            dateList.add(date);
            //查询营业额
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.atTime(LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", COMPLETED);
            Double turnover = orderMapper.sumByDate(map);
            if (turnover == null) {
                turnover = 0.0;
            }
            turnoverList.add(turnover);
            date = date.plusDays(1);
        }
        //转换成字符串格式
        String dateListString = dateList.stream().map(LocalDate::toString).collect(Collectors.joining(","));
        String turnoverListString = turnoverList.stream().map(String::valueOf).collect(Collectors.joining(","));
        TurnoverReportVO turnoverReportVO = TurnoverReportVO
                .builder()
                .dateList(dateListString)
                .turnoverList(turnoverListString)
                .build();
        return turnoverReportVO;
    }
}
