package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 订单超时取消订单
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 取消支付超时订单
     */
//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "1/5 * * * * ?")
    public void processTimeOutOrder() {
        log.info("查询支付超时订单:{}", LocalDateTime.now());
        //计算时间
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        List<Orders> orders = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);
        if (orders != null && !orders.isEmpty()) {
            //更新数据
            log.info("发现{}个支付超时订单", orders.size());
            for (Orders order : orders) {
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("支付超时");
                order.setStatus(Orders.CANCELLED);
                orderMapper.update(order);
            }
        } else {
            log.info("暂无支付超时订单");
        }
    }

    /**
     * 一直处于派送中的订单改成完成
     */
//    @Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(cron = "0/5 * * * * ?")
    public void processDeliveryOrder() {
        log.info("自动改派送中订单为完成:{}", LocalDateTime.now());
        //计算时间
        LocalDateTime time = LocalDateTime.now().minusMinutes(60);
        List<Orders> orders = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);
        if (orders != null && !orders.isEmpty()) {
            //更新数据
            log.info("发现{}个'派送中'订单", orders.size());
            for (Orders order : orders) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        } else {
            log.info("暂无'派送中'订单");
        }
    }
}
