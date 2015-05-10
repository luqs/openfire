package com.skyseas.openfireplugins.group.iq.group;

import com.skyseas.openfireplugins.group.util.DataItemProcessDelegate;
import com.skyseas.openfireplugins.group.GroupInfo;
import org.xmpp.forms.DataForm;

import java.util.HashMap;

/**
* Created by zhangzhi on 2014/9/15.
*/
public class GroupSummaryProcessDelegate implements DataItemProcessDelegate<GroupInfo> {
    private final String groupServiceName;
    private final HashMap<String, Object> dataMap;

    public GroupSummaryProcessDelegate(String groupServiceName) {
        this.groupServiceName = groupServiceName;
        this.dataMap = new HashMap<String, Object>(5);
    }

    @Override
    public Object getPrimaryProperty(GroupInfo dataItem) {
        return dataItem.getId();
    }

    @Override
    public void beforeProcess(DataForm form) {
        form.addReportedField("jid",            null, null);
        form.addReportedField("openness",       null, null);
        form.addReportedField("owner",          null, null);
        form.addReportedField("name",           null, null);
        form.addReportedField("logo",           null, null);
        form.addReportedField("memberCount",    null, null);
        form.addReportedField("subject",        null, null);
    }

    @Override
    public void process(DataForm form, GroupInfo dataItem) {
        dataMap.put("jid",                      dataItem.getJID(groupServiceName));
        dataMap.put("openness",                 String.valueOf(dataItem.getOpennessType()));
        dataMap.put("owner",                    dataItem.getOwner());
        dataMap.put("name",                     dataItem.getName());
        dataMap.put("logo",                     dataItem.getLogo());
        dataMap.put("memberCount",              dataItem.getNumberOfMembers());
        dataMap.put("subject",                  dataItem.getSubject());
        form.addItemFields(dataMap);
    }
}
