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
 * The is the basis for MIF document elements.
 * This enables the creation of the element and to write it
 * to an output stream including sub-elements or a single value.
 */
public class MIFElement {
    protected String name;
    protected String valueStr = null;
    protected ArrayList valueElements = null;

    protected boolean started = false;
    protected boolean finish = false;
    protected boolean finished = false;

    /**
     */
    public MIFElement(String n) {
        name = n;
    }

    public void setValue(String str) {
        valueStr = str;
    }

    public void addElement(MIFElement el) {
        if(valueElements == null) {
            valueElements = new ArrayList();
        }
        valueElements.add(el);
    }

    /**
     * Output this element to an output stream.
     * This will output only so far as the fisrt unfinished child element.
     * This method can be called again to continue from the previous point.
     * An element that contains child elements will only be finished when
     * the finish method is called.
     */
    public boolean output(OutputStream os, int indent) throws IOException {
        if(finished) return true;
        if(valueElements == null && valueStr == null) return false;

        String indentStr = "";
        for(int c = 0; c < indent; c++) indentStr += " ";
        if(!started) {
            os.write((indentStr + "<" + name).getBytes());
            if(valueElements != null)
                os.write(("\n").getBytes());
            started = true;
        }
        if(valueElements != null) {
            boolean done = true;
            for(Iterator iter = valueElements.iterator(); iter.hasNext(); ) {
                MIFElement el = (MIFElement)iter.next();
                boolean d = el.output(os, indent + 1);
                if(d) {
                    iter.remove();
                } else {
                    done = false;
                    break;
                }
            }
            if(!finish || !done) {
                return false;
            }
            os.write((indentStr + "> # end of " + name + "\n").getBytes());
        } else {
            os.write((" " + valueStr + ">\n").getBytes());
        }
        finished = true;
        return true;
    }

    public void finish(boolean deep) {
        finish = true;
        if(deep && valueElements != null) {
            for(Iterator iter = valueElements.iterator(); iter.hasNext(); ) {
                MIFElement el = (MIFElement)iter.next();
                el.finish(deep);
            }
        }
    }
}

