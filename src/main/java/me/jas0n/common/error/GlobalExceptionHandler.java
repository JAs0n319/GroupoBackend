package me.jas0n.common.error;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // 1. 业务异常
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        pd.setTitle("Business rule violation");
        pd.setType(URI.create("https://errors.groupo.dev/" + ex.getCode()));
        pd.setProperty("code", ex.getCode());
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    // 2. 参数校验异常（Bean Validation）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, "参数校验失败");
        pd.setTitle("Validation failed");
        pd.setType(URI.create("https://errors.groupo.dev/VALIDATION_ERROR"));
        pd.setProperty("code", "VALIDATION_ERROR");

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (a, b) -> a // 同字段多条错误时保留第一个
                ));
        pd.setProperty("errors", errors);

        pd.setProperty("timestamp", Instant.now().toString());
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    // 3. 请求体解析错误（JSON语法/类型错误）
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "请求体解析失败");
        pd.setTitle("Bad Request");
        pd.setType(URI.create("https://errors.groupo.dev/BAD_REQUEST"));
        pd.setProperty("code", "BAD_REQUEST");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    // 4. JWT 解析/验证类异常（签名错误、过期、格式错等）
    @ExceptionHandler(JwtException.class)
    public ProblemDetail handleJwt(JwtException ex, HttpServletRequest req) {
        if (ex instanceof ExpiredJwtException) {
            var pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "刷新令牌已过期");
            pd.setTitle("Token expired");
            pd.setType(URI.create("https://errors.groupo.dev/TOKEN_EXPIRED"));
            pd.setProperty("code", "TOKEN_EXPIRED");
            pd.setProperty("timestamp", Instant.now().toString());
            pd.setInstance(URI.create(req.getRequestURI()));
            return pd;
        }
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "无效的令牌");
        pd.setTitle("Invalid token");
        pd.setType(URI.create("https://errors.groupo.dev/INVALID_TOKEN"));
        pd.setProperty("code", "INVALID_TOKEN");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    // 5. 签名密钥配置问题（密钥太短等）
    @ExceptionHandler(WeakKeyException.class)
    public ProblemDetail handleSigningKey(WeakKeyException ex, HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "签名密钥配置错误");
        pd.setTitle("Token signing key invalid");
        pd.setType(URI.create("https://errors.groupo.dev/TOKEN_SIGNING_KEY_INVALID"));
        pd.setProperty("code", "TOKEN_SIGNING_KEY_INVALID");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    // 6. 非 JWT 语义的参数不合法
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "请求参数不合法");
        pd.setTitle("Bad Request");
        pd.setType(URI.create("https://errors.groupo.dev/ILLEGAL_ARGUMENT"));
        pd.setProperty("code", "ILLEGAL_ARGUMENT");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }

    // 7. 查询参数类型不匹配/枚举转换失败等 -> 400
    @org.springframework.web.bind.annotation.ExceptionHandler({
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.core.convert.ConversionFailedException.class
    })
    public org.springframework.http.ProblemDetail handleQueryParam(Exception ex, jakarta.servlet.http.HttpServletRequest req) {
        var pd = org.springframework.http.ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatus.BAD_REQUEST, "无效的查询参数");
        pd.setTitle("Bad Request");
        pd.setType(java.net.URI.create("https://errors.groupo.dev/BAD_QUERY_PARAM"));
        pd.setProperty("code", "BAD_QUERY_PARAM");
        pd.setInstance(java.net.URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", java.time.Instant.now().toString());
        // 可选：暴露具体哪个参数错误（避免泄露栈）
        return pd;
    }

    // 8. 分页/排序参数非法 -> 400（如 sort 字段不存在）
    @org.springframework.web.bind.annotation.ExceptionHandler({
            org.springframework.data.mapping.PropertyReferenceException.class
    })
    public org.springframework.http.ProblemDetail handleSortProperty(Exception ex, jakarta.servlet.http.HttpServletRequest req) {
        var pd = org.springframework.http.ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatus.BAD_REQUEST, "无效的排序字段");
        pd.setTitle("Bad Request");
        pd.setType(java.net.URI.create("https://errors.groupo.dev/BAD_SORT"));
        pd.setProperty("code", "BAD_SORT");
        pd.setInstance(java.net.URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", java.time.Instant.now().toString());
        return pd;
    }

    // 9. 未知异常兜底
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAny(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("https://errors.groupo.dev/INTERNAL_ERROR"));
        pd.setProperty("code", "INTERNAL_ERROR");
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }
}