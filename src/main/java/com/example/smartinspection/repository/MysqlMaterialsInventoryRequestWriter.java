package com.example.smartinspection.repository;

import com.example.smartinspection.domain.MaterialsInventoryRequestRow;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MysqlMaterialsInventoryRequestWriter {
    private final @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate;

    public MysqlMaterialsInventoryRequestWriter(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }

    public int[] upsertBatch(List<MaterialsInventoryRequestRow> rows) {
        String sql = "INSERT INTO cus_raw_materials_inventory_request (source_id,supplier_id,request_type,bill_no,shipment_number,total_count,request_date,request_user_id,related_customer_id,status,confirm_date,confirm_user_id,payment_date,related_user_id,need_payment_money,remark,organization_item_id,total_taxation,total_pre_tax_amount,discounted_price,repair_bill_no,is_upgrade_data,inventory_begn_date,inventory_end_date,raw_sync_time,raw_update_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW()) ON DUPLICATE KEY UPDATE supplier_id=VALUES(supplier_id),request_type=VALUES(request_type),bill_no=VALUES(bill_no),shipment_number=VALUES(shipment_number),total_count=VALUES(total_count),request_date=VALUES(request_date),request_user_id=VALUES(request_user_id),related_customer_id=VALUES(related_customer_id),status=VALUES(status),confirm_date=VALUES(confirm_date),confirm_user_id=VALUES(confirm_user_id),payment_date=VALUES(payment_date),related_user_id=VALUES(related_user_id),need_payment_money=VALUES(need_payment_money),remark=VALUES(remark),organization_item_id=VALUES(organization_item_id),total_taxation=VALUES(total_taxation),total_pre_tax_amount=VALUES(total_pre_tax_amount),discounted_price=VALUES(discounted_price),repair_bill_no=VALUES(repair_bill_no),is_upgrade_data=VALUES(is_upgrade_data),inventory_begn_date=VALUES(inventory_begn_date),inventory_end_date=VALUES(inventory_end_date),raw_update_time=NOW()";
        return mysqlJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                MaterialsInventoryRequestRow r = rows.get(i);
                int c = 1;
                ps.setInt(c++, r.getId());
                ps.setObject(c++, r.getSupplierId());
                ps.setString(c++, r.getRequestType());
                ps.setString(c++, r.getBillNo());
                ps.setString(c++, r.getShipmentNumber());
                ps.setBigDecimal(c++, r.getTotalCount());
                ps.setTimestamp(c++, toTs(r.getRequestDate()));
                ps.setObject(c++, r.getRequestUserId());
                ps.setObject(c++, r.getRelatedCustomerId());
                ps.setInt(c++, r.getStatus());
                ps.setTimestamp(c++, toTs(r.getConfirmDate()));
                ps.setObject(c++, r.getConfirmUserId());
                ps.setTimestamp(c++, toTs(r.getPaymentDate()));
                ps.setObject(c++, r.getRelatedUserId());
                ps.setBigDecimal(c++, r.getNeedPaymentMoney());
                ps.setString(c++, r.getRemark());
                ps.setInt(c++, r.getOrganizationItemId());
                ps.setBigDecimal(c++, r.getTotalTaxation());
                ps.setBigDecimal(c++, r.getTotalPreTaxAmount());
                ps.setBigDecimal(c++, r.getDiscountedPrice());
                ps.setString(c++, r.getRepairBillNo());
                ps.setObject(c++, Boolean.TRUE.equals(r.getIsUpgradeData()) ? 1 : 0);
                ps.setDate(c++, toDate(r.getInventoryBegnDate()));
                ps.setDate(c++, toDate(r.getInventoryEndDate()));
            }

            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    private Timestamp toTs(LocalDateTime t) {
        return t == null ? null : Timestamp.valueOf(t);
    }

    private Date toDate(LocalDate d) {
        return d == null ? null : Date.valueOf(d);
    }
}
