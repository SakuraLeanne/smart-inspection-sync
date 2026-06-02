package com.example.smartinspection.repository;

import com.example.smartinspection.domain.CustomerServiceRow;
import com.example.smartinspection.service.SyncDateConvertService;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SqlServerCustomerServiceReader {
    private final @Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate; private final SyncDateConvertService convertService;
    public SqlServerCustomerServiceReader(@Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate, SyncDateConvertService convertService) { this.sqlServerJdbcTemplate = sqlServerJdbcTemplate; this.convertService = convertService; }
    public List<CustomerServiceRow> readByIdGreaterThan(int lastMaxId, int limit) {
        String sql = "SELECT TOP (?) * FROM dbo.CustomerService WHERE Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> MapperUtils.mapCustomerService(rs, convertService), limit, lastMaxId);
    }
}
