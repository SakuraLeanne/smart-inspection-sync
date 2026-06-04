package com.example.smartinspection.service;

import com.example.smartinspection.domain.OrganizationItemRow;
import com.example.smartinspection.repository.MysqlOrganizationItemWriter;
import com.example.smartinspection.repository.SqlServerOrganizationItemReader;
import com.example.smartinspection.repository.SyncTaskLogRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrganizationItemSyncService {
    private final SqlServerOrganizationItemReader reader;
    private final MysqlOrganizationItemWriter writer;
    private final SyncTaskLogRepository log;

    @Value("${sync.batch-size.organization-item:3000}")
    private int batchSize;

    public OrganizationItemSyncService(SqlServerOrganizationItemReader reader,
                                       MysqlOrganizationItemWriter writer,
                                       SyncTaskLogRepository log) {
        this.reader = reader;
        this.writer = writer;
        this.log = log;
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
}
