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
package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.Area;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.IDReferences;

// Java
import java.util.HashSet;

/**
 * base class for representation of formatting objects and their processing
 */
public abstract class FObj extends FONode {

    public abstract static class Maker {
        public abstract FObj make(FObj parent, PropertyList propertyList,
                                  String systemId, int line, int column)
            throws FOPException;
    }

    // protected PropertyList properties;
    public PropertyList properties;
    protected PropertyManager propMgr;
    protected String systemId;
    protected int line;
    protected int column;

    // markers
    private HashSet markerClassNames;

    protected FObj(FObj parent, PropertyList propertyList,
                   String systemId, int line, int column) {
        super(parent);
        this.properties = propertyList;    // TO BE REMOVED!!!
        propertyList.setFObj(this);
        this.propMgr = makePropertyManager(propertyList);
        this.systemId = systemId;
        this.line = line;
        this.column = column;
        setWritingMode();
    }

    public String getSystemId() {
        return systemId;
    }
  
    public int getLine() {
        return line;
    }
  
    public int getColumn() {
        return column;
    }
  
    protected PropertyManager makePropertyManager(PropertyList propertyList) {
        return new PropertyManager(propertyList);
    }

    /**
     * adds characters (does nothing here)
     * @param data text
     * @param start start position
     * @param length length of the text
     */
    protected void addCharacters(char data[], int start, int length) {
        // ignore
    }

    /**
     * generates the area or areas for this formatting object
     * and adds these to the area. This method should always be
     * overridden by all sub classes
     *
     * @param area
     */
    public int layout(Area area) throws FOPException {
        // should always be overridden
        return Status.OK;
    }

    /**
     * returns the name of the formatting object
     * @return the name of this formatting objects
     */
    public abstract String getName();

    /**
     *
     */
//      protected void start() {
//          // do nothing by default
//      }

    /**
     *
     */
    protected void end() {
        // do nothing by default
    }

    /**
     * lets outside sources access the property list
     * first used by PageNumberCitation to find the "id" property
     * @param name - the name of the desired property to obtain
     * @return the property
     */
    public Property getProperty(String name) {
        return (properties.get(name));
    }


    /**
     * Return the "content width" of the areas generated by this FO.
     * This is used by percent-based properties to get the dimension of
     * the containing block.
     * If an FO has a property with a percentage value, that value
     * is usually calculated on the basis of the corresponding dimension
     * of the area which contains areas generated by the FO.
     * NOTE: subclasses of FObj should implement this to return a reasonable
     * value!
     */
    public int getContentWidth() {
        return 0;
    }

    /**
     * removes property id
     * @param idReferences the id to remove
     */
    public void removeID(IDReferences idReferences) {
        if (((FObj)this).properties.get("id") == null
                || ((FObj)this).properties.get("id").getString() == null)
            return;
        idReferences.removeID(((FObj)this).properties.get("id").getString());
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            FONode child = (FONode)children.get(i);
            if ((child instanceof FObj)) {
                ((FObj)child).removeID(idReferences);
            }
        }
    }

    public boolean generatesReferenceAreas() {
        return false;
    }

    /**
     * Set writing mode for this FO.
     * Find nearest ancestor, including self, which generates
     * reference areas and use the value of its writing-mode property.
     * If no such ancestor is found, use the value on the root FO.
     */
    protected void setWritingMode() {
        FObj p;
        FObj parent;
        for (p = this;
                !p.generatesReferenceAreas() && (parent = p.getParent()) != null;
                p = parent);
        this.properties.setWritingMode(p.getProperty("writing-mode").getEnum());
    }


     public void addMarker(String markerClassName) throws FOPException {
//         String mcname = marker.getMarkerClassName();
         if (children != null) {
             for (int i = 0; i < children.size(); i++) {
                 FONode child = (FONode)children.get(i);
                 if (!child.mayPrecedeMarker()) {
                   throw new FOPException("A fo:marker must be an initial child of '"
                                          + getName()+"'", systemId, line, column);
                 }
             }
         }
         if (markerClassNames==null) {
             markerClassNames = new HashSet();
             markerClassNames.add(markerClassName);
         } else if (!markerClassNames.contains(markerClassName) ) {
             markerClassNames.add(markerClassName);
         } else {
             throw new FOPException("marker-class-name '"
                                    + markerClassName
                                    + "' already exists for this parent",
                                    systemId, line, column);
         }
     }

//     public boolean hasMarkers() {
//         return markers!=null;
//     }

//     public ArrayList getMarkers() {
//         if (markers==null) {
//             log.debug("GetMarkers failed (no markers). Should not happen.");
//             return null;
//         } else {
//             return new ArrayList(markers.values());
//         }
//     }
}

