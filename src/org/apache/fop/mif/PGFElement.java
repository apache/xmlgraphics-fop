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
 * Font Catalog element.
 * This is the reference lookup element for fonts in
 * the MIF document.
 */
public class PGFElement extends RefElement {

    /**
     */
    public PGFElement() {
        super("PgfCatalog");
    }

    public MIFElement lookupElement(Object key) {
        if(key == null) {
            MIFElement pgf = new MIFElement("Pgf");
            MIFElement prop = new MIFElement("PgfTag");
            prop.setValue("`Body'");
            pgf.addElement(prop);
            addElement(pgf);
            pgf.finish(true);
            return pgf;
        }
        return null;
    }
}

