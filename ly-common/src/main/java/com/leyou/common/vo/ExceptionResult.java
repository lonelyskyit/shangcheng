package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;

/**
 * view object, 视图图像，用来前端渲染
 */
@Data
public class ExceptionResult {
    private int status;
    private String message;
    private long timestamp;

    public ExceptionResult(ExceptionEnum exceptionEnum) {
        this.status = exceptionEnum.value();
        this.message = exceptionEnum.msg();
        this.timestamp = System.currentTimeMillis();
    }
}
