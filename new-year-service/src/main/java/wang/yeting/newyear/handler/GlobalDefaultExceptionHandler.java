package wang.yeting.newyear.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import wang.yeting.newyear.exceptions.BusinessException;
import wang.yeting.newyear.exceptions.LoginException;
import wang.yeting.newyear.exceptions.PermissionException;
import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.ResultCode;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author : weipeng
 * @date : 2020-08-04 16:33
 */
@Slf4j
@RestControllerAdvice
public class GlobalDefaultExceptionHandler {

    @ExceptionHandler(value = PermissionException.class)
    public Result handlerPermissionException(PermissionException exception) {
        return Result.noPermission(exception.getMessage());
    }

    @ExceptionHandler(value = LoginException.class)
    public Result handlerLoginException(LoginException exception) {
        return Result.tokenExpired(exception.getMessage());
    }

    @ExceptionHandler(value = BusinessException.class)
    public Result handlerBusinessException(BusinessException exception) {
        return Result.exceptionError(exception.getMessage());
    }

    @ExceptionHandler(value = UndeclaredThrowableException.class)
    public Result handlerUndeclaredThrowableException(UndeclaredThrowableException exception) {
        Throwable undeclaredThrowable = exception.getUndeclaredThrowable();
        if (undeclaredThrowable instanceof LoginException) {
            return Result.tokenExpired(undeclaredThrowable.getMessage());
        } else if (undeclaredThrowable instanceof PermissionException) {
            return Result.noPermission(exception.getMessage());
        }
        return Result.exceptionError(undeclaredThrowable.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public Result handlerException(Exception exception) {
        log.error("handlerException", exception);
        return Result.exceptionError(ResultCode.exception.message);
    }
}
