-- 全量同步接口调用说明
-- 应用启动后，通过 HTTP 接口手动触发全量同步；不再支持定时任务、启动参数触发或断点增量同步。

-- 1) 首次使用前，执行 init_sync_tables.sql 创建 5 张原始同步表及 cus_sync_task_log。

-- 2) 单表全量同步：
-- curl -X POST http://localhost:8080/api/sync/customer-service/full
-- curl -X POST http://localhost:8080/api/sync/customer-service-history/full
-- curl -X POST http://localhost:8080/api/sync/organization-item/full
-- curl -X POST http://localhost:8080/api/sync/materials-inventory-request/full

-- 3) 全部原始表全量同步（按组织、工单、工单历史、物料申请/明细顺序执行）：
-- curl -X POST http://localhost:8080/api/sync/full

-- 4) 执行 verify_sync.sql 查看原始同步表行数、最大 source_id 与最近任务日志。
