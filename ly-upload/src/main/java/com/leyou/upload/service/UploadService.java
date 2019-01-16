package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class UploadService {

    @Autowired
    private FastFileStorageClient storageClient;

    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg", "image/png", "image/bmp");

    public String uploadImage(MultipartFile file) {
        try {
            // 校验文件类型
            if(!ALLOW_TYPES.contains(file.getContentType())){
                // 无效文件
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image == null){
                // 不是图片
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

            // 获取拓展名
            String fileEtName = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            // 上传到FastDFS
            StorePath storePath = storageClient
                    .uploadFile(file.getInputStream(), file.getSize(), fileEtName, null);

            // 返回地址
            return "http://image.leyou.com/" + storePath.getFullPath();
        } catch (IOException e){
            log.error("[图片上传] 文件上传失败!", e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
