/*
 * $Id$
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

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.rtf.rtflib.rtfdoc;

import java.util.HashMap;
import java.util.Iterator;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;


/**  Attributes for RtfText
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfAttributes
implements java.lang.Cloneable {
    private HashMap m_values = new HashMap();

    /** set attributes from a other attributes object.
     *  @return this object, for chaining calls
     */
    public RtfAttributes set (RtfAttributes attrs) {
        if (attrs != null) {
            Iterator it = attrs.nameIterator ();
            while (it.hasNext ()) {
                String name = (String) it.next ();
                if (attrs.getValue(name) instanceof Integer) {
                    Integer value = (Integer)attrs.getValue (name);
                    if (value == null) {
                        set (name);
                    }  else {
                        set (name, value.intValue ());
                    }
                } else if (attrs.getValue(name) instanceof String) {
                    String value = (String)attrs.getValue (name);
                    if (value == null) {
                        set (name);
                    } else {
                        set (name, value);
                    }
                } else {
                        set (name);
                }


            }
            // indicate the XSL attributes used to build the Rtf attributes
            setXslAttributes(attrs.getXslAttributes());
        }
        return this;
    }

    /** set an attribute that has no value.
     *  @return this object, for chaining calls
     */
    public RtfAttributes set(String name) {
        m_values.put(name, null);
        return this;
    }

    /** unset an attribute that has no value
     *  @return this object, for chaining calls
     */
    public RtfAttributes unset(String name) {
        m_values.remove(name);
        return this;
    }

    /** debugging log */
    public String toString() {
        return m_values.toString() + "(" + super.toString() + ")";
    }

    /** implement cloning */
    public Object clone() {
        final RtfAttributes result = new RtfAttributes();
        result.m_values = (HashMap)m_values.clone();

        // Added by Normand Masse
        // indicate the XSL attributes used to build the Rtf attributes
        if (m_xsl_attributes != null) {
            result.m_xsl_attributes = new org.xml.sax.helpers.AttributesImpl(m_xsl_attributes);
        }

        return result;
    }

    /** set an attribute that has an integer value
     *  @return this object, for chaining calls
     */
    public RtfAttributes set(String name, int value) {
        m_values.put(name, new Integer(value));
        return this;
    }

    public RtfAttributes set(String name, String type) {
        m_values.put(name, type);
        return this;
    }

    /** get the value of an attribute, null if not found */
    public Object getValue(String name) {
        return m_values.get(name);
    }

    /** true if given attribute is set */
    public boolean isSet(String name) {
        return m_values.containsKey(name);
    }

    /** return an Iterator on all names that are set */
    public Iterator nameIterator() {
        return m_values.keySet().iterator();
    }

    private Attributes m_xsl_attributes = null;

    // Added by Normand Masse
    // Used for attribute inheritance
    public Attributes getXslAttributes() {
        return m_xsl_attributes;
    }

    // Added by Normand Masse
    // Used for attribute inheritance
    public void setXslAttributes(Attributes pAttribs) {
        if (pAttribs == null) {
            return;
        }
        // copy/replace the xsl attributes into the current attributes
        if (m_xsl_attributes != null) {
            for (int i = 0; i < pAttribs.getLength(); i++) {
                String wKey = pAttribs.getQName(i);
                int wPos = m_xsl_attributes.getIndex(wKey);
                if (wPos == -1) {
                    ((AttributesImpl)m_xsl_attributes).addAttribute(pAttribs.getURI(i),
                            pAttribs.getLocalName(i), pAttribs.getQName(i),
                            pAttribs.getType(i), pAttribs.getValue(i));
                } else {
                    ((AttributesImpl)m_xsl_attributes).setAttribute(wPos, pAttribs.getURI(i),
                            pAttribs.getLocalName(i), pAttribs.getQName(i),
                            pAttribs.getType(i), pAttribs.getValue(i));
                }
            }
        } else {
            m_xsl_attributes = new org.xml.sax.helpers.AttributesImpl(pAttribs);
        }
    }
}
