package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.*;
import junit.framework.TestCase;
import mockit.Delegate;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.xmpp.packet.JID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatUserManagerImplTest extends TestCase {
    @Mocked
    GroupMemberPersistenceManager persistenceManager;
    @Mocked
    Group group;

    @Mocked
    NumberOfUsersListener numberOfUsersListener;

    private int groupId = 100;
    private GroupMemberInfo memberInfo;
    private ChatUserManagerImpl manager;
    private String xmppDomain = "skysea.com";


    @Override
    protected void setUp() throws Exception {
        memberInfo = new GroupMemberInfo();
        memberInfo.setUserName("user");
        memberInfo.setNickName("碧眼狐狸");

        final List<GroupMemberInfo> members = new ArrayList<GroupMemberInfo>();
        members.add(memberInfo);
        new NonStrictExpectations() {
            {
                persistenceManager.getGroupMembers(groupId, null);
                result = members;
                times = 1;

                group.getId();
                result = String.valueOf(groupId);
            }
        };

        manager = new ChatUserManagerImpl(group, xmppDomain, numberOfUsersListener, persistenceManager);
    }

    public void testInitialize() throws Exception {
        assertUserInManager(memberInfo.getUserName(), memberInfo.getNickName());
        assertEquals(1, manager.getUsers().size());
    }

    public void testAddUser() throws Exception {
        // Arrange
        final String userName = "user2";
        final String nickname = "大海故乡";
        new NonStrictExpectations(GroupEventDispatcher.class) {
            {
                persistenceManager.addMembers(groupId, with(new Delegate<List<GroupMemberInfo>>() {
                    public void validate(List<GroupMemberInfo> members) {
                        assertEquals(1, members.size());
                        assertEquals(userName, members.get(0).getUserName());
                        assertEquals(nickname, members.get(0).getNickName());
                    }
                }));
                result = true;
                times = 1;

                GroupEventDispatcher.fireUserJoined(group, with(new Delegate<ChatUser>() {
                    public void validate(ChatUser user) {
                        assertEquals(userName, user.getUserName());
                        assertEquals(nickname, user.getNickname());
                    }
                }));
                times = 1;

                numberOfUsersListener.numberOfUsersChanged(2);
                times = 1;
            }
        };

        // Act
        final ChatUser user = manager.addUser(userName, nickname);

        // Assert
        assertEquals(user.getUserName(), userName);
        assertEquals(user.getNickname(), nickname);
        assertEquals(user, manager.getUser(userName));
        assertUserInManager(userName, nickname);
        assertEquals(2, manager.getUsers().size());
    }

    public void testAddUsers() throws Exception {
        // Arrange
        final GroupMemberInfo memberInfo1 = new GroupMemberInfo("user1", "nick1");
        final GroupMemberInfo memberInfo2 = new GroupMemberInfo("user2", "nick2");
        List<GroupMemberInfo> memberInfos = Arrays.asList(memberInfo1, memberInfo2);
        new NonStrictExpectations(GroupEventDispatcher.class) {
            {
                persistenceManager.addMembers(groupId, with(new Delegate<List<GroupMemberInfo>>() {
                    public void validate(List<GroupMemberInfo> members) {
                        assertTrue(members.contains(memberInfo1));
                        assertTrue(members.contains(memberInfo2));
                    }
                }));
                result = true;
                times = 1;

                GroupEventDispatcher.fireUserJoined(group, (ChatUser)any);
                times = 2;
            }
        };

        // Act
        assertEquals(2, manager.addUsers(memberInfos).size());

        // Assert
        assertNotNull(manager.getUser(memberInfo1.getUserName()));
        assertNotNull(manager.getUser(memberInfo2.getUserName()));
    }

    public void testRemoveUser() throws Exception {
        // Arrange
        final ChatUser user = manager.getUser(memberInfo.getUserName());
        final String reason = "对不起，再见。";
        new NonStrictExpectations(GroupEventDispatcher.class) {
            {
                persistenceManager.removeMember(groupId, memberInfo.getUserName());
                result = true;
                times = 1;

                GroupEventDispatcher.fireUserExited(group, user, reason);
                times = 1;

                numberOfUsersListener.numberOfUsersChanged(0);
                times = 1;
            }
        };

        // Act
        ChatUser result = manager.removeUser(ChatUserManager.RemoveType.EXIT, memberInfo.getUserName(), null, reason);

        // Assert
        assertEquals(user, result);
        assertEquals(0, manager.getUsers().size());
        assertFalse(manager.hasUser(memberInfo.getUserName()));
        assertNull(manager.getUser(memberInfo.getUserName()));
    }


    public void testKickUser() throws Exception {
        // Arrange
        final ChatUser user = manager.getUser(memberInfo.getUserName());
        final String reason = "你太吵了，请离开吧！";
        final JID ownerJid = new JID("owner@skysea.com");
        new NonStrictExpectations(GroupEventDispatcher.class) {
            {
                persistenceManager.removeMember(groupId, memberInfo.getUserName());
                result = true;
                times = 1;

                GroupEventDispatcher.fireUserKicked(group, user, ownerJid, reason);
                times = 1;

                numberOfUsersListener.numberOfUsersChanged(0);
                times = 1;
            }
        };

        // Act
        ChatUser result = manager.removeUser(ChatUserManager.RemoveType.KICK, memberInfo.getUserName(), ownerJid, reason);

        // Assert
        assertEquals(user, result);
        assertEquals(0, manager.getUsers().size());
        assertFalse(manager.hasUser(memberInfo.getUserName()));
        assertNull(manager.getUser(memberInfo.getUserName()));
    }

    public void testChangeNickname() throws Exception {
        // Arrange
        final String newNickname = "大刀关胜";
        final ChatUser user = manager.getUser(memberInfo.getUserName());
        new NonStrictExpectations(GroupEventDispatcher.class) {
            {
                persistenceManager.changeGroupProfile(groupId, memberInfo.getUserName(), newNickname);
                result = true;
                times = 1;

                GroupEventDispatcher.fireUserNicknameChanged(group, user, memberInfo.getNickName());
                times = 1;
            }
        };

        // Act
        manager.changeNickname(memberInfo.getUserName(), newNickname);

        // Assert
        assertEquals(newNickname, user.getNickname());
    }

    private void assertUserInManager(String userName, String nickName) {
        assertTrue(manager.hasUser(userName));
        ChatUser user = manager.getUser(userName);
        assertEquals(userName, user.getUserName());
        assertEquals(nickName, user.getNickname());
        assertEquals(new JID(userName + "@" + xmppDomain), ((ChatUserImpl) user).getJid());

        boolean found = false;
        for (ChatUser u : manager.getUsers()) {
            if (u.getUserName().equals(userName)) {
                assertEquals(nickName, u.getNickname());
                found = true;
            }
        }
        assertTrue(found);
    }
}