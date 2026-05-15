package com.example.smartinspection.service;

import com.example.smartinspection.domain.CustomerServiceRow;
import com.example.smartinspection.repository.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceSyncService {
    private final SqlServerCustomerServiceReader reader; private final MysqlCustomerServiceWriter writer; private final SyncCheckpointRepository checkpoint; private final SyncTaskLogRepository log;
    @Value("${sync.batch-size.customer-service:3000}") private int batchSize; @Value("${sync.recent-refresh-days.customer-service:90}") private int recentDays;
    public CustomerServiceSyncService(SqlServerCustomerServiceReader r,MysqlCustomerServiceWriter w,SyncCheckpointRepository c,SyncTaskLogRepository l){reader=r;writer=w;checkpoint=c;log=l;}
    public void syncNew(){ run("sync_customer_service_new","INCREMENT",true); }
    public void refreshRecent(){ run("refresh_customer_service_recent","REFRESH",false); }
    private void run(String task,String type,boolean useCheckpoint){ String b=UUID.randomUUID().toString().replace("-",""); long logId=log.start(task,b,type); int read=0,write=0,last=useCheckpoint?checkpoint.getLastMaxId(task):0; int offset=0; try{ while(true){ List<CustomerServiceRow> rows=useCheckpoint?reader.readNewById(last,batchSize):reader.readRecent(recentDays,offset,batchSize); if(rows.isEmpty()) break; read+=rows.size(); writer.upsertBatch(rows,b); write+=rows.size(); last=Math.max(last,rows.get(rows.size()-1).getId()); offset+=rows.size(); } if(useCheckpoint) checkpoint.saveOrUpdate(task,last); log.finishSuccess(logId,read,write);}catch(Exception e){log.finishFail(logId,read,write,e.getMessage()); throw e;}}
}
