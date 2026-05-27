package com.example.smartinspection.repository;

import com.example.smartinspection.domain.OrganizationItemRow;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MysqlOrganizationItemWriter {
    private final @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate;

    public MysqlOrganizationItemWriter(@Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }

    public int[] upsertBatch(List<OrganizationItemRow> rows, String batchNo) {
        String sql = "INSERT INTO cus_raw_organization_item (source_id,parent_id,name,code,item_type,address,tel,remark,region_id,building_id,company_id,group_id,unit_id,city_id,building_type,floors,floor,purpose,house_status,house_state,house_type_id,discriminator,hierarchical_path,contact,contact_phone,phone,email,is_virtual,is_disabled,is_rentable,is_rented,is_sold,is_lease_out,uuid,number,terminal_code,brand_code,check_in_date,handover_date,sold_time,update_time,sync_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW()) ON DUPLICATE KEY UPDATE parent_id=VALUES(parent_id),name=VALUES(name),code=VALUES(code),item_type=VALUES(item_type),address=VALUES(address),tel=VALUES(tel),remark=VALUES(remark),region_id=VALUES(region_id),building_id=VALUES(building_id),company_id=VALUES(company_id),group_id=VALUES(group_id),unit_id=VALUES(unit_id),city_id=VALUES(city_id),building_type=VALUES(building_type),floors=VALUES(floors),floor=VALUES(floor),purpose=VALUES(purpose),house_status=VALUES(house_status),house_state=VALUES(house_state),house_type_id=VALUES(house_type_id),discriminator=VALUES(discriminator),hierarchical_path=VALUES(hierarchical_path),contact=VALUES(contact),contact_phone=VALUES(contact_phone),phone=VALUES(phone),email=VALUES(email),is_virtual=VALUES(is_virtual),is_disabled=VALUES(is_disabled),is_rentable=VALUES(is_rentable),is_rented=VALUES(is_rented),is_sold=VALUES(is_sold),is_lease_out=VALUES(is_lease_out),uuid=VALUES(uuid),number=VALUES(number),terminal_code=VALUES(terminal_code),brand_code=VALUES(brand_code),check_in_date=VALUES(check_in_date),handover_date=VALUES(handover_date),sold_time=VALUES(sold_time),update_time=VALUES(update_time),sync_time=NOW()";
        return mysqlJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                OrganizationItemRow r = rows.get(i);
                int c = 1;
                ps.setInt(c++, r.getId()); ps.setObject(c++, r.getParentId()); ps.setString(c++, r.getName()); ps.setString(c++, r.getCode()); ps.setObject(c++, r.getItemType());
                ps.setString(c++, r.getAddress()); ps.setString(c++, r.getTel()); ps.setString(c++, r.getRemark()); ps.setObject(c++, r.getRegionId()); ps.setObject(c++, r.getBuildingId());
                ps.setObject(c++, r.getCompanyId()); ps.setObject(c++, r.getGroupId()); ps.setObject(c++, r.getUnitId()); ps.setObject(c++, r.getCityId()); ps.setObject(c++, r.getBuildingType());
                ps.setObject(c++, r.getFloors()); ps.setObject(c++, r.getFloor()); ps.setObject(c++, r.getPurpose()); ps.setObject(c++, r.getHouseStatus()); ps.setObject(c++, r.getHouseState());
                ps.setObject(c++, r.getHouseTypeId()); ps.setString(c++, r.getDiscriminator()); ps.setString(c++, r.getHierarchicalPath()); ps.setString(c++, r.getContact()); ps.setString(c++, r.getContactPhone());
                ps.setString(c++, r.getPhone()); ps.setString(c++, r.getEmail()); ps.setObject(c++, r.getIsVirtual()); ps.setObject(c++, r.getIsDisabled()); ps.setObject(c++, r.getIsRentable());
                ps.setObject(c++, r.getIsRented()); ps.setObject(c++, r.getIsSold()); ps.setObject(c++, r.getIsLeaseOut()); ps.setString(c++, r.getUuid()); ps.setString(c++, r.getNumber());
                ps.setString(c++, r.getTerminalCode()); ps.setString(c++, r.getBrandCode()); ps.setTimestamp(c++, toTs(r.getCheckInDate())); ps.setTimestamp(c++, toTs(r.getHandoverDate()));
                ps.setTimestamp(c++, toTs(r.getSoldTime())); ps.setTimestamp(c++, toTs(r.getUpdateTime()));
            }
            public int getBatchSize() { return rows.size(); }
        });
    }

    private Timestamp toTs(java.time.LocalDateTime t) { return t == null ? null : Timestamp.valueOf(t); }
}
