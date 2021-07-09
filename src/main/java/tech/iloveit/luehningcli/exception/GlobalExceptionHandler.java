package tech.iloveit.luehningcli.exception;

import tech.iloveit.luehningcli.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 全局异常处理
 */
@Slf4j
@ControllerAdvice
@Component
public class GlobalExceptionHandler {
    /**
     * 全局异常捕捉处理
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResponseResult errorHandler(Exception ex) {
        log.error("系统异常Exception："+ex.getMessage());
        ex.printStackTrace();
        return ResponseResult.fail(ex.getMessage());
    }

    /**
     * validator校验失败信息处理
     * @param exception
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public ResponseResult validationHandler(BindException exception) {
        log.error("数据校验异常：" + exception.getMessage());
        BindingResult bindingResult = exception.getBindingResult();

        Map<String,String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError)->{
            errorMap.put(fieldError.getField(),fieldError.getDefaultMessage());
        });

        return new ResponseResult(500,"数据校验异常",errorMap);
    }

    /**
     * 拦截捕捉业务异常 ServiceException.class
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = ServiceException.class)
    public ResponseResult commonExceptionHandler(ServiceException ex) {
        log.error("业务异常ServiceException："+ex.getMessage());
        ex.printStackTrace();
        return ResponseResult.info(ex.getMessage());
    }

}
