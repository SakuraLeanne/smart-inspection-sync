package com.example.smartinspection.repository;

import com.example.smartinspection.domain.CustomerServiceRow;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MysqlCustomerServiceWriter {
    private final @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate;
    public MysqlCustomerServiceWriter(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) { this.mysqlJdbcTemplate = mysqlJdbcTemplate; }
    public int[] upsertBatch(List<CustomerServiceRow> rows, String batchNo) {
        String sql = "INSERT INTO cus_raw_customer_service (source_id,voucher_no,region_id,service_source,customer_name,phone,finish_time_required,accepted_date,details,service_type,comment,comment_content,complaint_status,confirm_time,sync_batch_no,sync_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW()) ON DUPLICATE KEY UPDATE voucher_no=VALUES(voucher_no),region_id=VALUES(region_id),service_source=VALUES(service_source),customer_name=VALUES(customer_name),phone=VALUES(phone),finish_time_required=VALUES(finish_time_required),accepted_date=VALUES(accepted_date),details=VALUES(details),service_type=VALUES(service_type),comment=VALUES(comment),comment_content=VALUES(comment_content),complaint_status=VALUES(complaint_status),confirm_time=VALUES(confirm_time),sync_batch_no=VALUES(sync_batch_no),sync_time=NOW()";
        return mysqlJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() { public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException { CustomerServiceRow r=rows.get(i); ps.setInt(1,r.getId()); ps.setString(2,r.getVoucherNo()); ps.setObject(3,r.getRegionId()); ps.setObject(4,r.getServiceSource()); ps.setString(5,r.getCustomerName()); ps.setString(6,r.getPhone()); ps.setTimestamp(7,toTs(r.getFinishTimeRequired())); ps.setTimestamp(8,toTs(r.getAcceptedDate())); ps.setString(9,r.getDetails()); ps.setObject(10,r.getServiceType()); ps.setObject(11,r.getComment()); ps.setString(12,r.getCommentContent()); ps.setObject(13,r.getComplaintStatus()); ps.setTimestamp(14,toTs(r.getConfirmTime())); ps.setString(15,batchNo);} public int getBatchSize(){return rows.size();}});
    }
    private Timestamp toTs(java.time.LocalDateTime t){return t==null?null:Timestamp.valueOf(t);} }
