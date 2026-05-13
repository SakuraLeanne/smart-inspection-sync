package com.example.smartinspection.job;

import com.example.smartinspection.config.SyncTaskProperties;
import com.example.smartinspection.service.CustomerServiceHistorySyncService;
import com.example.smartinspection.service.CustomerServiceSyncService;
import com.example.smartinspection.service.SyncAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FullInitRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(FullInitRunner.class);
    private final SyncTaskProperties properties;
    private final SyncAdminService adminService;
    private final CustomerServiceSyncService customerServiceSyncService;
    private final CustomerServiceHistorySyncService historySyncService;

    public FullInitRunner(SyncTaskProperties properties, SyncAdminService adminService,
                          CustomerServiceSyncService customerServiceSyncService,
                          CustomerServiceHistorySyncService historySyncService) {
        this.properties = properties;
        this.adminService = adminService;
        this.customerServiceSyncService = customerServiceSyncService;
        this.historySyncService = historySyncService;
    }

    @Override
    public void run(String... args) {
        if (!properties.getFullInit().isEnabled()) {
            return;
        }
        log.info("[FULL-INIT] Start full init process");
        if (properties.getFullInit().isTruncateBeforeRun()) {
            log.warn("[FULL-INIT] truncate raw tables before run");
            adminService.truncateRawTables();
        }
        adminService.resetIncrementCheckpoint();
        customerServiceSyncService.syncNew();
        historySyncService.syncNew();
        log.info("[FULL-INIT] Done");
    }
}
