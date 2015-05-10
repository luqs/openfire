package com.skyseas.openfireplugins.group.util;

import org.xmpp.forms.DataForm;

/**
 * Created by apple on 14-9-14.
 */
public interface DataItemProcessDelegate<T> {
    Object getPrimaryProperty(T dataItem);

    void beforeProcess(DataForm form);

    void process(DataForm form, T dataItem);
}
