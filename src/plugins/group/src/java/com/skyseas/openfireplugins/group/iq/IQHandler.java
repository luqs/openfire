package com.skyseas.openfireplugins.group.iq;

import com.skyseas.openfireplugins.group.GroupService;
import org.xmpp.packet.IQ;

/**
 * Created by apple on 14-9-14.
 */
public interface IQHandler {
    public final static String USER_NAMESPACE = "http://skysea.com/protocol/group#user";
    public final static String OWNER_NAMESPACE = "http://skysea.com/protocol/group#owner";
    public final static String MEMBER_NAMESPACE = "http://skysea.com/protocol/group#member";
    public final static String GROUP_NAMESPACE = "http://skysea.com/protocol/group";
    public final static String QUERY_ELEMENT_NAME = "query";

    void initialize(GroupService groupService);
    void process(IQContext packet);
}
