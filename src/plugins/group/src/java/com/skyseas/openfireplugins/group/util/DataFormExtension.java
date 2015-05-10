package com.skyseas.openfireplugins.group.util;

import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

/**
* Created by zhangzhi on 2014/9/25.
*/
public final class DataFormExtension extends DataForm {
    public static final QName Q_NAME = QName.get(DataForm.ELEMENT_NAME, DataForm.NAMESPACE);

    public DataFormExtension(Element element) {
        super(element);
    }

    public int getFirstValueAsInt(String name, int defValue) {
        String value = getFirstValue(name);
        if(value != null) {
            try{
                return Integer.valueOf(value);
            }catch (NumberFormatException e){;}
        }
        return defValue;
    }

    public String getFirstValue(String name) {
        FormField field = getField(name);
        if(field != null) {
            return field.getFirstValue();
        }
        return null;
    }

    public static DataFormExtension getForm(IQ packet) {
        Element form = packet.getChildElement().element(Q_NAME);
        return form != null ? new DataFormExtension(form) : null;
    }
    /*public static DataFormExtension getForm(Element element) {
        Element formElement = element.element(Q_NAME);
        if(formElement != null) {
            return new DataFormExtension(formElement);
        }
        return null;
    }*/
}
