package com.skyseas.openfireplugins.webservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.XMPPContextListener;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.JiveGlobals;

import com.google.gson.Gson;

public class OpenfireWebServiceImpl implements OpenfireWebService {

	@Override
	public String getAllGroup() {
		BaseResult baseResult = new BaseResult();
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
		con = DbConnectionManager.getConnection();
		String sql = "select * from sky_Group";
		stmt = con.createStatement();
		rs = stmt.executeQuery(sql);
		int  a =XMPPServer.getInstance().getSessionManager().getConnectionsCount(false);
		
		baseResult.setObject(Rs2Map(rs));
		} catch (Exception e) {
			baseResult.setErrorCode(1);
			baseResult.setErrorMessage(e.getMessage());
		} finally {
			DbConnectionManager.closeConnection(rs, stmt, con);
		}
		return new Gson().toJson(baseResult);
	}
	
	@Override
	public String createGroup(String[] jids,String ownerJid,String name,String desc){
		BaseResult baseResult = new BaseResult();
		if(StringUtils.isEmpty(ownerJid)||StringUtils.isEmpty(name)||jids==null ||jids.length<=0){
			baseResult.setErrorCode(1);
			baseResult.setErrorMessage("输入参数不合法!");
		}else{
			Connection con = null;
			PreparedStatement pstmt = null;
			PreparedStatement memberPstmt = null;
			ResultSet rs = null;
			try {
			con = DbConnectionManager.getConnection();
			con.setAutoCommit(false);
			String sql = "insert into "
					+ "sky_Group (name,owner,openness,description,numberOfMembers,status,createTime) "
					+ "VALUES (?,?,?,?,?,?,?)";
			pstmt = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, name);
			pstmt.setString(2, ownerJid);
			pstmt.setByte(3, (byte)2);
			pstmt.setString(4, desc);
			pstmt.setInt(5, jids.length);
			pstmt.setString(6, "0");
			pstmt.setTimestamp(7, new Timestamp(new Date().getTime()));
			if(pstmt.executeUpdate()>0
					&&(rs = pstmt.getGeneratedKeys()).next()){
				Long id = rs.getLong(1); 
				sql = "insert into sky_GroupMembers(groupId,userName,nickName,joinTime)"
						+ " values(?,?,?,?)";
				memberPstmt = con.prepareStatement(sql);
				for(int i = 0;i<jids.length;i++){
					memberPstmt.setLong(1, id);
					memberPstmt.setString(2, jids[i]);
					memberPstmt.setString(3, jids[i]);
					memberPstmt.setTimestamp( 4, new Timestamp(new Date().getTime()));
					memberPstmt.addBatch();
				}
				memberPstmt.executeBatch();
			}
			con.commit();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					con.rollback();
				} catch (SQLException e1) {
				}
				baseResult.setErrorCode(1);
				baseResult.setErrorMessage(e.getMessage());
			} finally {
				DbConnectionManager.closeConnection(rs, pstmt, con);
			}
		}
		return new Gson().toJson(baseResult);
	}

	@Override
	public String updateGroupStatus(String groupId, String status) {
		BaseResult baseResult = new BaseResult();
		if(StringUtils.isEmpty(groupId)||StringUtils.isEmpty(status)){
			baseResult.setErrorCode(1);
			baseResult.setErrorMessage("输入参数不合法!");
		}else{
			Connection con = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
			con = DbConnectionManager.getConnection();
			con.setAutoCommit(false);
			String sql = "update sky_Group set status=? where id=?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, status);
			pstmt.setString(2, groupId);
			pstmt.executeUpdate();
			
			Map<String,String> m = new HashMap<String, String>();
			m.put("event_name", "GroupSatatusChange");
			m.put("status", status);
			m.put("groupid", groupId);
			JiveGlobals.setProperty(FIRE_PROPERTY_LISTENER_TMP,new Gson().toJson(m));
			con.commit();
			} catch (Exception e) {
				try {
					con.rollback();
				} catch (SQLException e1) {
				}
				baseResult.setErrorCode(1);
				baseResult.setErrorMessage(e.getMessage());
			} finally {
				DbConnectionManager.closeConnection(rs, pstmt, con);
			}
		}
		
		return new Gson().toJson(baseResult);
	}
	
	@Override
	public String updateGroupSetting(String propertyName, String value) {
		BaseResult baseResult = new BaseResult();
		try {
			JiveGlobals.setProperty(propertyName,value);
		} catch (Exception e) {
			baseResult.setErrorCode(1);
			baseResult.setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
		return new Gson().toJson(baseResult);
	}
	
	@Override
	public String getGroupSetting(String propertyName) {
		BaseResult baseResult = new BaseResult();
		try {
			String value = JiveGlobals.getProperty(propertyName,"true");
			baseResult.setObject(value);
		} catch (Exception e) {
			baseResult.setErrorCode(1);
			baseResult.setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
		return new Gson().toJson(baseResult);
	}

	/**
	 * 发送服务器消息
	 */
	@Override
	public String sendServerMessage(String domain, String toId, String content) {
		BaseResult baseResult = new BaseResult();

//		XMPPServer
//				.getInstance()
//				.getSessionManager()
//				.sendServerMessage(new JID(toId + "@" + domain), "服务器提示",
//						content);
		return new Gson().toJson(baseResult);
	}

	/**
	 * 发送一条消息
	 */
	@Override
	public String sendNoticeMessage(String domain, String fromId, String toId,
			String content) {
		BaseResult baseResult = new BaseResult();
//		XMPPServer server = XMPPServer.getInstance();
//		MessageRouter messageRouter = server.getMessageRouter();
//		Message message = new Message();
//		message.setFrom(new JID(fromId + "@" + domain));
//		message.setTo(new JID(toId + "@" + domain));
//		message.setBody(content);
//		message.setType(Message.Type.headline);
//		messageRouter.route(message);
		return new Gson().toJson(baseResult);
	}

	private List<Map<String,Object>> Rs2Map(ResultSet rs) throws SQLException{
		List<Map<String,Object>> rsList = new ArrayList<Map<String,Object>>();
		ResultSetMetaData metaData = rs.getMetaData();
		int columCnt = metaData.getColumnCount();
		while(rs.next()){
			Map<String,Object> map = new HashMap<String, Object>();
			for(int i=1;i<=columCnt;i++){
				map.put(metaData.getColumnName(i), rs.getObject(i));
			}
			rsList.add(map);
		}
		return rsList;
	}

}