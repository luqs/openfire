package com.skyseas.openfireplugins.group.util;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

/**
 * Created by zhangzhi on 2014/9/9.
 */
public abstract class ModelPacket {

    protected final Element extensionRoot;
    protected final Element modeElement;

    protected ModelPacket(Element extensionRoot, String modelElementName) {
        assert extensionRoot != null;
        assert modelElementName!= null;

        this.extensionRoot = extensionRoot;
        this.modeElement = extensionRoot.element(modelElementName);
        assert modeElement != null;
    }


    protected ModelPacket(String name, String namespace, String modelElementName) {
        this.extensionRoot = DocumentHelper.createElement(QName.get(name,namespace));
        this.modeElement = extensionRoot.addElement(modelElementName);
    }


    protected String getElementValue(String elementName, String defaultValue) {
        Element ele = this.modeElement.element(elementName);
        if(ele == null) { return  defaultValue; }
        return ele.getStringValue();
    }

    public Element getModeElement() {
        return modeElement;
    }

    public Element getExtensionRoot(){
        return extensionRoot;
    }

    public void appendTo(Element element) {
        element.add(this.extensionRoot);
    }
}
