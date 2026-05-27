package com.example.smartinspection.repository;

import com.example.smartinspection.domain.OrganizationItemRow;
import com.example.smartinspection.service.SyncDateConvertService;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SqlServerOrganizationItemReader {
    private final @Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate;
    private final SyncDateConvertService convertService;

    public SqlServerOrganizationItemReader(@Qualifier("sqlServerJdbcTemplate") JdbcTemplate sqlServerJdbcTemplate,
                                           SyncDateConvertService convertService) {
        this.sqlServerJdbcTemplate = sqlServerJdbcTemplate;
        this.convertService = convertService;
    }

    public List<OrganizationItemRow> readNewById(int lastMaxId, int limit) {
        String sql = "SELECT TOP (?) Id, Name, UpdateTime FROM dbo.OrganizationItem WHERE Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> {
            OrganizationItemRow row = new OrganizationItemRow();
            row.setId(rs.getInt("Id"));
            row.setName(rs.getString("Name"));
            row.setUpdateTime(convertService.toLocalDateTime(rs.getTimestamp("UpdateTime")));
            return row;
        }, limit, lastMaxId);
    }

    public List<OrganizationItemRow> readByUpdateTime(String lastSyncTime, int limit) {
        String sql = "SELECT TOP (?) Id, Name, UpdateTime FROM dbo.OrganizationItem WHERE UpdateTime > ? ORDER BY UpdateTime ASC, Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> {
            OrganizationItemRow row = new OrganizationItemRow();
            row.setId(rs.getInt("Id"));
            row.setName(rs.getString("Name"));
            row.setUpdateTime(convertService.toLocalDateTime(rs.getTimestamp("UpdateTime")));
            return row;
        }, limit, lastSyncTime);
    }
}
