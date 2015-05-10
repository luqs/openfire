package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupEventDispatcher;
import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.GroupQueryObject;
import com.skyseas.openfireplugins.group.util.Paging;
import junit.framework.TestCase;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.xmpp.packet.JID;

import java.util.ArrayList;
import java.util.List;

public class GroupManagerImplTest extends TestCase {
    GroupManagerImpl groupManager;
    GroupInfo groupInfo;
    @Mocked GroupImpl group;
    @Mocked GroupFacade facade;

    @Override
    public void setUp() {
        groupManager = new GroupManagerImpl(facade);
        groupInfo = new GroupInfo();
        groupInfo.setId(100);
        groupInfo.setOwner("zz");

        new NonStrictExpectations() {
            {
                group.getId();
                result = String.valueOf(groupInfo.getId());
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
                facade.search(queryObject, offset, limit);
                result = groups;
                times = 1;
            }
        };

        // Act
        Object result = groupManager.search(queryObject, offset, limit);

        // Assert
        assertEquals(groups, result);
    }

    public void testCreate() throws Exception {
        // Arrange
        new NonStrictExpectations() {
            {
                facade.create(groupInfo);
                result = group;
                times = 1;

                GroupEventDispatcher.fireGroupCreated(group);
                times = 1;
            }
        };

        // Act
        Group retGroup = groupManager.create(groupInfo);

        // Assert
        assertEquals(group, retGroup);
        assertEquals(retGroup, groupManager.getActivatedGroup(retGroup.getId()));
    }

    public void testGetMemberGroups() throws Exception {
        // Arrange
        final String username = "zz";
        final List<GroupInfo> groups = new ArrayList<GroupInfo>();
        new NonStrictExpectations() {
            {
                facade.getMemberJoinedGroups(username);
                result = groups;
                times = 1;
            }
        };

        // Act
        Object result = groupManager.getMemberJoinedGroups(username);

        // Assert
        assertEquals(groups, result);
    }

    public void testRemoveGroup() throws Exception {

        // Arrange
        final JID owner = new JID("owner@skysea.com");
        final String reason = "再见了";
        new NonStrictExpectations() {
            {
                facade.load(String.valueOf(groupInfo.getId()));
                result = group;
                times = 1;
            }
        };

        new NonStrictExpectations() {
            {
                group.destroy(owner, reason);
                result = true;
                times = 1;
            }
        };

        // Act
        Group result1 = groupManager.getGroup(group.getId());
        boolean result2 = groupManager.remove(result1, owner, reason);
        Group result3 = groupManager.getActivatedGroup(group.getId());

        // Assert
        assertEquals(group, result1);
        assertTrue(result2);
        assertNull(result3);
    }

    public void testRemoveGroup_When_Group_Is_Not_Active() {
        assertNull(groupManager.getActivatedGroup(group.getId()));
        assertFalse(groupManager.remove(group, new JID("owner@skysea.com"), "test"));
    }

    public void testGetGroup() throws Exception {
        // Arrange
        new NonStrictExpectations() {
            {
                facade.load(String.valueOf(groupInfo.getId()));
                result = group;
                times = 1;
            }
        };

        // Act
        Object result1 = groupManager.getGroup(group.getId());
        Object result2 = groupManager.getGroup(group.getId());
        Object result3 = groupManager.getActivatedGroup(group.getId());

        // Assert
        assertEquals(group, result1);
        assertEquals(group, result2);
        assertEquals(group, result3);
    }

    @Mocked GroupImpl group1;
    @Mocked GroupImpl group2;
    public void testClean() throws Exception {
        // Arrange
        new NonStrictExpectations(){
            {
                facade.load(String.valueOf(groupInfo.getId()));
                result = group;

                facade.load("g1");
                result = group1;

                facade.load("g2");
                result = group2;

                group.isIdleState();
                result = false;

                group1.isIdleState();
                result = true;

                group2.isIdleState();
                result = true;
            }
        };

        assertEquals(group, groupManager.getGroup(String.valueOf(groupInfo.getId())));
        assertEquals(group1,groupManager.getGroup("g1"));
        assertEquals(group2,groupManager.getGroup("g2"));
        assertNotNull(groupManager.getActivatedGroup(String.valueOf(groupInfo.getId())));
        assertNotNull(groupManager.getActivatedGroup("g1"));
        assertNotNull(groupManager.getActivatedGroup("g2"));

        // Act
        groupManager.clean();

        // Assert
        assertNotNull(groupManager.getActivatedGroup(String.valueOf(groupInfo.getId())));
        assertNull(groupManager.getActivatedGroup("g1"));
        assertNull(groupManager.getActivatedGroup("g2"));
    }


}