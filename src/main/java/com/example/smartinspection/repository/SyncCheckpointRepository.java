package com.example.smartinspection.repository;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SyncCheckpointRepository {
    private final @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate;
    public SyncCheckpointRepository(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) { this.mysqlJdbcTemplate = mysqlJdbcTemplate; }
    public int getLastMaxId(String taskCode) {
        Integer val = mysqlJdbcTemplate.query("SELECT last_max_id FROM cus_sync_task_checkpoint WHERE task_code=?", rs -> rs.next()?rs.getInt(1):0, taskCode);
        return val == null ? 0 : val;
    }
    public void saveOrUpdate(String taskCode, int lastMaxId) {
        mysqlJdbcTemplate.update("INSERT INTO cus_sync_task_checkpoint(task_code,last_max_id,last_sync_time) VALUES(?,?,?) ON DUPLICATE KEY UPDATE last_max_id=VALUES(last_max_id),last_sync_time=VALUES(last_sync_time)", taskCode, lastMaxId, java.sql.Timestamp.valueOf(LocalDateTime.now()));
    }

    public LocalDateTime getLastSyncTime(String taskCode) {
        return mysqlJdbcTemplate.query("SELECT last_sync_time FROM cus_sync_task_checkpoint WHERE task_code=?", rs -> rs.next() && rs.getTimestamp(1) != null ? rs.getTimestamp(1).toLocalDateTime() : null, taskCode);
    }

    public void saveOrUpdateTime(String taskCode, LocalDateTime lastSyncTime) {
        mysqlJdbcTemplate.update("INSERT INTO cus_sync_task_checkpoint(task_code,last_max_id,last_sync_time) VALUES(?,?,?) ON DUPLICATE KEY UPDATE last_sync_time=VALUES(last_sync_time)", taskCode, 0, java.sql.Timestamp.valueOf(lastSyncTime));
    }
}
