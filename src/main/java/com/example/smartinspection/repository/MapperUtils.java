package com.example.smartinspection.repository;

import com.example.smartinspection.domain.CustomerServiceHistoryRow;
import com.example.smartinspection.domain.CustomerServiceRow;
import com.example.smartinspection.service.SyncDateConvertService;

import java.sql.ResultSet;
import java.sql.SQLException;

final class MapperUtils {
    private MapperUtils() {
    }

    static CustomerServiceRow mapCustomerService(ResultSet rs, SyncDateConvertService c) throws SQLException {
        CustomerServiceRow r = new CustomerServiceRow();
        r.setId(rs.getInt("Id"));
        r.setVoucherNo(rs.getString("VoucherNo"));
        r.setRegionId((Integer) rs.getObject("RegionId"));
        r.setServiceSource((Integer) rs.getObject("ServiceSource"));
        r.setCustomerName(rs.getString("CustomerName"));
        r.setPhone(rs.getString("Phone"));
        r.setFinishTimeRequired(c.normalize(rs.getTimestamp("FinishTimeRequired")));
        r.setAcceptedDate(c.normalize(rs.getTimestamp("AcceptedDate")));
        r.setDetails(rs.getString("Details"));
        r.setServiceType((Integer) rs.getObject("ServiceType"));
        r.setAppointmentTime(c.normalize(rs.getTimestamp("AppointmentTime")));
        r.setPlace(rs.getString("Place"));
        r.setWeChatOpenId(rs.getString("WeChatOpenId"));
        r.setEvaluation(rs.getString("Evaluation"));
        r.setQuality((Integer) rs.getObject("Quality"));
        r.setAttitude((Integer) rs.getObject("Attitude"));
        r.setPunctuality((Integer) rs.getObject("Punctuality"));
        r.setCreateTime(c.normalize(rs.getTimestamp("CreateTime")));
        r.setComment((Integer) rs.getObject("Comment"));
        r.setCommentContent(rs.getString("CommentContent"));
        r.setComplaintStatus((Integer) rs.getObject("ComplaintStatus"));
        r.setConfirmTime(c.normalize(rs.getTimestamp("ConfirmTime")));
        return r;
    }

    static CustomerServiceHistoryRow mapCustomerServiceHistory(ResultSet rs, SyncDateConvertService c) throws SQLException {
        CustomerServiceHistoryRow r = new CustomerServiceHistoryRow();
        r.setId(rs.getInt("Id"));
        r.setCreatedById(rs.getString("CreatedById"));
        r.setCreateDate(c.normalize(rs.getTimestamp("CreateDate")));
        r.setContent(rs.getString("Content"));
        r.setCustomerServiceId(rs.getInt("CustomerServiceId"));
        r.setOperationStatus((Integer) rs.getObject("OperationStatus"));
        r.setComplaintHandleId((Integer) rs.getObject("ComplaintHandleId"));
        r.setDispatchId((Integer) rs.getObject("DispatchId"));
        r.setWordContent(rs.getString("WordContent"));
        return r;
    }
}
