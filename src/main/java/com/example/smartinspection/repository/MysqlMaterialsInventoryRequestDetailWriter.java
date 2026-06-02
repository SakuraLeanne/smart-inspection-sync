package com.example.smartinspection.repository;

import com.example.smartinspection.domain.MaterialsInventoryRequestDetailRow;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MysqlMaterialsInventoryRequestDetailWriter {
    private final @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate;

    public MysqlMaterialsInventoryRequestDetailWriter(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }

    public int[] upsertBatch(List<MaterialsInventoryRequestDetailRow> rows) {
        String sql = "INSERT INTO cus_raw_materials_inventory_request_detail (source_id,materials_inventory_request_id,material_id,materials_inventory_id,source_warehouse_id,source_warehouse_area_id,target_warehouse_id,target_warehouse_area_id,request_count,actual_count,buying_price,supplier_price,sale_price,transfer_price,settlement_price,wholesale_price,taxation,return_price,remark,transfer_supplier_id,created_date,raw_sync_time,raw_update_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW()) ON DUPLICATE KEY UPDATE materials_inventory_request_id=VALUES(materials_inventory_request_id),material_id=VALUES(material_id),materials_inventory_id=VALUES(materials_inventory_id),source_warehouse_id=VALUES(source_warehouse_id),source_warehouse_area_id=VALUES(source_warehouse_area_id),target_warehouse_id=VALUES(target_warehouse_id),target_warehouse_area_id=VALUES(target_warehouse_area_id),request_count=VALUES(request_count),actual_count=VALUES(actual_count),buying_price=VALUES(buying_price),supplier_price=VALUES(supplier_price),sale_price=VALUES(sale_price),transfer_price=VALUES(transfer_price),settlement_price=VALUES(settlement_price),wholesale_price=VALUES(wholesale_price),taxation=VALUES(taxation),return_price=VALUES(return_price),remark=VALUES(remark),transfer_supplier_id=VALUES(transfer_supplier_id),created_date=VALUES(created_date),raw_update_time=NOW()";
        return mysqlJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                MaterialsInventoryRequestDetailRow r = rows.get(i);
                int c = 1;
                ps.setInt(c++, r.getId());
                ps.setInt(c++, r.getMaterialsInventoryRequestId());
                ps.setInt(c++, r.getMaterialId());
                ps.setObject(c++, r.getMaterialsInventoryId());
                ps.setObject(c++, r.getSourceWarehouseId());
                ps.setObject(c++, r.getSourceWarehouseAreaId());
                ps.setObject(c++, r.getTargetWarehouseId());
                ps.setObject(c++, r.getTargetWarehouseAreaId());
                ps.setBigDecimal(c++, r.getRequestCount());
                ps.setBigDecimal(c++, r.getActualCount());
                ps.setBigDecimal(c++, r.getBuyingPrice());
                ps.setBigDecimal(c++, r.getSupplierPrice());
                ps.setBigDecimal(c++, r.getSalePrice());
                ps.setBigDecimal(c++, r.getTransferPrice());
                ps.setBigDecimal(c++, r.getSettlementPrice());
                ps.setBigDecimal(c++, r.getWholesalePrice());
                ps.setBigDecimal(c++, r.getTaxation());
                ps.setBigDecimal(c++, r.getReturnPrice());
                ps.setString(c++, r.getRemark());
                ps.setObject(c++, r.getTransferSupplierId());
                ps.setTimestamp(c++, toTs(r.getCreatedDate()));
            }

            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    private Timestamp toTs(LocalDateTime t) {
        return t == null ? null : Timestamp.valueOf(t);
    }
}
