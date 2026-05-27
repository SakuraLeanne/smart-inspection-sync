package com.example.smartinspection.repository;

import com.example.smartinspection.domain.OrganizationItemRow;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MysqlOrganizationItemWriter {
    private final @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate;

    public MysqlOrganizationItemWriter(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }

    public int[] upsertBatch(List<OrganizationItemRow> rows, String batchNo) {
        String sql = "INSERT INTO cus_raw_organization_item (source_id,name,update_time,sync_batch_no,sync_time) VALUES (?,?,?,?,NOW()) " +
            "ON DUPLICATE KEY UPDATE name=VALUES(name),update_time=VALUES(update_time),sync_batch_no=VALUES(sync_batch_no),sync_time=NOW()";
        return mysqlJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                OrganizationItemRow r = rows.get(i);
                ps.setInt(1, r.getId());
                ps.setString(2, r.getName());
                ps.setTimestamp(3, r.getUpdateTime() == null ? null : Timestamp.valueOf(r.getUpdateTime()));
                ps.setString(4, batchNo);
            }
            public int getBatchSize() { return rows.size(); }
        });
    }
}
