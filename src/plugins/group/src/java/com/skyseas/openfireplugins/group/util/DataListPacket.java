package com.skyseas.openfireplugins.group.util;

import com.skyseas.openfireplugins.group.iq.IQHandler;
import org.xmpp.forms.DataForm;
import org.xmpp.packet.PacketExtension;

/**
* Created by zhangzhi on 2014/9/3.
*/
public class DataListPacket<T> extends PacketExtension {

    protected final Iterable<T> dataList;
    protected final DataItemProcessDelegate<T> delegate;

    public DataListPacket(String namespace,
                          Iterable<T> dataList,
                          DataItemProcessDelegate<T> processDelegate) {
        super(IQHandler.QUERY_ELEMENT_NAME, namespace);
        this.dataList = dataList;
        this.delegate = processDelegate;
        this.initialize();
    }

    private void initialize() {
        DataForm form = new DataForm(DataForm.Type.result);
        delegate.beforeProcess(form);
        for (T item: dataList){
            delegate.process(form, item);
        }
        this.element.add(form.getElement());
    }
}
