package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupEventDispatcher;
import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.ServiceIQHandler;
import com.skyseas.openfireplugins.group.iq.XHandler;
import org.dom4j.Element;
import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import java.util.Date;

/**
 * 圈子创建处理程序。
 * Created by apple on 14-9-14.
 */
@XHandler(namespace = IQHandler.GROUP_NAMESPACE, elementName = "x")
public class CreateHandler extends ServiceIQHandler {

    @Override
    public void process(IQ packet) {
        assert packet != null;

        GroupInfo info = getGroupInfo(packet);
        Group group = groupManager.create(info);

        if(group != null) {
            routePacket(createResultIQ(packet, group.getJid()));
        }else {
            replyError(packet, PacketError.Condition.internal_server_error);
        }
    }

    private GroupInfo getGroupInfo(IQ packet) {
        GroupInfo groupInfo = GroupInfoPacket.getGroupInfo(packet);
        if(groupInfo.getOpennessType() == null) {
            groupInfo.setOpennessType(GroupInfo.OpennessType.PUBLIC);
        }
        groupInfo.setOwner(packet.getFrom().getNode());
        groupInfo.setCreateTime(new Date());
        groupInfo.setStatus("0");
        return groupInfo;
    }

    private IQ createResultIQ(IQ packet, JID jid) {
        packet = IQ.createResultIQ(packet);
        Element element = packet.setChildElement("x", IQHandler.GROUP_NAMESPACE);

        /* 创建数据表单返回新创建的圈子jid */
        DataForm form = new DataForm(DataForm.Type.result);
        FormField jidField = form.addField();
        jidField.setVariable("jid");
        jidField.addValue(jid);
        element.add(form.getElement());

        return packet;
    }
}
