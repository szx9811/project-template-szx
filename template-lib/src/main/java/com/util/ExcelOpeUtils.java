package com.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author 佘忠新
 * @Date 2022/5/27 9:40
 * @Version 1.0
 */
@Slf4j
public class ExcelOpeUtils {

    /**
     * 导出excel通用方法
     * @param response
     * @param list
     * @param fileName 生成的文件名
     */
    public static void excelExport(HttpServletResponse response, List<? extends Object> list, Class clazz, String fileName) {
        // 注意 simpleWrite在数据量不大的情况下可以使用（5000以内，具体也要看实际情况），数据量大参照 重复多次写入

        // 写法1 JDK8+
        // since: 3.0.0-beta1

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyyMMdd");

        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + df.format(date) + ".xlsx");


        try {
            // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为fileName 然后文件流会自动关闭
            // 如果这里想使用03 则 传入excelType参数即可

            //如果导出不需要表头，则改为write(response.getOutputStream())
            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(fileName)
                    //注册策略：例如合并单元格，下拉等等
                    //.registerWriteHandler()
                    .doWrite(() -> {
                        // 分页查询数据
                        return list;
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



        /**
     * excel导入
     * 导入模板
     *
     * @param file  文件
     * @param clazz clazz
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Transactional
    public void excelImport(MultipartFile file, Class<T> clazz) throws IOException, ClassNotFoundException {
        
        HashMap hashMap = ExcelOpeUtils.getImportField(clazz.getName());
        Set fieldSet = hashMap.entrySet();
    
        System.out.println(hashMap);
        System.out.println(fieldSet);
    
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(file.getInputStream(), clazz, new AnalysisEventListener<T>() {
    
            /**
             * 单次缓存的数据量
             */
    
            public static final int BATCH_COUNT = 100;
    
    
            /**
             * 临时存储
             */
    
            private List<T> cachedDataList = new ArrayList<>();
    
            @Override
            public void invoke(T data, AnalysisContext context) {
                //TODO 导入时的校验
    
    
                cachedDataList.add(data);
                if (cachedDataList.size() >= BATCH_COUNT) {
                    saveData();
                    // 存储完成清理 list
                    cachedDataList = new ArrayList<>();
                }
    
            }
    
    
            /**
             * @param headMap 传入excel头部(第一行数据)数据的index,name
             *                校验excel头部格式
             */
    
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
    
                System.out.println("表头的当前行********" + context.readRowHolder().getRowIndex());
                System.out.println("表头************" + headMap);
    
                if (0 == context.readRowHolder().getRowIndex()) {
    
                    Set<Map.Entry<Integer, String>> entrySet = headMap.entrySet();
                    if (!fieldSet.containsAll(entrySet) || !entrySet.containsAll(fieldSet)) {
                        throw new ExcelAnalysisException("请检查模板的正确性！");
                    }
    
                }
    
    
            }
    
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                saveData();
            }
    
    
            /**
             * 加上存储数据库
             */
    
            private void saveData() {
                log.info("{}条数据，开始存储数据库！", cachedDataList.size());
    	     //TODO 
                log.info("存储数据库成功！");
            }
        }).sheet()
                //第一行开始读取表头
                .headRowNumber(1).doRead();
    
    }
    
    
    /**
     * 获取导入的实体类的excel注解
     *
     * @param referencePath 类路径
     * @return {@link HashMap}
     * @throws ClassNotFoundException 类没有发现异常
     */
    public static HashMap getImportField(String referencePath) throws ClassNotFoundException {
        Class<?> aClass = Class.forName(referencePath);
        //获取类的成员变量
        Field[] declaredFields = aClass.getDeclaredFields();
    
        //此map中添加需要导入的变量
        HashMap hashMap = new HashMap();
    
        int index = 0;
        for (int i = 0; i < declaredFields.length; i++) {
    
            //没有添加@ExcelIgnore的变量则添加至hashmap中
            if (!(declaredFields[i].isAnnotationPresent(ExcelIgnore.class))) {
                hashMap.put(index, declaredFields[i].getName());
                index++;
            }
    
        }
    
        return hashMap;
    }
    
}