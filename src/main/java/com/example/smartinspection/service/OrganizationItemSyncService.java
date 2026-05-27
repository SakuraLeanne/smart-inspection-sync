package com.example.smartinspection.service;

import com.example.smartinspection.domain.OrganizationItemRow;
import com.example.smartinspection.repository.*;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrganizationItemSyncService {
    private final SqlServerOrganizationItemReader reader;
    private final MysqlOrganizationItemWriter writer;
    private final SyncCheckpointRepository checkpoint;
    private final SyncTaskLogRepository log;
    @Value("${sync.batch-size.organization-item:3000}")
    private int batchSize;

    public OrganizationItemSyncService(SqlServerOrganizationItemReader r, MysqlOrganizationItemWriter w, SyncCheckpointRepository c, SyncTaskLogRepository l) {
        reader = r;
        writer = w;
        checkpoint = c;
        log = l;
    }

    public void syncFullById() {
        String task = "sync_organization_item_full";
        String b = UUID.randomUUID().toString().replace("-", "");
        long logId = log.start(task, b, "FULL");
        int read = 0, write = 0, last = checkpoint.getLastMaxId(task);
        try {
            while (true) {
                List<OrganizationItemRow> rows = reader.readNewById(last, batchSize);
                if (rows.isEmpty()) break;
                read += rows.size();
                writer.upsertBatch(rows, b);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
            }
            checkpoint.saveOrUpdate(task, last);
            log.finishSuccess(logId, read, write);
        } catch (Exception e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }

    public void syncIncrementByUpdateTime() {
        String task = "sync_organization_item_increment";
        String b = UUID.randomUUID().toString().replace("-", "");
        long logId = log.start(task, b, "INCREMENT");
        int read = 0, write = 0;
        LocalDateTime lastSync = checkpoint.getLastSyncTime(task);
        String lastSyncStr = lastSync == null ? "1900-01-01 00:00:00" : lastSync.toString().replace('T', ' ');
        LocalDateTime max = lastSync;
        try {
            while (true) {
                List<OrganizationItemRow> rows = reader.readByUpdateTime(lastSyncStr, batchSize);
                if (rows.isEmpty()) break;
                read += rows.size();
                writer.upsertBatch(rows, b);
                write += rows.size();
                max = rows.get(rows.size() - 1).getUpdateTime();
                lastSyncStr = max.toString().replace('T', ' ');
            }
            checkpoint.saveOrUpdateTime(task, max == null ? LocalDateTime.now() : max);
            log.finishSuccess(logId, read, write);
        } catch (Exception e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }
}
