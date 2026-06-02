package com.example.smartinspection.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MaterialsInventoryRequestRow {
    private Integer id;
    private Integer supplierId;
    private String requestType;
    private String billNo;
    private String shipmentNumber;
    private BigDecimal totalCount;
    private LocalDateTime requestDate;
    private Integer requestUserId;
    private Integer relatedCustomerId;
    private Integer status;
    private LocalDateTime confirmDate;
    private Integer confirmUserId;
    private LocalDateTime paymentDate;
    private Integer relatedUserId;
    private BigDecimal needPaymentMoney;
    private String remark;
    private Integer organizationItemId;
    private BigDecimal totalTaxation;
    private BigDecimal totalPreTaxAmount;
    private BigDecimal discountedPrice;
    private String repairBillNo;
    private Boolean isUpgradeData;
    private LocalDate inventoryBegnDate;
    private LocalDate inventoryEndDate;
}
