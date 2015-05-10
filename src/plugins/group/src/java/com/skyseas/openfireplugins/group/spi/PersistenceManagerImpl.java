package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.*;
import com.skyseas.openfireplugins.group.util.Paging;
import com.skyseas.openfireplugins.group.util.StringUtils;
import org.jivesoftware.database.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 持久化管理器实现。
 * Created by zhangzhi on 2014/8/27.
 */
public class PersistenceManagerImpl implements GroupPersistenceManager, GroupMemberPersistenceManager, PersistenceFactory {
    public final static PersistenceManagerImpl INSTANCE = new PersistenceManagerImpl();

    private final static Logger LOG = LoggerFactory.getLogger(PersistenceManagerImpl.class);
    private final static String SELECT_GROUP =
            "SELECT `id`, `owner`,`name`,`logo`,`numberOfMembers`,`subject`,`openness` FROM `sky_Group` %1$s ORDER BY `numberOfMembers` DESC LIMIT ?,?;";
    private final static String SELECT_GROUP_COUNT =
            "SELECT COUNT(*) FROM `sky_Group` %1$s;";
    private final static  String INSERT_GROUP =
            "INSERT INTO sky_Group (name,owner,category,logo,description,openness,createTime,subject,numberOfMembers,status) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)";
    private final static String DELETE_GROUP =
            "DELETE FROM `sky_Group` WHERE id = ?";
    private final static String INSERT_GROUP_MEMBER =
            "INSERT INTO `sky_GroupMembers` (groupId, userName, nickName, joinTime)" +
                    "VALUES (?,?,?,?)";
    private final static String DELETE_GROUP_MEMBER =
            "DELETE FROM `sky_GroupMembers` WHERE groupId = ? AND userName = ?";

    private final static String DELETE_GROUP_MEMBERS =
            "DELETE FROM `sky_GroupMembers` WHERE groupId = ?";

    private final static String UPDATE_GROUP_MEMBER =
            "UPDATE `sky_GroupMembers` SET `nickName` = ? WHERE groupId = ? AND userName = ?";

    private final static String SELECT_SINGLE_GROUP =
            "SELECT id, name,owner,category,logo,description,openness,createTime,subject,numberOfMembers,status " +
             "FROM `sky_Group` WHERE id = ?";

    private final static String SELECT_GROUPS_BY_MEMBER =
            "SELECT  `id`,`owner`,`name`,`logo`,`numberOfMembers`,`subject`, `openness` " +
                    "FROM `sky_Group` WHERE ID IN(SELECT `groupid` FROM sky_GroupMembers WHERE `userName` = ?)";

    private final static String SELECT_GROUP_ALL_MEMBERS =
            "SELECT `id`,`groupId`,`userName`,`nickName`,`joinTime` FROM sky_GroupMembers ";

    private final static String UPDATE_GROUP_NUMBER_OF_MEMBERS =
            "UPDATE `sky_Group` SET `numberOfMembers` = `numberOfMembers` + ? WHERE id = ?";

    private final static String SELECT_EXIST_SPECIFIC_MEMBER =
            "SELECT 1 FROM `sky_GroupMembers` WHERE groupId = ? AND userName = ?";

    @Override
    public GroupMemberPersistenceManager getGroupMemberPersistenceManager() {
        return this;
    }

    @Override
    public GroupPersistenceManager getGroupPersistenceManager() {
        return this;
    }

