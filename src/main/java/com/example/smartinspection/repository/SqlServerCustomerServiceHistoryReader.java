package com.example.smartinspection.repository;

import com.example.smartinspection.domain.CustomerServiceHistoryRow;
import com.example.smartinspection.service.SyncDateConvertService;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SqlServerCustomerServiceHistoryReader {
    private final JdbcTemplate sqlServerJdbcTemplate; private final SyncDateConvertService convertService;
    public SqlServerCustomerServiceHistoryReader(JdbcTemplate sqlServerJdbcTemplate, SyncDateConvertService convertService) { this.sqlServerJdbcTemplate = sqlServerJdbcTemplate; this.convertService = convertService; }
    public List<CustomerServiceHistoryRow> readNewById(int lastMaxId, int limit) {
        String sql = "SELECT TOP (?) * FROM dbo.CustomerServiceHistory WHERE Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> MapperUtils.mapCustomerServiceHistory(rs, convertService), limit, lastMaxId);
    }
    public List<CustomerServiceHistoryRow> readRecent(int days, int offset, int limit) {
        String sql = "SELECT * FROM dbo.CustomerServiceHistory WHERE CreateDate >= DATEADD(day, -?, GETDATE()) ORDER BY Id ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> MapperUtils.mapCustomerServiceHistory(rs, convertService), days, offset, limit);
    }
}
