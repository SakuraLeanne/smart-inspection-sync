package com.example.smartinspection.repository;

import com.example.smartinspection.domain.MaterialsInventoryRequestRow;
import com.example.smartinspection.service.SyncDateConvertService;
import java.sql.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SqlServerMaterialsInventoryRequestReader {
    private static final String COLUMNS = "Id,SupplierId,RequestType,BillNo,ShipmentNumber,TotalCount,RequestDate,RequestUserId,RelatedCustomerId,Status,ConfirmDate,ConfirmUserId,PaymentDate,RelatedUserId,NeedPaymentMoney,Remark,OrganizationItemId,TotalTaxation,TotalPreTaxAmount,DiscountedPrice,RepairBillNo,IsUpgradeData,InventoryBegnDate,InventoryEndDate";

    private final @Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate;
    private final SyncDateConvertService convertService;

    public SqlServerMaterialsInventoryRequestReader(@Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate,
                                                    SyncDateConvertService convertService) {
        this.sqlServerJdbcTemplate = sqlServerJdbcTemplate;
        this.convertService = convertService;
    }

    public List<MaterialsInventoryRequestRow> readByIdGreaterThan(int lastMaxId, int limit) {
        String sql = "SELECT TOP (?) " + COLUMNS + " FROM dbo.MaterialsInventoryRequest WHERE Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> mapRow(rs), limit, lastMaxId);
    }

    private MaterialsInventoryRequestRow mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        MaterialsInventoryRequestRow row = new MaterialsInventoryRequestRow();
        row.setId(rs.getInt("Id"));
        row.setSupplierId((Integer) rs.getObject("SupplierId"));
        row.setRequestType(rs.getString("RequestType"));
        row.setBillNo(rs.getString("BillNo"));
        row.setShipmentNumber(rs.getString("ShipmentNumber"));
        row.setTotalCount(rs.getBigDecimal("TotalCount"));
        row.setRequestDate(convertService.toLocalDateTime(rs.getTimestamp("RequestDate")));
        row.setRequestUserId((Integer) rs.getObject("RequestUserId"));
        row.setRelatedCustomerId((Integer) rs.getObject("RelatedCustomerId"));
        row.setStatus(rs.getInt("Status"));
        row.setConfirmDate(convertService.toLocalDateTime(rs.getTimestamp("ConfirmDate")));
        row.setConfirmUserId((Integer) rs.getObject("ConfirmUserId"));
        row.setPaymentDate(convertService.toLocalDateTime(rs.getTimestamp("PaymentDate")));
        row.setRelatedUserId((Integer) rs.getObject("RelatedUserId"));
        row.setNeedPaymentMoney(rs.getBigDecimal("NeedPaymentMoney"));
        row.setRemark(rs.getString("Remark"));
        row.setOrganizationItemId(rs.getInt("OrganizationItemId"));
        row.setTotalTaxation(rs.getBigDecimal("TotalTaxation"));
        row.setTotalPreTaxAmount(rs.getBigDecimal("TotalPreTaxAmount"));
        row.setDiscountedPrice(rs.getBigDecimal("DiscountedPrice"));
        row.setRepairBillNo(rs.getString("RepairBillNo"));
        row.setIsUpgradeData(toBoolean(rs.getObject("IsUpgradeData")));
        row.setInventoryBegnDate(toLocalDate(rs.getDate("InventoryBegnDate")));
        row.setInventoryEndDate(toLocalDate(rs.getDate("InventoryEndDate")));
        return row;
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return Boolean.valueOf(value.toString());
    }

    private java.time.LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
