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
    CATEGORY_NOT_EXISTED(1013, "Danh mục không tồn tại", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_EXISTED(1014, "Tên danh mục đã tồn tại", HttpStatus.BAD_REQUEST),
    CATEGORY_SLUG_EXISTED(1015, "Slug danh mục đã tồn tại", HttpStatus.BAD_REQUEST),
    CATEGORY_HAS_PRODUCTS(1016, "Không thể xóa danh mục đang chứa sản phẩm", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(1017, "Không tìm thấy sản phẩm", HttpStatus.NOT_FOUND),
    PRODUCT_EXISTED(1018, "Tên sản phẩm đã tồn tại", HttpStatus.BAD_REQUEST),
    PRODUCT_IN_USE(1019, "Không thể xóa sản phẩm đã phát sinh đơn hàng, hãy chuyển trạng thái ngừng bán", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1020, "Không tìm thấy danh mục", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(1021, "Không tìm thấy giao dịch thanh toán", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(1022, "Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND),
    PAYMENT_TRANSACTION_EXISTED(1023, "Mã giao dịch thanh toán đã tồn tại", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_AMOUNT(1024, "Số tiền thanh toán vượt quá số tiền còn lại của đơn hàng", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_COMPLETED(1025, "Không thể chỉnh sửa thông tin hoặc xóa giao dịch đã thanh toán thành công", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_PAID(1026, "Đơn hàng này đã được thanh toán đủ", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatusCode statusCode;
}