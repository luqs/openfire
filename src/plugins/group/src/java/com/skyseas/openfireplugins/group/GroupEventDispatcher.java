package com.skyseas.openfireplugins.group;

import org.xmpp.packet.JID;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 圈子事件分派器。
 * Created by zhangzhi on 2014/9/15.
 */
public final class GroupEventDispatcher {
    private final static ConcurrentLinkedQueue<GroupEventListener> listeners
            = new ConcurrentLinkedQueue<GroupEventListener>();
    private GroupEventDispatcher(){}

    public static void fireUserJoined(Group group, ChatUser user) {
        for (GroupEventListener listener : listeners) {
            listener.userJoined(group, user);
        }
    }

    public static void fireUserExited(Group group, ChatUser user, String reason) {
        for (GroupEventListener listener : listeners) {
            listener.userExited(group, user, reason);
        }
    }

    public static void fireUserKicked(Group group, ChatUser user, JID from, String reason) {
        for (GroupEventListener listener : listeners) {
            listener.userKicked(group, user, from, reason);
        }
    }

    public static void fireUserNicknameChanged(Group group, ChatUser user, String oldNickname) {
        for (GroupEventListener listener : listeners) {
            listener.userNicknameChanged(group, user, oldNickname);
        }
    }

    public static void fireGroupDestroyed(Group group, JID from, String reason) {
        for (GroupEventListener listener : listeners) {
            listener.groupDestroyed(group, from, reason);
        }
    }

    public static void fireGroupCreated(Group group) {
        for (GroupEventListener listener : listeners) {
            listener.groupCreated(group);
        }
    }

    public static void fireGroupInfoChanged(Group group,  JID from) {
        for (GroupEventListener listener : listeners) {
            listener.groupInfoChanged(group, from);
        }
    }

    public static void addEventListener(GroupEventListener listener) {
        if(listener == null){ throw  new NullPointerException("listener"); }
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeEventListener(GroupEventListener listener) {
        if(listener == null){ throw  new NullPointerException("listener"); }
        listeners.remove(listener);
    }

    public static Collection<GroupEventListener> getEventListeners() {
        return Collections.unmodifiableCollection(listeners);
    }
}
