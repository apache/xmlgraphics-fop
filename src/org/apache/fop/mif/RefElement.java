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
 * Reference MIF Element.
 * This element is a lookup reference set that contains
 * a list of resources used in the MIF Document.
 * When a lookup is performed it will either create a new
 * element or return an existing element that is valid.
 * THe key depends on the type of reference, it should be able
 * to uniquely identify the element.
 */
public class RefElement extends MIFElement {

    /**
     */
    public RefElement(String n) {
        super(n);
    }

    public MIFElement lookupElement(Object key) {
        return null;
    }
}

