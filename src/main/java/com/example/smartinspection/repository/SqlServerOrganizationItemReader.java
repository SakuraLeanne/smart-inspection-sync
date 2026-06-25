package com.example.smartinspection.repository;

import com.example.smartinspection.domain.OrganizationItemRow;
import com.example.smartinspection.service.SyncDateConvertService;
import java.time.LocalDateTime;
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

    public List<OrganizationItemRow> readByIdGreaterThan(int lastMaxId, int limit) {
        String sql = "SELECT TOP (?) Id,ParentId,Name,Code,ItemType,Address,Tel,Remark,RegionId,BuildingId,CompanyId,GroupId,UnitId,CityId,BuildingType,Floors,Floor,Purpose,HouseStatus,HouseState,HouseTypeId,Discriminator,HierarchicalPath,Contact,ContactPhone,Phone,Email,IsVirtual,IsDisabled,IsRentable,IsRented,IsSold,IsLeaseOut,Uuid,Number,TerminalCode,BrandCode,CheckInDate,HandoverDate,SoldTime,UpdateTime FROM dbo.OrganizationItem WHERE Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> mapRow(rs), limit, lastMaxId);
    }

    /**
     * OrganizationItem 的主增量通道：UpdateTime 非空的数据用“UpdateTime + Id”组合水位，
     * 避免同一更新时间下有多条记录时仅按时间推进造成漏数。
     */
    public List<OrganizationItemRow> readByUpdateTimeAfter(LocalDateTime lastUpdateTime, int lastId, int limit) {
        String sql = "SELECT TOP (?) Id,ParentId,Name,Code,ItemType,Address,Tel,Remark,RegionId,BuildingId,CompanyId,GroupId,UnitId,CityId,BuildingType,Floors,Floor,Purpose,HouseStatus,HouseState,HouseTypeId,Discriminator,HierarchicalPath,Contact,ContactPhone,Phone,Email,IsVirtual,IsDisabled,IsRentable,IsRented,IsSold,IsLeaseOut,Uuid,Number,TerminalCode,BrandCode,CheckInDate,HandoverDate,SoldTime,UpdateTime FROM dbo.OrganizationItem WHERE UpdateTime IS NOT NULL AND (UpdateTime > ? OR (UpdateTime = ? AND Id > ?)) ORDER BY UpdateTime ASC, Id ASC";
        java.sql.Timestamp lastTime = java.sql.Timestamp.valueOf(lastUpdateTime);
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> mapRow(rs), limit, lastTime, lastTime, lastId);
    }

    /**
     * UpdateTime 为空的数据无法按更新时间识别修改，日常增量只捕获这部分的新 Id。
     * 老数据被修改的场景交给定期分段重刷兜底。
     */
    public List<OrganizationItemRow> readNullUpdateTimeByIdGreaterThan(int lastMaxId, int limit) {
        String sql = "SELECT TOP (?) Id,ParentId,Name,Code,ItemType,Address,Tel,Remark,RegionId,BuildingId,CompanyId,GroupId,UnitId,CityId,BuildingType,Floors,Floor,Purpose,HouseStatus,HouseState,HouseTypeId,Discriminator,HierarchicalPath,Contact,ContactPhone,Phone,Email,IsVirtual,IsDisabled,IsRentable,IsRented,IsSold,IsLeaseOut,Uuid,Number,TerminalCode,BrandCode,CheckInDate,HandoverDate,SoldTime,UpdateTime FROM dbo.OrganizationItem WHERE UpdateTime IS NULL AND Id > ? ORDER BY Id ASC";
        return sqlServerJdbcTemplate.query(sql, (rs, rn) -> mapRow(rs), limit, lastMaxId);
    }


    private OrganizationItemRow mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        OrganizationItemRow row = new OrganizationItemRow();
        row.setId(rs.getInt("Id")); row.setParentId((Integer) rs.getObject("ParentId")); row.setName(rs.getString("Name"));
        row.setCode(rs.getString("Code")); row.setItemType((Integer) rs.getObject("ItemType")); row.setAddress(rs.getString("Address"));
        row.setTel(rs.getString("Tel")); row.setRemark(rs.getString("Remark")); row.setRegionId((Integer) rs.getObject("RegionId"));
        row.setBuildingId((Integer) rs.getObject("BuildingId")); row.setCompanyId((Integer) rs.getObject("CompanyId"));
        row.setGroupId((Integer) rs.getObject("GroupId")); row.setUnitId((Integer) rs.getObject("UnitId")); row.setCityId((Integer) rs.getObject("CityId"));
        row.setBuildingType((Integer) rs.getObject("BuildingType")); row.setFloors((Integer) rs.getObject("Floors")); row.setFloor((Integer) rs.getObject("Floor"));
        row.setPurpose((Integer) rs.getObject("Purpose")); row.setHouseStatus((Integer) rs.getObject("HouseStatus")); row.setHouseState((Integer) rs.getObject("HouseState"));
        row.setHouseTypeId((Integer) rs.getObject("HouseTypeId")); row.setDiscriminator(rs.getString("Discriminator")); row.setHierarchicalPath(rs.getString("HierarchicalPath"));
        row.setContact(rs.getString("Contact")); row.setContactPhone(rs.getString("ContactPhone")); row.setPhone(rs.getString("Phone")); row.setEmail(rs.getString("Email"));
        row.setIsVirtual((Boolean) rs.getObject("IsVirtual")); row.setIsDisabled((Boolean) rs.getObject("IsDisabled")); row.setIsRentable((Boolean) rs.getObject("IsRentable"));
        row.setIsRented((Boolean) rs.getObject("IsRented")); row.setIsSold((Boolean) rs.getObject("IsSold")); row.setIsLeaseOut((Boolean) rs.getObject("IsLeaseOut"));
        row.setUuid(rs.getString("Uuid")); row.setNumber(rs.getString("Number")); row.setTerminalCode(rs.getString("TerminalCode")); row.setBrandCode(rs.getString("BrandCode"));
        row.setCheckInDate(convertService.toLocalDateTime(rs.getTimestamp("CheckInDate"))); row.setHandoverDate(convertService.toLocalDateTime(rs.getTimestamp("HandoverDate")));
        row.setSoldTime(convertService.toLocalDateTime(rs.getTimestamp("SoldTime"))); row.setUpdateTime(convertService.toLocalDateTime(rs.getTimestamp("UpdateTime")));
        return row;
    }
}
