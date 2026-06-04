package com.example.smartinspection.repository;

import com.example.smartinspection.domain.CustomerServiceHistoryRow;
import com.example.smartinspection.service.SyncDateConvertService;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SqlServerCustomerServiceHistoryReader {
    private final @Qualifier("sqlServerJdbcTemplate")
    JdbcTemplate sqlServerJdbcTemplate;
    private final SyncDateConvertService convertService;

    public SqlServerCustomerServiceHistoryReader(@Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate, SyncDateConvertService convertService) {
        this.sqlServerJdbcTemplate = sqlServerJdbcTemplate;
        this.convertService = convertService;
    }

    public List<CustomerServiceHistoryRow> readByIdGreaterThan(int lastMaxId, int limit) {
        String sql = "SELECT TOP (?) * FROM dbo.CustomerServiceHistory WHERE Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> MapperUtils.mapCustomerServiceHistory(rs, convertService), limit, lastMaxId);
    }
}
