/*
 * $Id: MIFElement.java,v 1.3 2003/03/07 08:09:26 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.mif;

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

