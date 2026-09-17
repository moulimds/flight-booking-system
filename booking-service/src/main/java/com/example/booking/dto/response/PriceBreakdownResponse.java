package com.example.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdownResponse {

    private MemberPricingDetail adult;
    private MemberPricingDetail child;
    private MemberPricingDetail infant;
    private BigDecimal grandTotal;
    private String currency;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberPricingDetail {
        private Integer count;
        private BigDecimal basePricePerPerson;
        private BigDecimal taxPerPerson;
        private BigDecimal totalPerPerson;
        private BigDecimal subtotal;
    }
}
