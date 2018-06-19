package com.beerboy.ss.model.properties;

import com.beerboy.ss.model.Xml;

public class FileProperty extends AbstractProperty implements Property {

    public FileProperty() {
        super("file", "file");
    }

    public static boolean isType(String type, String format) {
        if (type != null && "file".equals(type.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

    public FileProperty xml(Xml xml) {
        this.setXml(xml);
        return this;
    }

    public FileProperty readOnly() {
        this.setReadOnly(Boolean.TRUE);
        return this;
    }
}