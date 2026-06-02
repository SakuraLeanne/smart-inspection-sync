package com.example.smartinspection.service;

import com.example.smartinspection.domain.MaterialsInventoryRequestDetailRow;
import com.example.smartinspection.domain.MaterialsInventoryRequestRow;
import com.example.smartinspection.repository.MysqlMaterialsInventoryRequestDetailWriter;
import com.example.smartinspection.repository.MysqlMaterialsInventoryRequestWriter;
import com.example.smartinspection.repository.SqlServerMaterialsInventoryRequestDetailReader;
import com.example.smartinspection.repository.SqlServerMaterialsInventoryRequestReader;
import com.example.smartinspection.repository.SyncTaskLogRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MaterialsInventoryRequestFullSyncService {
    private final SqlServerMaterialsInventoryRequestReader requestReader;
    private final MysqlMaterialsInventoryRequestWriter requestWriter;
    private final SqlServerMaterialsInventoryRequestDetailReader detailReader;
    private final MysqlMaterialsInventoryRequestDetailWriter detailWriter;
    private final SyncTaskLogRepository log;

    @Value("${sync.batch-size.materials-inventory-request:300}")
    private int requestBatchSize;

    @Value("${sync.batch-size.materials-inventory-request-detail:500}")
    private int detailBatchSize;

    @Value("${sync.throttle-sleep-ms.materials-inventory-request:100}")
    private long sleepMillisAfterBatch;

    public MaterialsInventoryRequestFullSyncService(SqlServerMaterialsInventoryRequestReader requestReader,
                                                    MysqlMaterialsInventoryRequestWriter requestWriter,
                                                    SqlServerMaterialsInventoryRequestDetailReader detailReader,
                                                    MysqlMaterialsInventoryRequestDetailWriter detailWriter,
                                                    SyncTaskLogRepository log) {
        this.requestReader = requestReader;
        this.requestWriter = requestWriter;
        this.detailReader = detailReader;
        this.detailWriter = detailWriter;
        this.log = log;
    }

    public void syncFull() {
        syncRequestsFull();
        syncDetailsFull();
    }

    private void syncRequestsFull() {
        String task = "sync_materials_inventory_request_full";
        String batchNo = newBatchNo();
        long logId = log.start(task, batchNo, "FULL");
        int read = 0;
        int write = 0;
        int last = 0;
        try {
            while (true) {
                List<MaterialsInventoryRequestRow> rows = requestReader.readByIdGreaterThan(last, requestBatchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                requestWriter.upsertBatch(rows);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
                sleepQuietly();
            }
            log.finishSuccess(logId, read, write);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }

    private void syncDetailsFull() {
        String task = "sync_materials_inventory_request_detail_full";
        String batchNo = newBatchNo();
        long logId = log.start(task, batchNo, "FULL");
        int read = 0;
        int write = 0;
        int last = 0;
        try {
            while (true) {
                List<MaterialsInventoryRequestDetailRow> rows = detailReader.readByIdGreaterThan(last, detailBatchSize);
                if (rows.isEmpty()) {
                    break;
                }
                read += rows.size();
                detailWriter.upsertBatch(rows);
                write += rows.size();
                last = Math.max(last, rows.get(rows.size() - 1).getId());
                sleepQuietly();
            }
            log.finishSuccess(logId, read, write);
        } catch (RuntimeException e) {
            log.finishFail(logId, read, write, e.getMessage());
            throw e;
        }
    }

    private String newBatchNo() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    private void sleepQuietly() {
        if (sleepMillisAfterBatch <= 0) {
            return;
        }
        try {
            Thread.sleep(sleepMillisAfterBatch);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("materials inventory request full sync interrupted", e);
        }
    }
}
