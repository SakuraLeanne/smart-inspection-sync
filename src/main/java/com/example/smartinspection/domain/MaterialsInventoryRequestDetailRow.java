package com.example.smartinspection.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MaterialsInventoryRequestDetailRow {
    private Integer id;
    private Integer materialsInventoryRequestId;
    private Integer materialId;
    private Integer materialsInventoryId;
    private Integer sourceWarehouseId;
    private Integer sourceWarehouseAreaId;
    private Integer targetWarehouseId;
    private Integer targetWarehouseAreaId;
    private BigDecimal requestCount;
    private BigDecimal actualCount;
    private BigDecimal buyingPrice;
    private BigDecimal supplierPrice;
    private BigDecimal salePrice;
    private BigDecimal transferPrice;
    private BigDecimal settlementPrice;
    private BigDecimal wholesalePrice;
    private BigDecimal taxation;
    private BigDecimal returnPrice;
    private String remark;
    private Integer transferSupplierId;
    private LocalDateTime createdDate;
}
