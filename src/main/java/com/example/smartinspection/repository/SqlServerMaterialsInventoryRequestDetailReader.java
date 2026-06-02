package com.example.smartinspection.repository;

import com.example.smartinspection.domain.MaterialsInventoryRequestDetailRow;
import com.example.smartinspection.service.SyncDateConvertService;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SqlServerMaterialsInventoryRequestDetailReader {
    private static final String COLUMNS = "Id,MaterialsInventoryRequestId,MaterialId,MaterialsInventoryId,SourceWarehouseId,SourceWarehouseAreaId,TargetWarehouseId,TargetWarehouseAreaId,[Count],ActualCount,BuyingPrice,SupplierPrice,SalePrice,TransferPrice,SettlementPrice,WholesalePrice,Taxation,ReturnPrice,Remark,TransferSupplierId,CreatedDate";

    private final @Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate;
    private final SyncDateConvertService convertService;

    public SqlServerMaterialsInventoryRequestDetailReader(@Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate,
                                                          SyncDateConvertService convertService) {
        this.sqlServerJdbcTemplate = sqlServerJdbcTemplate;
        this.convertService = convertService;
    }

    public List<MaterialsInventoryRequestDetailRow> readByIdGreaterThan(int lastMaxId, int limit) {
        String sql = "SELECT TOP (?) " + COLUMNS + " FROM dbo.MaterialsInventoryRequestDetail WHERE Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> {
            MaterialsInventoryRequestDetailRow row = new MaterialsInventoryRequestDetailRow();
            row.setId(rs.getInt("Id"));
            row.setMaterialsInventoryRequestId(rs.getInt("MaterialsInventoryRequestId"));
            row.setMaterialId(rs.getInt("MaterialId"));
            row.setMaterialsInventoryId((Integer) rs.getObject("MaterialsInventoryId"));
            row.setSourceWarehouseId((Integer) rs.getObject("SourceWarehouseId"));
            row.setSourceWarehouseAreaId((Integer) rs.getObject("SourceWarehouseAreaId"));
            row.setTargetWarehouseId((Integer) rs.getObject("TargetWarehouseId"));
            row.setTargetWarehouseAreaId((Integer) rs.getObject("TargetWarehouseAreaId"));
            row.setRequestCount(rs.getBigDecimal("Count"));
            row.setActualCount(rs.getBigDecimal("ActualCount"));
            row.setBuyingPrice(rs.getBigDecimal("BuyingPrice"));
            row.setSupplierPrice(rs.getBigDecimal("SupplierPrice"));
            row.setSalePrice(rs.getBigDecimal("SalePrice"));
            row.setTransferPrice(rs.getBigDecimal("TransferPrice"));
            row.setSettlementPrice(rs.getBigDecimal("SettlementPrice"));
            row.setWholesalePrice(rs.getBigDecimal("WholesalePrice"));
            row.setTaxation(rs.getBigDecimal("Taxation"));
            row.setReturnPrice(rs.getBigDecimal("ReturnPrice"));
            row.setRemark(rs.getString("Remark"));
            row.setTransferSupplierId((Integer) rs.getObject("TransferSupplierId"));
            row.setCreatedDate(convertService.toLocalDateTime(rs.getTimestamp("CreatedDate")));
            return row;
        }, limit, lastMaxId);
    }
}
