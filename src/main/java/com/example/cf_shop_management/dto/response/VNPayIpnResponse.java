package com.example.cf_shop_management.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayIpnResponse {

    @JsonProperty("RspCode")
    private String rspCode;

    @JsonProperty("Message")
    private String message;

    public static VNPayIpnResponse of(String rspCode, String message) {
        return new VNPayIpnResponse(rspCode, message);
    }
}