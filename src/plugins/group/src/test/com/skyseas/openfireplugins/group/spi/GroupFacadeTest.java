package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.*;
import com.skyseas.openfireplugins.group.iq.group.MockChatUser;
import com.skyseas.openfireplugins.group.util.Paging;
import junit.framework.TestCase;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.jivesoftware.openfire.PacketRouter;
import org.xmpp.packet.JID;

import java.util.ArrayList;
import java.util.List;

public class GroupFacadeTest extends TestCase {
    @Mocked GroupPersistenceManager persistenceManager;
    @Mocked GroupService groupService;
    @Mocked GroupIQDispatcher dispatcher;
    @Mocked PacketRouter packetRouter;
    @Mocked ChatUserManager chatUserManager;
    @Mocked GroupMemberPersistenceManager groupMemberPersistenceManager;
    private GroupFacade facade;
    private GroupInfo groupInfo;
    private GroupImpl group;

    @Override
    protected void setUp() throws  Exception {
        facade = new GroupFacade();
        facade.setPersistenceFactory(persistenceFactory);

        groupInfo = new GroupInfo();
        groupInfo.setId(100);
        groupInfo.setOwner("zz");

        group = new GroupImpl(
                new JID("100@group.skysea.com"),
                groupInfo,
                "skysea.com",
                dispatcher,
                packetRouter,
                persistenceManager,
                groupMemberPersistenceManager);

        new NonStrictExpectations(group){
            {
                group.getChatUserManager();
                result = chatUserManager;
            }
        };
    }

    public void testSearch() throws Exception {
        // Arrange
        final Paging<GroupInfo> groups = new Paging<GroupInfo>();
        final GroupQueryObject queryObject = new GroupQueryObject();
        final int offset = 1;
        final int limit = 10;

        new NonStrictExpectations() {
            {
                persistenceManager.queryGroups(queryObject, offset, limit);
                result = groups;
                times = 1;
            }
        };

        // Act
        Object result = facade.search(queryObject, offset, limit);

        // Assert
        assertEquals(groups, result);
    }

    public void testGetMemberGroups() throws Exception {
        // Arrange
        final String username = "zz";
        final List<GroupInfo> groups = new ArrayList<GroupInfo>();
        new NonStrictExpectations() {
            {
                persistenceManager.getMemberJoinedGroups(username);
                result = groups;
                times = 1;
            }
        };

        // Act
        Object result = facade.getMemberJoinedGroups(username);

        // Assert
        assertEquals(groups, result);
    }

    public void testCreate() throws Exception {
        // Arrange
        new NonStrictExpectations(facade) {
            {
                facade.newInstance(groupInfo);
                result = group;

                persistenceManager.addGroup(groupInfo);
                times = 1;

                chatUserManager.addUser(groupInfo.getOwner(), groupInfo.getOwner());
                result = new MockChatUser("zz","zz");
                times = 1;
            }
        };

        // Act
        Group retGroup = facade.create(groupInfo);

        // Assert
        assertEquals(group, retGroup);
    }

    public void testLoad() throws Exception {
        // Arrange
        new NonStrictExpectations(facade){
            {
                persistenceManager.getGroup(groupInfo.getId());
                result = groupInfo;
                times = 1;

                facade.newInstance(groupInfo);
                result = group;
                times = 1;
            }
        };

        // Act
        Group retGroup = facade.load(String.valueOf(groupInfo.getId()));

        // Assert
        assertEquals(group, retGroup);
    }

    private PersistenceFactory persistenceFactory = new PersistenceFactory() {
        @Override
        public GroupMemberPersistenceManager getGroupMemberPersistenceManager() {
            return groupMemberPersistenceManager;
        }

        @Override
        public GroupPersistenceManager getGroupPersistenceManager() {
            return persistenceManager;
        }
    };
}