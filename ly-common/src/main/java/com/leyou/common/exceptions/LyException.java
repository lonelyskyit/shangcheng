package com.leyou.common.exceptions;

import com.leyou.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LyException extends RuntimeException {
    private ExceptionEnum exceptionEnum;

    @Override
    public String getMessage() {
        return exceptionEnum.msg();
    }

    public int getStatus(){
        return exceptionEnum.value();
    }
}
