package com.skyseas.openfireplugins.group.iq;

import org.xmpp.packet.IQ;

import java.util.HashMap;

/**
 * Created by zhangzhi on 2014/9/15.
 */
public final class IQContext{
    public static final String ITEM_GROUP = "GROUP";
    private final IQ packet;
    private HashMap<String, Object> _items;

    public IQContext(IQ packet) {
        if(packet == null) { throw new NullPointerException("packet is null."); }
        this.packet = packet;
    }

    public IQ getPacket(){
        return packet;
    }

    public <T> T getItem(String name) {
        if(name == null){ throw new NullPointerException("name is null."); }
        if(_items != null) {
            return (T)_items.get(name);
        }else {
            return null;
        }
    }

    public void setItem(String name, Object value){
        if(name == null) { throw new NullPointerException("name is null."); }
        if(_items == null) {
            _items = new HashMap<String, Object>(2);
        }
        _items.put(name, value);
    }
}
