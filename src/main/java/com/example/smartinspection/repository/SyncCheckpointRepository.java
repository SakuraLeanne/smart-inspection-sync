package com.example.smartinspection.repository;

import com.example.smartinspection.domain.SyncCheckpoint;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SyncCheckpointRepository {
    private final @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate;

    public SyncCheckpointRepository(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }

    /**
     * 读取一张源表某一种增量方案的成功水位；没有水位时返回默认空水位，便于首次增量从 0 开始。
     */
    public SyncCheckpoint getOrDefault(String taskCode, String sourceTable, String checkpointType) {
        String sql = "SELECT task_code,source_table,checkpoint_type,last_id,last_time FROM cus_sync_checkpoint "
                + "WHERE task_code=? AND source_table=? AND checkpoint_type=?";
        List<SyncCheckpoint> rows = mysqlJdbcTemplate.query(sql, (rs, rn) -> {
            SyncCheckpoint checkpoint = new SyncCheckpoint();
            checkpoint.setTaskCode(rs.getString("task_code"));
            checkpoint.setSourceTable(rs.getString("source_table"));
            checkpoint.setCheckpointType(rs.getString("checkpoint_type"));
            Integer lastId = (Integer) rs.getObject("last_id");
            checkpoint.setLastId(lastId == null ? 0 : lastId);
            Timestamp lastTime = rs.getTimestamp("last_time");
            checkpoint.setLastTime(lastTime == null ? null : lastTime.toLocalDateTime());
            return checkpoint;
        }, taskCode, sourceTable, checkpointType);
        if (!rows.isEmpty()) {
            return rows.get(0);
        }
        SyncCheckpoint checkpoint = new SyncCheckpoint();
        checkpoint.setTaskCode(taskCode);
        checkpoint.setSourceTable(sourceTable);
        checkpoint.setCheckpointType(checkpointType);
        checkpoint.setLastId(0);
        return checkpoint;
    }

    /**
     * 只有当前批次数据写入 MySQL 成功后才推进水位，避免任务失败后跳过未落库的数据。
     */
    public void save(String taskCode, String sourceTable, String checkpointType, Integer lastId, LocalDateTime lastTime) {
        String sql = "INSERT INTO cus_sync_checkpoint(task_code,source_table,checkpoint_type,last_id,last_time,last_success_time) "
                + "VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE last_id=VALUES(last_id),last_time=VALUES(last_time),"
                + "last_success_time=VALUES(last_success_time),update_time=NOW()";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        mysqlJdbcTemplate.update(sql, taskCode, sourceTable, checkpointType, lastId, toTs(lastTime), now);
    }

    private Timestamp toTs(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }
}
