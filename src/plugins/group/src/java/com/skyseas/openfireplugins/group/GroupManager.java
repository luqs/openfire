package com.skyseas.openfireplugins.group;

import com.skyseas.openfireplugins.group.util.Paging;
import org.xmpp.packet.JID;

import java.util.List;

/**
 * Created by apple on 14-9-14.
 */
public interface GroupManager {

    /**
     * 搜索圈子信息列表。
     * @return
     */
    Paging<GroupInfo> search(GroupQueryObject query, int offset, int limit);

    /**
     * 创建圈子。
     * @param groupInfo
     * @return 圈子标示符。
     */
    Group create(GroupInfo groupInfo);

    /**
     * 获得圈子Id。
     * @param id
     * @return
     */
    Group getGroup(String id);

    /**
     * 删除圈子。
     * @param group
     * @param operator
     * @param reason
     * @return
     */
    boolean remove(Group group, JID operator, String reason);

    /**
     * 获得成员加入过的圈子列表。
     * @param userName
     * @return
     */
    List<GroupInfo> getMemberJoinedGroups(String userName);
}
