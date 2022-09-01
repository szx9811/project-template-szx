package com.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 佘忠新
 * @Date 2022/7/20 14:40
 * @Version 1.0
 */
public class BeanUtilSZX {

    /**
     * 转换
     * 类转换
     *
     * @param records     待转换的list
     * @param sourceClazz 源clazz
     * @param targetClazz 目标clazz
     * @return {@link List}<{@link ?}>
     */
    public static <targetClazz> List<?> convert(List<?> records, Class<?> sourceClazz, Class<?> targetClazz) throws IllegalAccessException, InstantiationException {

        ArrayList answer = new ArrayList();


        for (Object record : records) {
            targetClazz o = (targetClazz) targetClazz.newInstance();
            BeanUtils.copyProperties(record, o);

            answer.add(o);
        }

        return answer;

    }
}