package com.example.smartinspection.job;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/sync")
public class SyncTriggerController {
    private final CustomerServiceSyncJob customerServiceSyncJob;

    public SyncTriggerController(CustomerServiceSyncJob customerServiceSyncJob) {
        this.customerServiceSyncJob = customerServiceSyncJob;
    }

    @PostMapping("/organization-item/full")
    public Map<String, Object> triggerOrganizationItemFullSync() {
        customerServiceSyncJob.syncOrganizationItemFull();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "organization item full sync triggered");
        resp.put("triggerTime", LocalDateTime.now().toString());
        return resp;
    }
}
