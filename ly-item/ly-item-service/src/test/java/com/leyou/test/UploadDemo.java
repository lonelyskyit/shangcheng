package com.leyou.test;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * @ClassName: UploadDemo
 * @Description: TODO
 * @Author: sky
 * @CreateDate: 2018/10/25/025  20:52
 * @Version: 1.0
 */
public class UploadDemo {
    //java入口
    public static void main(String[] args) {

    }

    public static HashMap<String, Object> uploadFiles(HttpServletRequest request, HttpServletResponse response) {
        HashMap<String, Object> map = new HashMap<>();
        String fileName="";
        try {
            //设置编码
            request.setCharacterEncoding("utf-8");
            response.setCharacterEncoding("utf-8");
            //获取文件上传目录
            String realPath = request.getRealPath("/");
            //定义上传目录
            String dirPath=realPath+"/bigdata";

            //
            File dirFile = new File(dirPath);
            if (!dirFile.exists()) {

            }
            //上传操作
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            //获取文件列表
            ServletRequestContext requestContext = new ServletRequestContext(request);
            List<FileItem> items = upload.parseRequest(requestContext);
            if (null != items) {
                Iterator<FileItem> itr = items.iterator();
                while (itr.hasNext()) {
                    FileItem fileItem = itr.next();
                    if (fileItem.isFormField()) {
                        continue;
                    } else {
                        //文件重命名 arry.jpg or .png
                        String name = fileItem.getName();
                        int i = name.lastIndexOf(".");
                        //cong i开始截取后缀名
                        String ext = name.substring(i, name.length());
                        fileName=new Date().getTime()+ext; //用时间戳进行拼接
                        //xie 写文件
                        File saveFile = new File(dirPath, fileName);
                        //将文件写入服务器中
                        fileItem.write(saveFile);
                        //将文件存储在map集合中，返回页面中进行展示
                        map.put("name", fileItem.getName());
                        map.put("size", fileItem.getSize());
                        map.put("newName", fileName);
                        map.put("url", "bigdata/" + fileName);
                    }
                }
            }
            //将服务器文件
            HdfsUtil hu=new HdfsUtil();
           // hu.fileUpload(dirPath, "ddYun_" + new Random().nextInt(1000) + "" + new Random().nextInt(1000));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
