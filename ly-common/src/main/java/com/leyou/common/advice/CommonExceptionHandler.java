package com.leyou.common.advice;

import com.leyou.common.exceptions.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {

    /**
     * 只处理了自定义异常
     * @param e
     * @return
     */
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handleException(LyException e){
        return ResponseEntity.status(e.getStatus())
                .body(new ExceptionResult(e.getExceptionEnum()));
    }
}
