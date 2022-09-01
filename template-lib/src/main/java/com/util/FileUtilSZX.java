package com.util;

import com.alibaba.excel.annotation.ExcelIgnore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author 佘忠新
 * @Date 2022/6/15 18:58
 * @Version 1.0
 */
@Slf4j
public class FileUtilSZX {
    /**
     * zip解压
     *
     * @param srcFile     zip源文件全路径
     * @param destDirPath 解压后的文件存放的地址
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static List<String> unZip(File srcFile, String destDirPath) throws RuntimeException {
        //记录解压出来的所有文件名
        List<String> filesName = new ArrayList<>();
        long start = System.currentTimeMillis();
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "所指文件不存在");
        }
        // 开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(srcFile, Charset.forName("GBK"));
            Enumeration<?> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                //添加进filesName
                filesName.add(entry.getName());
                System.out.println("解压文件:" + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + "/" + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(destDirPath + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("解压完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filesName;
    }




    /**
     * 删除文件
     * @param dir
     */
    public static void delete(File dir) {
        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            dir.delete();
            return;
        }

        for (int i = 0; i < files.length; i++) {
            delete(files[i]);
            if (files[i].isDirectory()) {
                files[i].delete();
            }
        }
        dir.delete();
    }

    /**
     * 上传单个文件
     * @param file
     * @param uploadPath  上传路径  example：/xxx/xx/x
     * @return 存储的文件全路径
     * @throws Exception
     */
    public static String upload(MultipartFile file, String uploadPath) throws Exception {
        String originName = file.getOriginalFilename();
        //截取到的是文件后缀 例csv
        String fileType =originName.substring(originName.lastIndexOf(".")+1);
        StringBuilder fileName = new StringBuilder(UUID.randomUUID().toString());
        fileName.append(".").append(fileType);
        String filePath = uploadPath.concat(fileName.toString());
        File uploadFile = new File(filePath);
        if(!uploadFile.getParentFile().exists()){
            if(uploadFile.getParentFile().mkdirs()){
                log.info("创建父级文件夹:{}",uploadFile.getParentFile().getAbsolutePath());
            }
        }
        while(true){
            if(!uploadFile.exists()){
                break;
            }
            log.info("文件名重复：{}",uploadFile.getAbsolutePath());
            fileName = new StringBuilder(UUID.randomUUID().toString());
            fileName.append(".").append(fileType);
            filePath = uploadPath.concat(fileName.toString());
            uploadFile = new File(filePath);
        }
        try {
            file.transferTo(uploadFile);
        } catch (IOException e) {
            log.error("发现异常：{}",e.getMessage());
            throw new Exception("文件上传失败！");
        }

        return filePath;
    }


    /**
     * 下载文件
     *
     * @param filePath   文件全路径
     * @param originName 生成的文件名称
     * @return {@link ResponseEntity}<{@link InputStreamResource}>
     * @throws IOException ioexception
     */
    public static ResponseEntity<InputStreamResource> downloadFile(String filePath, String originName) throws IOException{
        FileSystemResource file = new FileSystemResource(filePath);
        log.info("传入的文件名为:{}",originName);
        HttpHeaders headers = setHttpHeaders(file);
        headers.add("Content-Disposition",
                String.format("attachment; filename= %s", URLEncoder.encode(originName,"UTF-8")));
        return ResponseEntity
                .ok()
                .headers(headers)
//                .contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream;charset=UTF-8"))
                .body(new InputStreamResource(file.getInputStream()));
    }

    public static HttpHeaders setHttpHeaders(Resource file){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return headers;
    }

}

