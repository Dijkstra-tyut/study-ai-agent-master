package study.studyai.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            BindException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class
    })
    public BaseResponse<?> paramsExceptionHandler(Exception e) {
        log.error("ParamsException", e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public BaseResponse<?> maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException e) {
        log.error("MaxUploadSizeExceededException", e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件过大，最大支持 100MB");
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
