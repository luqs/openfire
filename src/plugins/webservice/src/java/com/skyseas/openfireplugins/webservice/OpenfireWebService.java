package com.skyseas.openfireplugins.webservice;


public interface OpenfireWebService {
	public final static String ALLOW_CREATE_GROUP = "group.setting.allow_create_group";
	public final static String FIRE_PROPERTY_LISTENER_TMP = "fire.property.listener.tmp";

	public String getAllGroup();

	public String updateGroupStatus(String groupId, String status);

	public String createGroup(String[] jids, String ownerJid, String name,
			String desc);

	public String sendServerMessage(String domain, String toId, String content);

	public String sendNoticeMessage(String domain, String fromId, String toId,
			String content);

	public String updateGroupSetting(String propertyName, String value);

	public String getGroupSetting(String propertyName);
}
