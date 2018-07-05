package com.nio.util;

import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 *存储到本地
 */
public class FileUtils {

    /**
     * path = new File(ResourceUtils.getURL("classpath:").getPath());
     //在开发测试模式时，得到的地址为：{项目跟目录}/target/static/images/upload/
     //在打包成jar正式发布时，得到的地址为：{发布jar包目录}/static/images/upload/
     * @param fileName
     * @return   url
     */
    public static String storeFile(String fileName,byte[] pic)throws IOException{
        String picPath= System.getProperty("picPath");
        File upload = new File(picPath,"static/images/upload/");
        if(!upload.exists()) upload.mkdirs();
        String path="/images/upload/"+fileName;
            IOUtils.write(pic, new FileOutputStream(upload.getAbsolutePath()+"/"+fileName));

        return path;
    }

    public static void main(String[] args){

        System.out.println(System.getProperty("user.dir"));
    }

}
