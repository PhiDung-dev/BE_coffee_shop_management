package com.example.cf_shop_management.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    OTHERS_ERROR(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_EXISTED(1001, "Email đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_EXISTED(1002, "Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED(1003, "Role không tồn tại", HttpStatus.NOT_FOUND);

    int code;
    String message;
    HttpStatusCode statusCode;
}