package com.xuecheng.framework.exception;


import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
public class ExceptionCatch {

    // 日志处理类
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);

    private static ImmutableMap<Class< ? extends Throwable>, ResultCode> EXCEPTIONS;

    private static ImmutableMap.Builder<Class< ? extends Throwable>, ResultCode> builder = ImmutableMap.builder();

    // 存入异常的信息
    static {
        builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException customException){
        // Log日志
        LOGGER.error("catch Exception:{}",customException.getMessage(),customException);
        // 返回结果
        return new ResponseResult(customException.getResultCode());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception exception){
        // Log日志
        LOGGER.error("catch Exception:{}",exception.getMessage(),exception);
        // 返回结果
        if (EXCEPTIONS == null) {
            EXCEPTIONS = builder.build();
        }
        // 从Map中查到有没有对应的resultCode
        ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
        if (resultCode != null) {// 有的话直接返回resultCode
            return new ResponseResult(resultCode);
        }

        // 没有的话就返回普通的错误
        return new ResponseResult(CommonCode.SERVER_ERROR);


    }



}
