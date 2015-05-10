package com.skyseas.openfireplugins.group;

import junit.framework.TestCase;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.xmpp.packet.JID;

import java.util.ArrayList;

public class GroupEventDispatcherTest extends TestCase {
    String reason = "the reason";
    JID from = new JID("from@skysea.com");
    @Mocked Group group;
    @Mocked ChatUser user;
    @Mocked GroupEventListener listener1;
    @Mocked GroupEventListener listener2;
    ArrayList<GroupEventListener> listeners;

    @Override
    protected void setUp() throws Exception {

        listeners = new ArrayList<GroupEventListener>();
        listeners.add(listener1);
        listeners.add(listener2);

        GroupEventDispatcher.addEventListener(listener1);
        GroupEventDispatcher.addEventListener(listener2);
    }

    @Override
    protected void tearDown() throws Exception {
        GroupEventDispatcher.removeEventListener(listener1);
        GroupEventDispatcher.removeEventListener(listener2);
    }


    public void testAddEventListener() {
        assertEquals(2, GroupEventDispatcher.getEventListeners().size());
        assertTrue(GroupEventDispatcher.getEventListeners().contains(listener1));
        assertTrue(GroupEventDispatcher.getEventListeners().contains(listener2));
    }

    public void testRemoveEventListener() {
        GroupEventDispatcher.removeEventListener(listener1);
        assertEquals(1, GroupEventDispatcher.getEventListeners().size());
        assertFalse(GroupEventDispatcher.getEventListeners().contains(listener1));


        GroupEventDispatcher.removeEventListener(listener2);
        assertEquals(0, GroupEventDispatcher.getEventListeners().size());
        assertFalse(GroupEventDispatcher.getEventListeners().contains(listener2));
    }

    public void testFireUserJoined() throws Exception {
        // Arrange
        for (final GroupEventListener listener : listeners) {
            new NonStrictExpectations() {
                {
                    listener.userJoined(group, user);
                    times = 1;
                }
            };
        }

        // Act
        GroupEventDispatcher.fireUserJoined(group, user);
    }

    public void testFireUserExited() throws Exception {
        // Arrange
        for (final GroupEventListener listener : listeners) {
            new NonStrictExpectations() {
                {
                    listener.userExited(group, user, reason);
                    times = 1;
                }
            };
        }

        // Act
        GroupEventDispatcher.fireUserExited(group, user, reason);
    }

    public void testFireUserKicked() throws Exception {
        // Arrange
        for (final GroupEventListener listener : listeners) {
            new NonStrictExpectations() {
                {
                    listener.userKicked(group, user, from, reason);
                    times = 1;
                }
            };
        }

        // Act
        GroupEventDispatcher.fireUserKicked(group, user, from, reason);
    }

    public void testFireUserNicknameChanged() throws Exception {

        // Arrange
        final String oldNickname = "大花阿虎";
        for (final GroupEventListener listener : listeners) {
            new NonStrictExpectations() {
                {
                    listener.userNicknameChanged(group, user, oldNickname);
                    times = 1;
                }
            };
        }

        // Act
        GroupEventDispatcher.fireUserNicknameChanged(group, user, oldNickname);
    }

    public void testFireGroupDestroyed() throws Exception {
        // Arrange
        for (final GroupEventListener listener : listeners) {
            new NonStrictExpectations() {
                {
                    listener.groupDestroyed(group, from, reason);
                    times = 1;
                }
            };
        }

        // Act
        GroupEventDispatcher.fireGroupDestroyed(group, from, reason);
    }

    public void testFireGroupCreated() throws Exception {
        // Arrange
        for (final GroupEventListener listener : listeners) {
            new NonStrictExpectations() {
                {
                    listener.groupCreated(group);
                    times = 1;
                }
            };
        }

        // Act
        GroupEventDispatcher.fireGroupCreated(group);
    }

    public void testFireGroupInfoChanged() throws Exception {
        // Arrange
        for (final GroupEventListener listener : listeners) {
            new NonStrictExpectations() {
                {
                    listener.groupInfoChanged(group,from );
                    times = 1;
                }
            };
        }

        // Act
        GroupEventDispatcher.fireGroupInfoChanged(group, from);
    }
}