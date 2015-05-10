package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.Group;
import com.skyseas.openfireplugins.group.GroupInfo;
import com.skyseas.openfireplugins.group.iq.GroupIQHandler;
import com.skyseas.openfireplugins.group.iq.IQHandler;
import com.skyseas.openfireplugins.group.iq.QueryHandler;
import org.xmpp.forms.DataForm;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 圈子信息查询处理程序。
 * Created by apple on 14-9-14.
 */
@QueryHandler(namespace = IQHandler.GROUP_NAMESPACE, node = "info")
public class InfoQueryHandler extends GroupIQHandler {
    @Override
    public void process(IQ packet, Group group) {
        assert packet != null;
        assert group != null;

        IQ reply = createReply(packet, group.getJid(), group.getGroupInfo());
        routePacket(reply);
    }

    private IQ createReply(IQ packet, JID jid, GroupInfo groupInfo) {
        packet = IQ.createResultIQ(packet);
        DataForm form = new DataForm(DataForm.Type.result);
        form.addField("jid", null, null).addValue(jid.toString());
        form.addField("owner", null, null).addValue(groupInfo.getOwner());
        form.addField("name", null, null).addValue(groupInfo.getName());
        form.addField("logo", null, null).addValue(groupInfo.getLogo());
        form.addField("memberCount", null, null).addValue(groupInfo.getNumberOfMembers());
        form.addField("subject", null, null).addValue(groupInfo.getSubject());
        form.addField("description", null, null).addValue(groupInfo.getDescription());
        form.addField("openness", null, null).addValue(groupInfo.getOpennessType());
        form.addField("category", null, null).addValue(String.valueOf(groupInfo.getCategory()));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("utc"));
        form.addField("createTime", null, null).addValue(format.format(groupInfo.getCreateTime()));

        packet.setChildElement(QUERY_ELEMENT_NAME, IQHandler.GROUP_NAMESPACE)
                .addAttribute("node", "info")
                .add(form.getElement());

        return packet;
    }
}