    @Override
    public void addGroup(GroupInfo groupInfo) throws PersistenceException {
        assert groupInfo != null;
        Connection          con = null;
        PreparedStatement   pstmt = null;
        ResultSet           rs = null;

        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(INSERT_GROUP, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(    1, groupInfo.getName());
            pstmt.setString(    2, groupInfo.getOwner());
            pstmt.setInt(       3, groupInfo.getCategory());
            pstmt.setString(    4, groupInfo.getLogo());
            pstmt.setString(    5, groupInfo.getDescription());
            pstmt.setByte(      6, (byte) groupInfo.getOpennessType().ordinal());
            pstmt.setTimestamp( 7, new Timestamp(groupInfo.getCreateTime().getTime()));
            pstmt.setString(    8, groupInfo.getSubject());
            pstmt.setInt(       9, 0);
            pstmt.setString(       10, "0");
            if(pstmt.executeUpdate() > 0) {
                rs = pstmt.getGeneratedKeys();
                rs.next();
                groupInfo.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            LOG.error("创建圈子写入数据库失败", e);
            throw new PersistenceException(e);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    @Override
    public Paging<GroupInfo> queryGroups(GroupQueryObject queryObject, int offset, int limit) throws PersistenceException {
        assert queryObject != null;

        Connection          con             = null;
        PreparedStatement   pstmt           = null;
        ResultSet           rs              = null;
        ArrayList<Object>   parameters      = null;
        String              whereCondition  = null;


        FieldListBuilder fieldListBuilder = new FieldListBuilder("AND")
                .addField(true, "openness", 2, "<") // 2代表PRIVATE的圈子

                .addField(queryObject.getGroupId() > 0,
                        "id", queryObject.getGroupId())

                .addField(queryObject.getCategory() > 0,
                        "category", queryObject.getCategory())

                .addField(!StringUtils.isNullOrEmpty(queryObject.getName()),
                        "name", "%" + queryObject.getName() + "%", "LIKE");

        parameters = fieldListBuilder.getParameters();
        whereCondition = " WHERE " + fieldListBuilder.getSql();
        try {

            int count = 0;
            try {
                con = DbConnectionManager.getConnection();
                pstmt = con.prepareStatement(String.format(SELECT_GROUP_COUNT, whereCondition));
                fillParamemters(pstmt, parameters);

                rs = pstmt.executeQuery();
                rs.next();
                count = rs.getInt(1);
            }finally {
                DbConnectionManager.closeConnection(rs, pstmt, null);
            }

            Paging<GroupInfo> paging = new Paging<GroupInfo>();
            paging.setLimit(limit);
            paging.setOffset(offset);
            paging.setCount(count);

            if(count > 0) {
                try {
                    parameters.add(offset);
                    parameters.add(limit);

                    pstmt = con.prepareStatement(String.format(SELECT_GROUP, whereCondition));
                    fillParamemters(pstmt, parameters);
                    rs = pstmt.executeQuery();
                    paging.setItems(getGroupList(rs));
                }finally {
                    DbConnectionManager.closeConnection(rs, pstmt, null);
                }
            }

            return paging;

        }catch (Exception e) {
            LOG.error("查询圈子失败", e);
            throw new PersistenceException(e);
        }finally {
            DbConnectionManager.closeConnection(con);
        }
    }

    @Override
    public List<GroupInfo> getMemberJoinedGroups(String userName) throws PersistenceException {
        assert userName != null;

        Connection          con             = null;
        PreparedStatement   pstmt           = null;
        ResultSet           rs              = null;

        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_GROUPS_BY_MEMBER);

            pstmt.setString(1, userName);
            rs = pstmt.executeQuery();

            return getGroupList(rs);
        }catch (Exception e) {
            LOG.error("查询用户加入的圈子失败", e);
            throw new PersistenceException(e);
        }finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    @Override
    public boolean changeGroupProfile(int id, String userName, String nickname) throws PersistenceException {
        assert id > 0;
        assert userName != null;
        assert nickname != null;

        Connection          con             = null;
        PreparedStatement   pstmt           = null;

        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(UPDATE_GROUP_MEMBER);
            pstmt.setString(1,  nickname);
            pstmt.setInt(2,     id);
            pstmt.setString(3,  userName);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.error("更新成员个人信息失败", e);
            throw new PersistenceException(e);
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    @Override
    public List<GroupMemberInfo> getGroupMembers(int groupId, String userName) throws PersistenceException {
        assert groupId > 0;

        Connection          con             = null;
        PreparedStatement   pstmt           = null;
        ResultSet           rs              = null;
        ArrayList<Object>   parameters      = null;
        String              condition       = "";

        FieldListBuilder fieldListBuilder = new FieldListBuilder("AND")
                .addField(groupId > 0, "groupId", groupId)
                .addField(!StringUtils.isNullOrEmpty(userName), "userName", userName);

        parameters = fieldListBuilder.getParameters();
        if(parameters.size() > 0) {
            condition = " WHERE " + fieldListBuilder.getSql();
        }

        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_GROUP_ALL_MEMBERS + condition);
            fillParamemters(pstmt, parameters);

            rs = pstmt.executeQuery();
            return getMemberList(rs);
        }catch (Exception e) {
            LOG.error("查询用户加入的圈子失败", e);
            throw new PersistenceException(e);
        }finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    @Override
    public boolean updateGroup(GroupInfo groupInfo) throws PersistenceException {
        assert groupInfo != null;
        Connection          con             = null;
        PreparedStatement   pstmt           = null;
        ArrayList<Object>   parameters      = null;
        final String        PART_UPDATE_SQL = "update sky_Group set";
        StringBuilder       sqlBuilder      = new StringBuilder(PART_UPDATE_SQL);


        FieldListBuilder fieldListBuilder = new FieldListBuilder(",");
        fieldListBuilder.addField(groupInfo.getName() != null,
                "name", groupInfo.getName())

        .addField(groupInfo.getSubject() != null,
                "subject", groupInfo.getSubject())

        .addField(groupInfo.getDescription() != null,
                "description", groupInfo.getDescription())

        .addField(groupInfo.getLogo() != null,
                "logo", groupInfo.getLogo())

        .addField(groupInfo.getCategory() != 0,
                "category", groupInfo.getCategory());

        if(groupInfo.getOpennessType() != null) {
            fieldListBuilder.addField(true,
                    "openness",
                    groupInfo.getOpennessType().ordinal());
        }

        parameters = fieldListBuilder.getParameters();
        sqlBuilder.append(fieldListBuilder.getSql());

        // 没有需要更新的字段
        if(parameters.size() < 1) {
            return false;
        }

        sqlBuilder.append(" where id = ?");
        parameters.add(groupInfo.getId());

        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(sqlBuilder.toString());
            fillParamemters(pstmt, parameters);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.error("更新圈子数据库失败", e);
            throw new PersistenceException(e);
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }

    @Override
    public boolean addMembers(int groupId, List<GroupMemberInfo> members) throws PersistenceException{
        Connection          con     = null;
        PreparedStatement   pstmt   = null;
        boolean success             = false;

        try {
            con = DbConnectionManager.getConnection();
            con.setAutoCommit(false);

            for(GroupMemberInfo member : members) {
                if(!addMember(con, groupId, member)) {
                    return false;
                }
            }
            success = updateNumOfMembers(con, groupId, members.size());
        } catch (SQLException e) {
            LOG.error("添加圈子成员到数据库失败", e);
            throw new PersistenceException(e);
        } finally {
            try {
                if (success) {
                    con.commit();
                } else {
                    con.rollback();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            DbConnectionManager.closeConnection(pstmt, con);
        }

        return success;
    }

    private boolean addMember(Connection con, int groupId, GroupMemberInfo memberInfo) throws SQLException{
        PreparedStatement   pstmt = con.prepareStatement(INSERT_GROUP_MEMBER);
        pstmt.setInt(           1, groupId);
        pstmt.setString(        2, memberInfo.getUserName());
        pstmt.setString(        3, memberInfo.getNickName());
        pstmt.setTimestamp(     4, new Timestamp(new java.util.Date().getTime()));
        return pstmt.executeUpdate() > 0;
    }

    private boolean updateNumOfMembers(Connection con, int groupId, int num) {

        PreparedStatement pstmt = null;
        try{
            pstmt = con.prepareStatement(UPDATE_GROUP_NUMBER_OF_MEMBERS);
            pstmt.setInt(1, num);
            pstmt.setInt(2, groupId);
            return pstmt.executeUpdate() > 0;
        }catch (Exception exp) {
            LOG.error(String.format("更新圈子人数失败:GroupID:%d, Num:%d", groupId, num));
        }finally {
            DbConnectionManager.closeStatement(pstmt);
        }
        return false;
    }

    @Override
    public GroupInfo getGroup(int groupId) throws PersistenceException {
        if(groupId < 1) { return null; }

        Connection          con = null;
        PreparedStatement   pstmt = null;
        ResultSet           rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_SINGLE_GROUP);
            pstmt.setInt(1, groupId);

            rs = pstmt.executeQuery();
            if(rs.next()) {
                return getGroupInfo(rs);
            }
            return null;
        } catch (SQLException e) {
            LOG.error("从数据库获得圈子数据失败", e);
            throw new PersistenceException(e);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }

    }

    @Override
    public boolean isGroupMember(int groupId, String userName) throws PersistenceException {
        Connection          con = null;
        PreparedStatement   pstmt = null;
        ResultSet           rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_EXIST_SPECIFIC_MEMBER);
            pstmt.setInt(1,     groupId);
            pstmt.setString(2,  userName);

            rs = pstmt.executeQuery();
            if(rs.next()) {
                return rs.getInt(1) == 1;
            }

            return false;
        } catch (SQLException e) {
            LOG.error("从数据库查询圈子是否存在某成员失败。", e);
            throw new PersistenceException(e);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
    }

    @Override
    public boolean removeMember(int groupId, String userName) throws PersistenceException {
        Connection          con = null;
        PreparedStatement   pstmt = null;
        boolean success = false;

        try {
            con = DbConnectionManager.getConnection();
            con.setAutoCommit(false);

            pstmt = con.prepareStatement(DELETE_GROUP_MEMBER);
            pstmt.setInt(1, groupId);
            pstmt.setString(2, userName);
            success = pstmt.executeUpdate() > 0 && updateNumOfMembers(con, groupId, -1);

        } catch (SQLException e) {
            LOG.error("添加圈子成员到数据库失败", e);
            throw new PersistenceException(e);
        } finally {
            try {
                if (success) {
                    con.commit();
                } else {
                    con.rollback();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            DbConnectionManager.closeConnection(pstmt, con);
        }

        return success;
    }

    @Override
    public boolean removeGroup(int groupId) throws PersistenceException {
        Connection          con = null;
        PreparedStatement   pstmt = null;
        boolean             success = false;

        try {
            con = DbConnectionManager.getConnection();
            con.setAutoCommit(false);

            pstmt = con.prepareStatement(DELETE_GROUP);
            pstmt.setInt(1, groupId);
            success = pstmt.executeUpdate() > 0 && deleteGroupMembers(con, groupId);

        } catch (SQLException e) {
            LOG.error("添加圈子成员到数据库失败", e);
            throw new PersistenceException(e);
        } finally {
            try {
                if (success) {
                    con.commit();
                } else {
                    con.rollback();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            DbConnectionManager.closeConnection(pstmt, con);
        }

        return success;
    }

    private boolean deleteGroupMembers(Connection con, int groupId) {
        PreparedStatement   pstmt = null;
        try {
            pstmt = con.prepareStatement(DELETE_GROUP_MEMBERS);
            pstmt.setInt(1, groupId);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            LOG.error("删除圈子成员失败", e);
        }finally {
            DbConnectionManager.closeStatement(pstmt);
        }
        return false;
    }

    private GroupInfo getGroupInfo(ResultSet rs) throws SQLException {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(                rs.getInt("id"));
        groupInfo.setName(              rs.getString("name"));
        groupInfo.setOwner(             rs.getString("owner"));
        groupInfo.setCategory(          rs.getInt("category"));
        groupInfo.setLogo(              rs.getString("logo"));
        groupInfo.setDescription(       rs.getString("description"));
        groupInfo.setOpennessType(      GroupInfo.OpennessType.values()[rs.getInt("openness")]);
        groupInfo.setCreateTime(        rs.getDate("createTime"));
        groupInfo.setSubject(           rs.getString("subject"));
        groupInfo.setNumberOfMembers(   rs.getInt("numberOfMembers"));
        groupInfo.setStatus(  			rs.getString("status"));
        return groupInfo;
    }

    private void fillParamemters(PreparedStatement pstmt, ArrayList<Object> parameters) throws SQLException {
        for (int i = 1; i <= parameters.size(); i++) {
            pstmt.setObject(i, parameters.get(i - 1));
        }
    }

    private List<GroupMemberInfo> getMemberList(ResultSet rs) throws SQLException {
        ArrayList<GroupMemberInfo> list = new ArrayList<GroupMemberInfo>(rs.getFetchSize());
        while (rs.next()){
            GroupMemberInfo memberInfo = new GroupMemberInfo();
            memberInfo.setId(               rs.getInt("id"));
            memberInfo.setGroupId(          rs.getInt("groupId"));
            memberInfo.setUserName(         rs.getString("userName"));
            memberInfo.setNickName(         rs.getString("nickName"));
            memberInfo.setJoinTime(         rs.getDate("joinTime"));
            list.add(memberInfo);
        }
        return list;
    }

    private List<GroupInfo> getGroupList(ResultSet rs) throws SQLException {
        ArrayList<GroupInfo> list = new ArrayList<GroupInfo>(rs.getFetchSize());
        while (rs.next()){
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setId(rs.getInt(1));
            groupInfo.setOwner(rs.getString(2));
            groupInfo.setName(rs.getString(3));
            groupInfo.setLogo(rs.getString(4));
            groupInfo.setNumberOfMembers(rs.getInt(5));
            groupInfo.setSubject(rs.getString(6));
            groupInfo.setOpennessType(GroupInfo.OpennessType.values()[rs.getInt(7)]);
            list.add(groupInfo);
        }
        return list;
    }


    private static class FieldListBuilder {
        private final StringBuffer sqlBuilder;
        private final ArrayList<Object> parameters;
        private final String conj;

        public FieldListBuilder(String conj) {
            this.conj = conj;
            this.parameters = new ArrayList<Object>();
            this.sqlBuilder = new StringBuffer();
        }

        public FieldListBuilder addField(boolean ast, String fieldName, Object value) {
            return addField(ast, fieldName, value, "=");
        }

        public FieldListBuilder addField(boolean ast, String fieldName, Object value, String operator){
            if(ast){
                if(sqlBuilder.length() > 0) {
                    sqlBuilder.append(' ');
                    sqlBuilder.append(conj);
                    sqlBuilder.append(' ');
                }
                sqlBuilder.append(" `")
                .append(fieldName)
                .append("` ")
                .append(operator)
                .append(" ?");
                parameters.add(value);
            }

            return this;
        }

        public String getSql() {
            return sqlBuilder.toString();
        }

        public ArrayList<Object> getParameters() {
            return new ArrayList<Object>(this.parameters);
        }
    }

}
