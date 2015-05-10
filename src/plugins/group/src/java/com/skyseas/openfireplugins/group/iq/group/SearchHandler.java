package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.*;
import com.skyseas.openfireplugins.group.iq.*;
import com.skyseas.openfireplugins.group.util.*;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import java.util.List;

/**
 * 圈子搜索处理程序。
 * Created by apple on 14-9-14.
 */
@QueryHandler(namespace = SearchHandler.SEARCH_NAMESPACE)
public class SearchHandler extends ServiceIQHandler {
    public final static String SEARCH_NAMESPACE = "jabber:iq:search";

    @Override
    public void process(IQ packet) {
        Paging<GroupInfo> paging;
        try {
            /* 分页查询圈子数据 */
            RSMPacketExtension rsm = QueryInfo.getRSM(packet);
            paging = groupManager.search(
                    QueryInfo.getQuery(packet),
                    rsm.getIndex(0),
                    rsm.getMax(10));
        } catch (Exception e) {
            handleException(e, "搜索圈子失败");
            replyError(packet, PacketError.Condition.internal_server_error);
            return;
        }

        /* 构建分页数据包 */
        PagingPacket pagingPacket = new PagingPacket(
                SEARCH_NAMESPACE, paging,
                new GroupSummaryProcessDelegate(groupService.getServiceDomain()));

        IQ reply = IQ.createResultIQ(packet);
        reply.setChildElement(pagingPacket.getElement());
        routePacket(reply);
    }


    /**
     * 分页的扩展数据包。
     */
    static class PagingPacket<T> extends DataListPacket<T> {
        private final Paging<T> pagingData;

        public PagingPacket(String namespace,
                            Paging<T> pagingData,
                            DataItemProcessDelegate<T> dataItemProcessDelegate) {

            super(namespace, pagingData.getItems(), dataItemProcessDelegate);
            this.pagingData = pagingData;
            this.setRSM();
        }

        private void setRSM() {
            RSMPacketExtension pagingInfo = new RSMPacketExtension();
            pagingInfo.setCount(pagingData.getCount());

            if (pagingData.getCount() > 0) {
                List<T> dataItems = pagingData.getItems();

                if (dataItems.size() > 0) {
                    pagingInfo.setFirstValue(delegate.getPrimaryProperty(
                            dataItems.get(0)), pagingData.getOffset());

                    pagingInfo.setLastValue(delegate.getPrimaryProperty(
                            dataItems.get(dataItems.size() - 1)));
                }
            }
            this.element.add(pagingInfo.getElement());
        }
    }

    /**
     * 圈子查询信息
     */
    private final static class QueryInfo {
        public static GroupQueryObject getQuery(IQ packet) {
            DataFormExtension form = DataFormExtension.getForm(packet);
            GroupQueryObject query = new GroupQueryObject();
            if(form != null) {
                query.setName(form.getFirstValue("name"));
                query.setGroupId(form.getFirstValueAsInt("id", 0));
                query.setCategory(form.getFirstValueAsInt("category", 0));
            }
            return query;
        }

        public static RSMPacketExtension getRSM(IQ packet) {
           RSMPacketExtension rsm = RSMPacketExtension.getRSM(packet);
           return rsm != null ? rsm : new RSMPacketExtension();
        }
    }

}
