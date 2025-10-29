package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    /**
     * 前置通知
     */
    @Before("autoFillPointCut()")
    public void atuoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充");

        //获得注解类型 UPDATE or INSERT
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //获取当前被拦截的对象(但是是Object类)
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];

        //准备要赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //判断参数是集合还是单独对象
        if (entity instanceof List) {
            List<?> list = (List<?>) entity;
            if (!list.isEmpty()) {
                for (Object obj : list) {
                    fillField(obj, operationType, now, currentId);
                }
            }
        }
        else {
            fillField(entity, operationType, now, currentId);
        }

    }

//    public void fillField(Object entity, OperationType operationType, LocalDateTime now, Long currentId) {
//        //开始赋值
//        if (operationType == OperationType.INSERT) {
//            //四个参数
//            try {
//                //赋值updatetime
//                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
//                setUpdateTime.invoke(entity, now);
//                //赋值upDateUser
//                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
//                setUpdateUser.invoke(entity, currentId);
//                //赋值setCreateUser
//                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
//                setCreateUser.invoke(entity, currentId);
//                //赋值updatetime
//                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
//                setCreateTime.invoke(entity, now);
//
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//        } else if (operationType == OperationType.UPDATE) {
//            //两个参数
//            try {
//                //赋值updatetime
//                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
//                setUpdateTime.invoke(entity, now);
//                //赋值upDateUser
//                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
//                setUpdateUser.invoke(entity, currentId);
//
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
    /**
     * 自动填充核心逻辑
     */
    public void fillField(Object entity, OperationType operationType, LocalDateTime now, Long currentId) {
        Class<?> clazz = entity.getClass();

        if (operationType == OperationType.INSERT) {
            // 插入时：尝试填充四个字段（存在才填）
            setIfExists(clazz, entity, AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class, now);
            setIfExists(clazz, entity, AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class, now);
            setIfExists(clazz, entity, AutoFillConstant.SET_CREATE_USER, Long.class, currentId);
            setIfExists(clazz, entity, AutoFillConstant.SET_UPDATE_USER, Long.class, currentId);

        } else if (operationType == OperationType.UPDATE) {
            // 更新时：尝试填充两个字段（存在才填）
            setIfExists(clazz, entity, AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class, now);
            setIfExists(clazz, entity, AutoFillConstant.SET_UPDATE_USER, Long.class, currentId);
        }
    }

    /**
     * 安全地调用 setter 方法，如果字段不存在则跳过
     */
    private void setIfExists(Class<?> clazz, Object entity, String methodName, Class<?> paramType, Object value) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramType);
            method.invoke(entity, value);
        } catch (NoSuchMethodException ignored) {
            // 实体类没有这个字段，直接跳过，不抛异常
        } catch (Exception e) {
            throw new RuntimeException("自动填充字段出错：" + methodName, e);
        }
    }
}
