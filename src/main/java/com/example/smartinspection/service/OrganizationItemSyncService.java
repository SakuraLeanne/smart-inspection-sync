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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrganizationItemSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationItemSyncService.class);

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
     * 增量方案：仅按 OrganizationItem.Id 高水位同步新增组织项。
     * 已同步组织项即使源端 UpdateTime 变化或字段被修改，也不做回刷或覆盖。
     */
    public void syncIncremental() {
        String task = "sync_organization_item_incremental";
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        long logId = log.start(task, batchNo, "INCREMENTAL");
        int read = 0;
        int write = 0;
        int last = checkpointRepository.getOrDefault(task, "OrganizationItem", "ID").getLastId();
        try {
            LOGGER.info("Start incremental sync task={}, batchNo={}, strategy=ID_WATERMARK_ONLY, lastId={}",
                    task, batchNo, last);
            while (true) {
                List<OrganizationItemRow> rows = reader.readByIdGreaterThan(last, batchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                writer.upsertBatch(rows, batchNo);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
                checkpointRepository.save(task, "OrganizationItem", "ID", last, null);
            }
            log.finishSuccess(logId, read, write);
            LOGGER.info("Finish incremental sync task={}, batchNo={}, status=SUCCESS, read={}, write={}, finalLastId={}",
                    task, batchNo, read, write, last);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            LOGGER.error("Finish incremental sync task={}, batchNo={}, status=FAILED, read={}, write={}",
                    task, batchNo, read, write, e);
            throw e;
        }
    }
}
