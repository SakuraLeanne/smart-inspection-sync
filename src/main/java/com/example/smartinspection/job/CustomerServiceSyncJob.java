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

    /**
     * 增量同步工单主表（raw 层）。
     *
     * 任务编码：sync_customer_service_new
     * 逻辑：按 source Id 断点增量拉取 SQL Server CustomerService，批量 upsert 到 cus_raw_customer_service。
     * 说明：该任务会更新 cus_sync_task_checkpoint 中的 last_max_id。
     * Cron 来源：sync.schedule.customer-service-new
     */
//    @Scheduled(cron = "${sync.schedule.customer-service-new}")
    public void syncCustomerServiceNew(){ customerServiceSyncService.syncNew(); }
    /**
     * 增量同步工单处理记录表（raw 层）。
     *
     * 任务编码：sync_customer_service_history_new
     * 逻辑：按 source Id 断点增量拉取 SQL Server CustomerServiceHistory，批量 upsert 到 cus_raw_customer_service_history。
     * 说明：该任务会更新 cus_sync_task_checkpoint 中的 last_max_id。
     * Cron 来源：sync.schedule.customer-service-history-new
     */
//    @Scheduled(cron = "${sync.schedule.customer-service-history-new}")
    public void syncCustomerServiceHistoryNew(){ historySyncService.syncNew(); }
    /**
     * 滚动刷新近 N 天工单主表（raw 层）。
     *
     * 任务编码：refresh_customer_service_recent
     * 逻辑：按 accepted_date 时间窗回刷，覆盖状态变化（已分配/已处理/已关闭等）的工单。
     * 说明：该任务不推进 Id 断点，仅记录同步日志。
     * Cron 来源：sync.schedule.customer-service-recent
     */
//    @Scheduled(cron = "${sync.schedule.customer-service-recent}")
    public void refreshCustomerServiceRecent(){ customerServiceSyncService.refreshRecent(); }
    /**
     * 滚动刷新近 N 天工单处理记录（raw 层）。
     *
     * 任务编码：refresh_customer_service_history_recent
     * 逻辑：按 create_date 时间窗补刷，覆盖迟到/补录的处理记录。
     * 说明：该任务不推进 Id 断点，仅记录同步日志。
     * Cron 来源：sync.schedule.customer-service-history-recent
     */
//    @Scheduled(cron = "${sync.schedule.customer-service-history-recent}")
    public void refreshCustomerServiceHistoryRecent(){ historySyncService.refreshRecent(); }
}
