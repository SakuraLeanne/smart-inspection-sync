package com.example.smartinspection.repository;

import com.example.smartinspection.domain.CustomerServiceRow;
import com.example.smartinspection.service.SyncDateConvertService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SqlServerCustomerServiceReader {
    private final @Qualifier("sqlServerJdbcTemplate")
    JdbcTemplate sqlServerJdbcTemplate;
    private final SyncDateConvertService convertService;

    public SqlServerCustomerServiceReader(@Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate, SyncDateConvertService convertService) {
        this.sqlServerJdbcTemplate = sqlServerJdbcTemplate;
        this.convertService = convertService;
    }

    public List<CustomerServiceRow> readByIdGreaterThan(int lastMaxId, int limit) {
        String sql = "SELECT TOP (?) * FROM dbo.CustomerService WHERE Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> MapperUtils.mapCustomerService(rs, convertService), limit, lastMaxId);
    }

    /**
     * CustomerService 没有确认可用的更新时间字段，增量方案采用“Id 高水位 + CreateTime 回溯”。
     * 这里按 CreateTime 回溯近期工单，用 upsert 覆盖近期工单的状态、评价、投诉等后续变化。
     */
    public List<CustomerServiceRow> readByCreateTimeSince(LocalDateTime since, int lastId, int limit) {
        String sql = "SELECT TOP (?) * FROM dbo.CustomerService WHERE CreateTime >= ? AND Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> MapperUtils.mapCustomerService(rs, convertService),
                limit, java.sql.Timestamp.valueOf(since), lastId);
    }
}
