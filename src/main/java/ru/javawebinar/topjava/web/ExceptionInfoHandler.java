package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.javawebinar.topjava.util.ValidationUtil;
import ru.javawebinar.topjava.util.exception.ErrorInfo;
import ru.javawebinar.topjava.util.exception.ErrorType;
import ru.javawebinar.topjava.util.exception.IllegalRequestDataException;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

import static ru.javawebinar.topjava.util.ValidationUtil.getBindingResultString;
import static ru.javawebinar.topjava.util.exception.ErrorType.*;

@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class ExceptionInfoHandler {
    private static Logger log = LoggerFactory.getLogger(ExceptionInfoHandler.class);

    @Autowired
    private MessageSource messageSource;

    public final static Map<String, String> CONSTRAINS_I18N_MAP = Map.of(
            "users_unique_email_idx", "validation.emailnotunique",
            "meals_unique_user_datetime_idx", "validation.mealdatetimenotunique");

    //  http://stackoverflow.com/a/22358422/548473
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(NotFoundException.class)
    public ErrorInfo handleError(HttpServletRequest req, NotFoundException e, Locale locale) {
        return logAndGetErrorInfo(req, e, false, DATA_NOT_FOUND, locale);
    }

    @ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorInfo conflict(HttpServletRequest req, DataIntegrityViolationException e, Locale locale) {
        return logAndGetErrorInfo(req, e, true, DATA_ERROR, locale);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)  // 422
    @ExceptionHandler({IllegalRequestDataException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            BindException.class})
    public ErrorInfo illegalRequestDataError(HttpServletRequest req, Exception e, Locale locale) {
        return logAndGetErrorInfo(req, e, false, VALIDATION_ERROR, locale);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorInfo handleError(HttpServletRequest req, Exception e, Locale locale) {
        return logAndGetErrorInfo(req, e, true, APP_ERROR, locale);
    }

    //    https://stackoverflow.com/questions/538870/should-private-helper-methods-be-static-if-they-can-be-static
    private ErrorInfo logAndGetErrorInfo(HttpServletRequest req, Exception e, boolean logException, ErrorType errorType, Locale locale) {
        Throwable rootCause = ValidationUtil.getRootCause(e);
        if (logException) {
            log.error(errorType + " at request " + req.getRequestURL(), rootCause);
        } else {
            log.warn("{} at request  {}: {}", errorType, req.getRequestURL(), rootCause.toString());
        }
        String detail = getExceptionDetail(e, rootCause, locale);
        return new ErrorInfo(req.getRequestURL(),
                errorType,
                messageSource.getMessage(errorType.getDescription(), null, locale),
                detail);
    }

    private String getExceptionDetail(Throwable e, Throwable rootCause, Locale locale) {
        String exceptionDetail = rootCause.getLocalizedMessage();
        Class eClass = e.getClass();
        if (e instanceof BindException) {
            exceptionDetail = getBindingResultString(((BindException) e).getBindingResult());
        } else if (eClass.equals(DataIntegrityViolationException.class)) {
            for (Map.Entry<String, String> entry : CONSTRAINS_I18N_MAP.entrySet()) {
                if (rootCause.getLocalizedMessage().contains(entry.getKey())) {
                    exceptionDetail = messageSource.getMessage(entry.getValue(), null, locale);
                }
            }
        }
        return exceptionDetail;
    }
}