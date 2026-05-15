package com.example.smartinspection.job;

import com.example.smartinspection.service.CustomerServiceHistorySyncService;
import com.example.smartinspection.service.CustomerServiceSyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceSyncJob {
    private final CustomerServiceSyncService customerServiceSyncService;
    private final CustomerServiceHistorySyncService historySyncService;
    public CustomerServiceSyncJob(CustomerServiceSyncService c, CustomerServiceHistorySyncService h){this.customerServiceSyncService=c;this.historySyncService=h;}

    @Scheduled(cron = "${sync.schedule.customer-service-new}")
    public void syncCustomerServiceNew(){ customerServiceSyncService.syncNew(); }
    @Scheduled(cron = "${sync.schedule.customer-service-history-new}")
    public void syncCustomerServiceHistoryNew(){ historySyncService.syncNew(); }
    @Scheduled(cron = "${sync.schedule.customer-service-recent}")
    public void refreshCustomerServiceRecent(){ customerServiceSyncService.refreshRecent(); }
    @Scheduled(cron = "${sync.schedule.customer-service-history-recent}")
    public void refreshCustomerServiceHistoryRecent(){ historySyncService.refreshRecent(); }
}
