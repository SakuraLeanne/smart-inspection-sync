-- 全量同步接口调用说明
-- 应用启动后，通过 HTTP 接口手动触发同步；当前支持全量同步和基于 cus_sync_checkpoint 的增量同步。

-- 1) 首次使用前，执行 init_sync_tables.sql 创建 5 张原始同步表及 cus_sync_task_log。

-- 2) 单表全量同步：
-- curl -X POST http://localhost:8080/api/sync/customer-service/full
-- curl -X POST http://localhost:8080/api/sync/customer-service-history/full
-- curl -X POST http://localhost:8080/api/sync/organization-item/full
-- curl -X POST http://localhost:8080/api/sync/materials-inventory-request/full

-- 3) 全部原始表全量同步（按组织、工单、工单历史、物料申请/明细顺序执行）：
-- curl -X POST http://localhost:8080/api/sync/full

-- 4) 执行 verify_sync.sql 查看原始同步表行数、最大 source_id 与最近任务日志。

-- 5) 单表增量同步：
-- CustomerService：Id 高水位 + CreateTime 回溯窗口。
-- curl -X POST http://localhost:8080/api/sync/customer-service/incremental
-- CustomerServiceHistory：Id 高水位。
-- curl -X POST http://localhost:8080/api/sync/customer-service-history/incremental
-- OrganizationItem：UpdateTime 非空使用 UpdateTime+Id，UpdateTime 为空使用 Id 高水位。
-- curl -X POST http://localhost:8080/api/sync/organization-item/incremental
-- MaterialsInventoryRequest/Detail：主表 Id 高水位 + RequestDate 回溯；明细 Id 高水位 + 跟随主表 Id 重刷。
-- curl -X POST http://localhost:8080/api/sync/materials-inventory-request/incremental

-- 6) 全部原始表增量同步：
-- curl -X POST http://localhost:8080/api/sync/incremental
