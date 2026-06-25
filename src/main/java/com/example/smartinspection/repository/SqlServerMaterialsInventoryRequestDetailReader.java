package com.example.smartinspection.repository;

import com.example.smartinspection.domain.MaterialsInventoryRequestDetailRow;
import com.example.smartinspection.service.SyncDateConvertService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> mapRow(rs), limit, lastMaxId);
    }

    /**
     * 明细表没有更新时间；物料主表被 Id 增量或 RequestDate 回溯命中后，
     * 需要按主表 Id 重刷其所有明细，覆盖老明细被修改但 Id 未变化的场景。
     */
    public List<MaterialsInventoryRequestDetailRow> readByRequestIds(List<Integer> requestIds) {
        if (requestIds == null || requestIds.isEmpty()) {
            return Collections.emptyList();
        }
        String placeholders = requestIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT " + COLUMNS + " FROM dbo.MaterialsInventoryRequestDetail WHERE MaterialsInventoryRequestId IN ("
                + placeholders + ") ORDER BY MaterialsInventoryRequestId ASC, Id ASC";
        return sqlServerJdbcTemplate.query(sql, requestIds.toArray(), (rs, rn) -> mapRow(rs));
    }

    private MaterialsInventoryRequestDetailRow mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
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
    }
}
