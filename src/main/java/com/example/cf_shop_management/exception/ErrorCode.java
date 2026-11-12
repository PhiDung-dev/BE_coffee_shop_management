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
    ROLE_NOT_EXISTED(1003, "Role không tồn tại", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1004, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    PHONE_EXISTED(1005, "Số điện thoại đã tồn tại", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_LINKED(1006, "Tài khoản đã được liên kết với người dùng khác", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_EXISTED(1007, "Token đặt lại mật khẩu không tồn tại", HttpStatus.NOT_FOUND),
    TOKEN_INVALID(1008, "Token không hợp lệ hoặc không tồn tại", HttpStatus.BAD_REQUEST),
    TOKEN_ALREADY_USED(1009, "Token đặt lại mật khẩu đã được sử dụng", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1010, "Token đặt lại mật khẩu đã hết hạn", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_EXISTED(1011, "Voucher không tồn tại", HttpStatus.NOT_FOUND),
    VOUCHER_CODE_EXISTED(1012, "Mã voucher đã tồn tại", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatusCode statusCode;
}