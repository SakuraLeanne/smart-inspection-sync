-- 首次全量初始化（推荐）
-- 1) 可选：清空原始表（谨慎）
-- TRUNCATE TABLE cus_raw_customer_service_history;
-- TRUNCATE TABLE cus_raw_customer_service;

-- 2) 清空增量断点（必须）
DELETE FROM cus_sync_task_checkpoint
WHERE task_code IN ('sync_customer_service_new','sync_customer_service_history_new');

-- 3) 启动应用并开启全量参数：
-- java -jar app.jar --sync.full-init.enabled=true
-- 如果需要先清空原始表：
-- java -jar app.jar --sync.full-init.enabled=true --sync.full-init.truncate-before-run=true

-- 4) 查看执行日志
SELECT id, task_code, sync_type, status, read_count, write_count, start_time, end_time
FROM cus_sync_task_log
ORDER BY id DESC
LIMIT 20;

-- 5) 如希望连接异常时直接启动失败（用于CI/发布门禁），增加参数
-- java -jar app.jar --sync.full-init.enabled=true --sync.full-init.fail-fast=true
