/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.render.mif;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * The is the basis for MIF document elements.
 * This enables the creation of the element and to write it
 * to an output stream including sub-elements or a single value.
 */
public class MIFElement {
    protected String name;
    protected String valueStr = null;
    protected List valueElements = null;

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
        if (valueElements == null) {
            valueElements = new java.util.ArrayList();
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
        if (finished) {
            return true;
        }
        if (valueElements == null && valueStr == null) {
            return false;
        }

        String indentStr = "";
        for (int c = 0; c < indent; c++) {
            indentStr += " ";
        }
        if (!started) {
            os.write((indentStr + "<" + name).getBytes());
            if (valueElements != null) {
                os.write(("\n").getBytes());
            }
            started = true;
        }
        if (valueElements != null) {
            boolean done = true;
            for (Iterator iter = valueElements.iterator(); iter.hasNext();) {
                MIFElement el = (MIFElement)iter.next();
                boolean d = el.output(os, indent + 1);
                if (d) {
                    iter.remove();
                } else {
                    done = false;
                    break;
                }
            }
            if (!finish || !done) {
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
        if (deep && valueElements != null) {
            for (Iterator iter = valueElements.iterator(); iter.hasNext();) {
                MIFElement el = (MIFElement)iter.next();
                el.finish(deep);
            }
        }
    }
}

