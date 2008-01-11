/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.util.HashMap;
import java.util.Iterator;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;


/**  Attributes for RtfText
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfAttributes
implements java.lang.Cloneable {
    private HashMap values = new HashMap();

    /**
     * Set attributes from another attributes object
     * @param attrs RtfAttributes object whose elements will be copied into this
     *        instance
     * @return this object, for chaining calls
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

    /**
     * set an attribute that has no value.
     * @param name name of attribute to set
     * @return this object, for chaining calls
     */
    public RtfAttributes set(String name) {
        values.put(name, null);
        return this;
    }

    /**
     * unset an attribute that has no value
     * @param name name of attribute to unset
     * @return this object, for chaining calls
     */
    public RtfAttributes unset(String name) {
        values.remove(name);
        return this;
    }

    /**
     * debugging log
     * @return String representation of object
     */
    public String toString() {
        return values.toString() + "(" + super.toString() + ")";
    }

    /**
     * implement cloning
     * @return cloned Object
     */
    public Object clone() {
        final RtfAttributes result = new RtfAttributes();
        result.values = (HashMap)values.clone();

        // Added by Normand Masse
        // indicate the XSL attributes used to build the Rtf attributes
        if (xslAttributes != null) {
            result.xslAttributes = new org.xml.sax.helpers.AttributesImpl(xslAttributes);
        }

        return result;
    }

    /**
     * Set an attribute that has an integer value
     * @param name name of attribute
     * @param value value of attribute
     * @return this (which now contains the new entry), for chaining calls
     */
    public RtfAttributes set(String name, int value) {
        values.put(name, new Integer(value));
        return this;
    }

    /**
     * Set an attribute that has a String value
     * @param name name of attribute
     * @param type value of attribute
     * @return this (which now contains the new entry)
     */
    public RtfAttributes set(String name, String type) {
        values.put(name, type);
        return this;
    }

    /**
     * Set an attribute that has nested attributes as value
     * @param name name of attribute
     * @param value value of the nested attributes
     * @return this (which now contains the new entry)
     */
    public RtfAttributes set(String name, RtfAttributes value) {
        values.put(name, value);
        return this;
    }

    /**
     * @param name String containing attribute name
     * @return the value of an attribute, null if not found
     */
    public Object getValue(String name) {
        return values.get(name);
    }

    /**
     * @param name String containing attribute name
     * @return true if given attribute is set
     */
    public boolean isSet(String name) {
        return values.containsKey(name);
    }

    /** @return an Iterator on all names that are set */
    public Iterator nameIterator() {
        return values.keySet().iterator();
    }

    private Attributes xslAttributes = null;

    /**
     * Added by Normand Masse
     * Used for attribute inheritance
     * @return Attributes
     */
    public Attributes getXslAttributes() {
        return xslAttributes;
    }

    /**
     * Added by Normand Masse
     * Used for attribute inheritance
     * @param pAttribs attributes
     */
    public void setXslAttributes(Attributes pAttribs) {
        if (pAttribs == null) {
            return;
        }
        // copy/replace the xsl attributes into the current attributes
        if (xslAttributes != null) {
            for (int i = 0; i < pAttribs.getLength(); i++) {
                String wKey = pAttribs.getQName(i);
                int wPos = xslAttributes.getIndex(wKey);
                if (wPos == -1) {
                    ((AttributesImpl)xslAttributes).addAttribute(pAttribs.getURI(i),
                            pAttribs.getLocalName(i), pAttribs.getQName(i),
                            pAttribs.getType(i), pAttribs.getValue(i));
                } else {
                    ((AttributesImpl)xslAttributes).setAttribute(wPos, pAttribs.getURI(i),
                            pAttribs.getLocalName(i), pAttribs.getQName(i),
                            pAttribs.getType(i), pAttribs.getValue(i));
                }
            }
        } else {
            xslAttributes = new org.xml.sax.helpers.AttributesImpl(pAttribs);
        }
    }
    
    /**
     * Add integer value <code>addValue</code> to attribute with name <code>name</code>.
     * If there is no such setted attribute, then value of this attribure is equal to 
     * <code>addValue</code>.
     * @param addValue the increment of value
     * @param name the name of attribute
     */
    public void addIntegerValue(int addValue, String name) {
        Integer value = (Integer) getValue(name);
        int v = (value != null) ? value.intValue() : 0; 
        set(name, v + addValue);
    }
}
