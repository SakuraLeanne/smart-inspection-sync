package com.example.smartinspection.repository;

import com.example.smartinspection.domain.CustomerServiceHistoryRow;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MysqlCustomerServiceHistoryWriter {
    private final JdbcTemplate mysqlJdbcTemplate;
    public MysqlCustomerServiceHistoryWriter(JdbcTemplate mysqlJdbcTemplate) { this.mysqlJdbcTemplate = mysqlJdbcTemplate; }
    public int[] upsertBatch(List<CustomerServiceHistoryRow> rows, String batchNo) {
        String sql = "INSERT INTO ic_raw_customer_service_history (source_id,created_by_id,create_date,content,customer_service_id,operation_status,complaint_handle_id,dispatch_id,word_content,sync_batch_no,sync_time) VALUES (?,?,?,?,?,?,?,?,?,?,NOW()) ON DUPLICATE KEY UPDATE created_by_id=VALUES(created_by_id),create_date=VALUES(create_date),content=VALUES(content),customer_service_id=VALUES(customer_service_id),operation_status=VALUES(operation_status),complaint_handle_id=VALUES(complaint_handle_id),dispatch_id=VALUES(dispatch_id),word_content=VALUES(word_content),sync_batch_no=VALUES(sync_batch_no),sync_time=NOW()";
        return mysqlJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter(){public void setValues(java.sql.PreparedStatement ps,int i)throws java.sql.SQLException{CustomerServiceHistoryRow r=rows.get(i);ps.setInt(1,r.getId());ps.setString(2,r.getCreatedById());ps.setTimestamp(3,r.getCreateDate()==null?null:Timestamp.valueOf(r.getCreateDate()));ps.setString(4,r.getContent());ps.setInt(5,r.getCustomerServiceId());ps.setObject(6,r.getOperationStatus());ps.setObject(7,r.getComplaintHandleId());ps.setObject(8,r.getDispatchId());ps.setString(9,r.getWordContent());ps.setString(10,batchNo);} public int getBatchSize(){return rows.size();}});
    }
}
