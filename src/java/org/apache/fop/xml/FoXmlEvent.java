/*
 * $Id$
 * 
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.xml;

import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.pool.Poolable;

/**
 * This is a data class to encapsulate the data of an individual XML
 * parse event in an XML-FO file. The current version, while defining
 * accessor methods, leaves the component data of the event as protected.
 * The <tt>XMLSerialHandler</tt> methods set the values directly.
 */

public class FoXmlEvent extends XmlEvent {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** The FO type, as defined in FObjectNames, of fo: XML events. */
    private int foType = FObjectNames.NO_FO;

    /**
     * @param namespaces the <code>Namespaces</code> object
     * @param sequence the sequence number of the event within its
     * namespace
     * @param uriIndex the namesopace index
     */
    public FoXmlEvent (Namespaces namespaces, int sequence, int uriIndex) {
        super(namespaces, sequence, uriIndex);
    }

    /**
     * The fully defined constructor takes values for each of the data
     * elements.
     */
    public FoXmlEvent(Namespaces namespaces, int sequence,
                    int type, String chars, int uriIndex,
                    String localName, String qName,
                    AttributesImpl attributes, 
                    int foType)
    {
        super(namespaces, sequence, type, chars, uriIndex, localName,
                qName, attributes);
        this.foType = foType;
    }

    /**
     * The cloning constructor takes a reference to an existing
     * <tt>FoXmlEvent</tt> object.
     * @param ev the event to clone
     * @param sequence number of the clone
     */
    public FoXmlEvent(FoXmlEvent ev, int sequence) {
        super(ev, sequence);
        foType = ev.foType;
    }

    public FoXmlEvent(Namespaces namespaces, int sequence,
            int uriIndex, int type, String chars) {
        super(namespaces, sequence, uriIndex, type, chars);
    }

    public FoXmlEvent(Namespaces namespaces, int sequence,
            int type, int uriIndex, AttributesImpl attributes, int foType) {
        super(namespaces, sequence, uriIndex);
        this.type = type;
        this.uriIndex = uriIndex;
        this.attributes = attributes;
        this.foType = foType;
    }

    /**
     * Clear the fields of this event.  Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is cleared.
     * @return the cleared <tt>XmlEvent</tt> event.
     */
    public Poolable clear() {
        foType = FObjectNames.NO_FO;
        return super.clear();
    }

    /**
     * Clear the fields of this event.  Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is cleared.
     * @return the cleared <tt>XmlEvent</tt> event.
     */
    public FoXmlEvent clearFo() {
        return (FoXmlEvent)clear();
    }

    /**
     * Copy the fields of the argument event to this event.
     * Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is copied.
     * The <i>namespaces</i> field is not cleared.
     * @param ev the <tt>XmlEvent</tt> to copy.
     * @return the copied <tt>XmlEvent</tt> event.
     */
    public XmlEvent copyEvent(FoXmlEvent ev) {
        foType = ev.foType;
        return super.copyEvent(ev);
    }

    /**
     * Copy the fields of the argument event to this event.
     * Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is copied.
     * The <i>namespaces</i> field is not cleared.
     * @return the copied <tt>XmlEvent</tt> event.
     */
    public FoXmlEvent copyFoEvent(FoXmlEvent ev) {
        return (FoXmlEvent)copyEvent(ev);
    }

    /**
     * Get the FO type of this <i>FoXmlEvent</i>
     * @returns the FO type
     */
    public int getFoType() { return foType; }

    /**
     * Set the FO type of this <i>FoXmlEvent</i>
     *      * @param foType the FO type
     */
    public void setFoType(int foType) { this.foType = foType; }
    
    public String toString() {
        String tstr;
        try {
            tstr = "FO type: " + FObjectNames.getFOName(foType) + "\n";
        } catch (FOPException e) {
            throw new RuntimeException(e.getMessage());
        }
        tstr = tstr + super.toString();
        return tstr;
    }

}
