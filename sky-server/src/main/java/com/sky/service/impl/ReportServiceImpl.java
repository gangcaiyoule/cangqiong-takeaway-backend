package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

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

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>(); // 日期列表
        List<Integer> totalUserList = new ArrayList<>(); // 总用户两列表
        List<Integer> newUserList = new ArrayList<>(); //新增用户数列表

        LocalDate date = begin;

        while (!date.isAfter(end)) {
            dateList.add(date);
            //构建时间区间
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();

            Integer total = userMapper.countTotalUserUntil(endTime);
            Integer newUser = userMapper.countNewUserByDate(beginTime, endTime);

            totalUserList.add(total == null ? 0 : total);
            newUserList.add(newUser == null ? 0 : newUser);

            date = date.plusDays(1);
        }
        //装换成字符串
        String dateListStr = dateList.stream().map(LocalDate::toString).collect(Collectors.joining(","));
        String totalListStr = totalUserList.stream().map(String::valueOf).collect(Collectors.joining(","));
        String newUserListStr = newUserList.stream().map(String::valueOf).collect(Collectors.joining(","));
        UserReportVO userReportVO = UserReportVO
                .builder()
                .dateList(dateListStr)
                .totalUserList(totalListStr)
                .newUserList(newUserListStr)
                .build();
        return userReportVO;
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>(); // 日期列表
        List<Integer> orderCountList = new ArrayList<>(); //订单数列表
        List<Integer> validOrderCountList = new ArrayList<>(); //有效订单数列表
        LocalDate date = begin;
        while (!date.isAfter(end)) {
            dateList.add(date);
            //构造时间区间
            LocalDateTime beginTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();

            Map map = new HashMap<>();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", null);
            //订单数列表
            Integer orderCount = orderMapper.countByDate(map);
            orderCountList.add(orderCount == null ? 0 : orderCount);
            //有效订单数列表
            map.put("status", COMPLETED);
            Integer validOrderCount = orderMapper.countByDate(map);
            validOrderCountList.add(validOrderCount == null ? 0 : validOrderCount);
            date = date.plusDays(1);
        }
        //整理数据
        //订单总数
        Integer totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        //有效订单总数
        Integer validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        //订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount * 100;
        }
        //类型转化
        String dataListStr = dateList.stream().map(LocalDate::toString).collect(Collectors.joining(","));
        String orderCountListStr = orderCountList.stream().map(String::valueOf).collect(Collectors.joining(","));
        String validOrderCountListStr = validOrderCountList.stream().map(String::valueOf).collect(Collectors.joining(","));
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(dataListStr)
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(orderCountListStr)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .validOrderCountList(validOrderCountListStr)
                .build();
        return orderReportVO;
    }

    /**
     * 获取销量Top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO SalesTop10ReportVO(LocalDate begin, LocalDate end) {
        // 构建时间区间
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);

        // 查询销量前10的商品
        List<GoodsSalesDTO> top10List = orderDetailMapper.getTop10(beginTime, endTime);

        // 拆分成两个列表：商品名、销量
        List<String> nameList = top10List.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());

        List<Integer> numberList = top10List.stream()
                .map(GoodsSalesDTO::getNumber)
                .collect(Collectors.toList());

        // 拼成逗号分隔字符串
        String nameStr = String.join(",", nameList);
        String numberStr = numberList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // 构建返回对象
        return SalesTop10ReportVO.builder()
                .nameList(nameStr)
                .numberList(numberStr)
                .build();
    }

    /**
     * 导出报表
     * @param response
     */
    @Override
    public void exportBusiness(HttpServletResponse response) {
        //获取时间区间
        LocalDate dateBegin = LocalDate.now().plusDays(-30);
        LocalDate dateEnd = LocalDate.now();
        LocalDateTime beginTime = dateBegin.atStartOfDay();
        LocalDateTime endTime = dateEnd.atStartOfDay();
        //查询数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(beginTime, endTime);
        //通过POI填入excel
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //创建
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填入
            //第2行
            sheet.getRow(1).getCell(1).setCellValue("时间：" + beginTime + "至" + endTime);
            //第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            //第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            //明细数据填充
            for (int i = 0; i < 30; i++) {
                //构建时间区间
                LocalDate date = dateBegin.plusDays(i);
                LocalDateTime begin = date.atStartOfDay();
                LocalDateTime end = date.plusDays(1).atStartOfDay();
                //查询某一天的数据
                BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //用输出流把文件输出到浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
