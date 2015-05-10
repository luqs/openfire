package com.skyseas.openfireplugins.group;

import org.xmpp.packet.JID;

import java.util.Collection;
import java.util.List;

/**
 * Created by apple on 14-9-13.
 */
public interface ChatUserManager {

    /**
     * 用户删除类型。
     */
    public enum RemoveType {

        /**
         * 用户是自行退出。
         */
        EXIT,

        /**
         * 用户是被踢出的。
         */
        KICK
    }

    /**
     * 获得用户列表。
     * @return
     */
    Collection<? extends ChatUser> getUsers();

    /**
     * 获得用户数量。
     * @return
     */
    int getNumberOfUsers();

    /**
     * 获得特定用户。
     * @param userName
     * @return
     */
    ChatUser getUser(String userName);

    /**
     * 返回当前用户是否是多用户聊天房间用户。
     * @param userName
     * @return
     */
    boolean hasUser(String userName);

    /**
     * 添加用户。
     * @param userName
     * @param nickname
     */
    ChatUser addUser(String userName, String nickname) throws FullMemberException;

    /**
     * 批量添加用户。
     * @param memberInfoList
     * @return
     * @throws FullMemberException
     */
    List<ChatUser> addUsers(List<GroupMemberInfo> memberInfoList) throws FullMemberException;

    /**
     * 删除用户。
     * @param userName
     * @return
     */
    ChatUser removeUser(RemoveType type, String userName, JID from, String reason);

    /**
     * 修改用户昵称。
     * @param userName
     * @param nickname
     */
    void changeNickname(String userName, String nickname);
}
