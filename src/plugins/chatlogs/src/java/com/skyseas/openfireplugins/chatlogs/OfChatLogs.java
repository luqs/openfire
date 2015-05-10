package com.skyseas.openfireplugins.chatlogs;

import org.jivesoftware.util.JiveConstants;

/**
 * function: 聊天记录对象实体
 */
public class OfChatLogs {
	
	private Long messageid;

    private String sessionjid;

    private String sender;

    private String receiver;

    private String createdate;

    private Integer length;

    private String content;

    private String detail;

    private Integer state;

    public Long getMessageid() {
        return messageid;
    }

    public void setMessageid(Long messageid) {
        this.messageid = messageid;
    }

    public String getSessionjid() {
        return sessionjid;
    }

    public void setSessionjid(String sessionjid) {
        this.sessionjid = sessionjid == null ? null : sessionjid.trim();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender == null ? null : sender.trim();
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver == null ? null : receiver.trim();
    }

    public String getCreatedate() {
        return createdate;
    }

    public void setCreatedate(String createdate) {
        this.createdate = createdate == null ? null : createdate.trim();
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
    
	public interface LogState {
		int show = 0;
		int remove = 1;
	}
	/**
	 * function: 自增id序列管理器，类型变量
	 */
	public class ChatLogsConstants extends JiveConstants {
		// 日志表id自增对应类型
		public static final int CHAT_LOGS = 52;
		// 用户在线统计id自增对应类型
		public static final int USER_ONLINE_STATE = 53;
	}
	@Override
	public String toString() {
		return "messageid:"+getMessageid()+"\t"
		+"sessionjid:"+getSessionjid()+"\t"
		+"sender:"+getSender()+"\t"
		+"receiver:"+getReceiver()+"\t"
		+"createdate:"+getCreatedate()+"\t"
		+"content:"+getContent()+"\t"
		+"detail:"+getDetail()+"\t"
		+"length:"+getLength()+"\t"
		+"state:"+getState();
		 
	}

}