package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.util.DataItemProcessDelegate;
import com.skyseas.openfireplugins.group.util.DataListPacket;
import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.QueryHandler;
import com.skyseas.openfireplugins.group.iq.ServiceIQHandler;
import com.skyseas.openfireplugins.group.iq.group.GroupSummaryProcessDelegate;
import org.xmpp.forms.DataForm;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 用户圈子列表处理程序。
 * Created by zhangzhi on 2014/9/15.
 */
@QueryHandler(namespace = IQHandler.USER_NAMESPACE, node = "groups")
public class GroupsHandler extends ServiceIQHandler {

    @Override
    public void process(IQ packet) {
        assert packet != null;

        String userName = packet.getFrom().getNode();
        List<GroupInfo> groups = getJoinedGroups(userName);

        if (groups != null) {
            IQ reply = createResultIQ(packet, groups);
            routePacket(reply);
        } else {
            replyError(packet, PacketError.Condition.internal_server_error);
        }
    }

    private List<GroupInfo> getJoinedGroups(String userName) {
        List<GroupInfo> groups = groupManager.getMemberJoinedGroups(userName);
        if (groups != null) {
            return groups;
        } else {
            return Collections.emptyList();
        }
    }

    private IQ createResultIQ(IQ packet, List<GroupInfo> groups) {
        DataListPacket<GroupInfo> dataListPacket = new DataListPacket<GroupInfo>(
                IQHandler.USER_NAMESPACE,
                groups,
                new JoinedGroupsProcessDelegate());

        packet = IQ.createResultIQ(packet);
        packet.setChildElement(dataListPacket.getElement().addAttribute("node", "groups"));
        return packet;
    }

    private final class JoinedGroupsProcessDelegate implements DataItemProcessDelegate<GroupInfo> {
        private final HashMap<String, Object> dataMap;

        public JoinedGroupsProcessDelegate() {
            this.dataMap = new HashMap<String, Object>(5);
        }

        @Override
        public Object getPrimaryProperty(GroupInfo dataItem) {
            return dataItem.getId();
        }

        @Override
        public void beforeProcess(DataForm form) {
            form.addReportedField("jid", null, null);
        }

        @Override
        public void process(DataForm form, GroupInfo dataItem) {
            dataMap.put("jid", dataItem.getJID(groupService.getServiceDomain()));
            form.addItemFields(dataMap);
        }
    }

}
