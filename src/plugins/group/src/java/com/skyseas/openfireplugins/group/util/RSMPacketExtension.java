package com.skyseas.openfireplugins.group.util;

import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketExtension;

/**
 * Created by zhangzhi on 2014/8/28.
 * Result set management
 */
public class RSMPacketExtension extends PacketExtension {
    public final static QName Q_NAME        = QName.get("set", "http://jabber.org/protocol/rsm");
    private final static QName Q_NAME_MAX   = QName.get("max", Q_NAME.getNamespaceURI());
    private final static QName Q_NAME_INDEX = QName.get("index", Q_NAME.getNamespaceURI());
    private final static QName Q_NAME_COUNT = QName.get("count", Q_NAME.getNamespaceURI());
    private final static QName Q_NAME_FIRST = QName.get("first", Q_NAME.getNamespaceURI());
    private final static QName Q_NAME_LAST  = QName.get("last", Q_NAME.getNamespaceURI());

    public RSMPacketExtension(Element element) {
        super(element);
    }

    public RSMPacketExtension() {
        super(Q_NAME.getName(), Q_NAME.getNamespaceURI());
    }

    public int getMax(int defValue) {
        return elementValueOfInteger(Q_NAME_MAX, defValue);
    }

    public void setMax(int max) {
       changeElement(Q_NAME_MAX, String.valueOf(max));
    }


    public int getIndex(int defValue) {
        return elementValueOfInteger(Q_NAME_INDEX, defValue);
    }

    public void setIndex(int index) {
        changeElement(Q_NAME_INDEX, String.valueOf(index));
    }

    public int getCount() {
        return elementValueOfInteger(Q_NAME_COUNT, 0);
    }

    public void setCount(int count) {
        changeElement(Q_NAME_COUNT, String.valueOf(count));
    }

    public void setFirstValue(Object value){
        changeElement(Q_NAME_FIRST, String.valueOf(value));
    }

    public void setFirstValue(Object value, int index){
        changeElement(Q_NAME_FIRST, String.valueOf(value))
                .addAttribute(Q_NAME_INDEX, String.valueOf(index));
    }

    public void setLastValue(Object value) {
        changeElement(Q_NAME_LAST, String.valueOf(value));
    }

    private static int valueOfInteger(String stringValue, int defValue) {
        try {
            return Integer.valueOf(stringValue);
        } catch (Exception e) {
            ;
        }
        return defValue;
    }

    private int elementValueOfInteger(QName qname, int defValue) {
        Element ele = this.element.element(qname);
        if (ele == null) {
            return defValue;
        }
        return valueOfInteger(ele.getStringValue(), defValue);
    }

    private Element changeElement(QName name, String value) {
        Element ele = this.element.element(name);
        if(ele == null){
            ele = this.element.addElement(name);
        }
        ele.setText(value);
        return ele;
    }

    public static RSMPacketExtension getRSM(IQ packet) {
        Element ele = packet.getChildElement().element(RSMPacketExtension.Q_NAME);
        return ele != null ? new RSMPacketExtension(ele) : null;
    }
}
