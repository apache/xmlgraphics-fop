/*
 * $Id$
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.xml;

import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;

/**
 * This is a data class to encapsulate the data of an individual XML
 * parse event in an XML-FO file. The current version, while defining
 * accessor methods, leaves the component data of the event as protected.
 * The <tt>XMLSerialHandler</tt> methods set the values directly.
 */

public class FoXMLEvent extends XMLEvent {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** The FO type, as defined in FObjectNames, of fo: XML events. */
    protected int foType = FObjectNames.NO_FO;

    /**
     * The one-argument constructor uses the default initialization values:
     * NOEVENT for the event <i>type</i>, and null references for all others
     * except <i>namespaces</i>.
     */
    public FoXMLEvent (XMLNamespaces namespaces) {
        super(namespaces);
    }

    /**
     * The fully defined constructor takes values for each of the data
     * elements.
     */
    public FoXMLEvent(int type, String chars, int uriIndex,
                    String localName, String qName,
                    AttributesImpl attributes, XMLNamespaces namespaces,
                    int foType)
    {
        super
            (type, chars, uriIndex, localName, qName, attributes, namespaces);
        this.foType = foType;
    }

    /**
     * The cloning constructor takes a reference to an existing
     * <tt>FoXMLEvent</tt> object.
     */
    public FoXMLEvent(FoXMLEvent ev) {
        super(ev);
        foType = ev.foType;
    }

    public FoXMLEvent(int type, String chars, XMLNamespaces namespaces) {
        super(type, chars, namespaces);
    }

    public FoXMLEvent(int type, int uriIndex, AttributesImpl attributes,
                    XMLNamespaces namespaces, int foType) {
        super(namespaces);
        this.type = type;
        this.uriIndex = uriIndex;
        this.attributes = attributes;
        this.foType = foType;
    }

    /**
     * Clear the fields of this event.  Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is cleared.
     * @return the cleared <tt>XMLEvent</tt> event.
     */
    public XMLEvent clear() {
        foType = FObjectNames.NO_FO;
        return super.clear();
    }

    /**
     * Clear the fields of this event.  Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is cleared.
     * @return the cleared <tt>XMLEvent</tt> event.
     */
    public FoXMLEvent clearFo() {
        return (FoXMLEvent)clear();
    }

    /**
     * Copy the fields of the argument event to this event.
     * Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is copied.
     * The <i>namespaces</i> field is not cleared.
     * @param ev the <tt>XMLEvent</tt> to copy.
     * @return the copied <tt>XMLEvent</tt> event.
     */
    public XMLEvent copyEvent(FoXMLEvent ev) {
        foType = ev.foType;
        return super.copyEvent(ev);
    }

    /**
     * Copy the fields of the argument event to this event.
     * Provided for pool operations.
     * Neither the <i>namespaces</i> nor the <i>id</i> field is copied.
     * The <i>namespaces</i> field is not cleared.
     * @return the copied <tt>XMLEvent</tt> event.
     */
    public FoXMLEvent copyFoEvent(FoXMLEvent ev) {
        return (FoXMLEvent)copyEvent(ev);
    }

    /**
     * Get the FO type of this <i>FoXMLEvent</i>.
     * @returns the FO type.
     */
    public int getFoType() { return foType; }

    /**
     * Set the FO type of this <i>FoXMLEvent</i>.
     * @param foType -the FO type.
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
