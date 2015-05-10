package com.skyseas.openfireplugins.group.spi;

import com.skyseas.openfireplugins.group.ChatUser;
import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupEventListener;
import com.skyseas.openfireplugins.group.MessageFactory;
import org.jivesoftware.util.TaskEngine;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * 圈子事件广播监听器。
 * Created by zhangzhi on 2014/9/29.
 */
public final class GroupEventBroadcastListener implements GroupEventListener {
    public final static GroupEventBroadcastListener INSTANCE = new GroupEventBroadcastListener();

    private GroupEventBroadcastListener() {
    }

    @Override
    public void userJoined(final Group group, final ChatUser user) {
        TaskEngine.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                Message msg = MessageFactory.newInstanceForMemberJoin(user.getUserName(), user.getNickname());
                group.broadcast(msg);
            }
        });

    }

    @Override
    public void userExited(final Group group, final ChatUser user, final String reason) {
        TaskEngine.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                Message msg = MessageFactory.newInstanceForMemberExit(user.getUserName(), user.getNickname(), reason);
                group.broadcast(msg);

                /* 退出者也可以收到消息 */
                group.send(user.getJid(), msg);
            }
        });
    }

    @Override
    public void userKicked(final Group group, final ChatUser user, final JID from, final String reason) {
        TaskEngine.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                  /* 广播到圈子成员时，忽略reason，只有发给被踢出者时才包含reason */
                Message msg = MessageFactory.newInstanceForMemberKick(user.getUserName(), user.getNickname(), from, null);
                group.broadcast(msg);

                msg = MessageFactory.newInstanceForMemberKick(user.getUserName(), user.getNickname(), from, reason);
                group.send(((ChatUserImpl) user).getJid(), msg);

            }
        });
    }

    @Override
    public void userNicknameChanged(final Group group, final ChatUser user, final String oldNickname) {
        TaskEngine.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                Message msg = MessageFactory.newInstanceForMemberUpdateProfile(
                        user.getUserName(), oldNickname, user.getNickname());
                group.broadcast(msg);
            }
        });

    }

    @Override
    public void groupDestroyed(final Group group, final JID from, final String reason) {
        TaskEngine.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                Message msg = MessageFactory.newInstanceForGroupDestroy(from, reason);
                group.broadcast(msg);
            }
        });

    }

    @Override
    public void groupCreated(Group group) {

    }


    @Override
    public void groupInfoChanged(final Group group, final JID from) {
        TaskEngine.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                Message msg = MessageFactory.newInstanceForGroupInfoChange(from);
                group.broadcast(msg);
            }
        });

    }
}
