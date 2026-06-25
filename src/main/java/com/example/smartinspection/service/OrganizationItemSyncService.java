package com.example.smartinspection.service;

import com.example.smartinspection.domain.OrganizationItemRow;
import com.example.smartinspection.domain.SyncCheckpoint;
import com.example.smartinspection.repository.MysqlOrganizationItemWriter;
import com.example.smartinspection.repository.SqlServerOrganizationItemReader;
import com.example.smartinspection.repository.SyncCheckpointRepository;
import com.example.smartinspection.repository.SyncTaskLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrganizationItemSyncService {
    private final SqlServerOrganizationItemReader reader;
    private final MysqlOrganizationItemWriter writer;
    private final SyncTaskLogRepository log;
    private final SyncCheckpointRepository checkpointRepository;

    @Value("${sync.batch-size.organization-item:3000}")
    private int batchSize;

    public OrganizationItemSyncService(SqlServerOrganizationItemReader reader,
                                       MysqlOrganizationItemWriter writer,
                                       SyncTaskLogRepository log,
                                       SyncCheckpointRepository checkpointRepository) {
        this.reader = reader;
        this.writer = writer;
        this.log = log;
        this.checkpointRepository = checkpointRepository;
    }

    public void syncFull() {
        String task = "sync_organization_item_full";
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        long logId = log.start(task, batchNo, "FULL");
        int read = 0;
        int write = 0;
        int last = 0;
        try {
            while (true) {
                List<OrganizationItemRow> rows = reader.readByIdGreaterThan(last, batchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                writer.upsertBatch(rows, batchNo);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
            }
            log.finishSuccess(logId, read, write);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }

    /**
     * 增量方案：OrganizationItem 分两条通道同步。
     * 1) UpdateTime 非空：使用“UpdateTime + Id”组合水位，覆盖组织项名称、状态、层级等修改；
     * 2) UpdateTime 为空：只能用 Id 高水位捕获新增，老数据修改需要定期分段重刷兜底。
     */
    public void syncIncremental() {
        String task = "sync_organization_item_incremental";
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        long logId = log.start(task, batchNo, "INCREMENTAL");
        int read = 0;
        int write = 0;
        try {
            SyncCheckpoint updateCheckpoint = checkpointRepository.getOrDefault(task, "OrganizationItem", "UPDATE_TIME_ID");
            LocalDateTime lastTime = updateCheckpoint.getLastTime() == null
                    ? LocalDateTime.of(1900, 1, 1, 0, 0)
                    : updateCheckpoint.getLastTime();
            int lastUpdateId = updateCheckpoint.getLastId() == null ? 0 : updateCheckpoint.getLastId();
            while (true) {
                List<OrganizationItemRow> rows = reader.readByUpdateTimeAfter(lastTime, lastUpdateId, batchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                writer.upsertBatch(rows, batchNo);
                write += rows.size();
                OrganizationItemRow lastRow = rows.get(rows.size() - 1);
                lastTime = lastRow.getUpdateTime();
                lastUpdateId = lastRow.getId();
                checkpointRepository.save(task, "OrganizationItem", "UPDATE_TIME_ID", lastUpdateId, lastTime);
            }

            int lastNullId = checkpointRepository.getOrDefault(task, "OrganizationItem", "NULL_UPDATE_TIME_ID").getLastId();
            while (true) {
                List<OrganizationItemRow> rows = reader.readNullUpdateTimeByIdGreaterThan(lastNullId, batchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                writer.upsertBatch(rows, batchNo);
                write += rows.size();
                lastNullId = Math.max(lastNullId, rows.get(rows.size() - 1).getId());
                checkpointRepository.save(task, "OrganizationItem", "NULL_UPDATE_TIME_ID", lastNullId, null);
            }
            log.finishSuccess(logId, read, write);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }
}
