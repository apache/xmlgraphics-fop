/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.mif;

// Java
import java.io.*;
import java.util.*;

/**
 *
 */
public class RulingElement extends RefElement {

    /**
     */
    public RulingElement() {
        super("RulingCatalog");
    }

    public MIFElement lookupElement(Object key) {
        if(key == null) {
            MIFElement rul = new MIFElement("Ruling");
            MIFElement prop = new MIFElement("RulingTag");
            prop.setValue("`Default'");
            rul.addElement(prop);
            prop = new MIFElement("RulingPenWidth");
            prop.setValue("1");
            rul.addElement(prop);
            prop = new MIFElement("RulingPen");
            prop.setValue("0");
            rul.addElement(prop);
            prop = new MIFElement("RulingLines");
            prop.setValue("1");
            rul.addElement(prop);

            addElement(rul);
            rul.finish(true);
            return rul;
        }
        return null;
    }
}

