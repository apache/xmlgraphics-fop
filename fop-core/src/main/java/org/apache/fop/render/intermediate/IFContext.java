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

package org.apache.fop.render.intermediate;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.Constants;
import org.apache.fop.render.pdf.PDFStructureTreeBuilder;

/**
 * This class provides a context object that is valid for a single processing run to create
 * an output file using the intermediate format. It allows access to the user agent and other
 * context information, such as foreign attributes for certain elements in the intermediate
 * format.
 * <p>
 * Foreign attributes are usually specific to a particular output format implementation. Most
 * implementations will just ignore all foreign attributes for most elements. That's why the
 * main IF interfaces are not burdened with this.
 */
public class IFContext implements PageIndexContext {

    private FOUserAgent userAgent;

    /** foreign attributes: Map<QName, Object> */
    private Map foreignAttributes = Collections.EMPTY_MAP;

    private Locale language;

    private StructureTreeElement structureTreeElement;

    private String id = "";

    private String location;

    private boolean hyphenated;

    private int pageIndex = -1;

    private int pageNumber = -1;

    private RegionType regionType;

    /**
     * Main constructor.
     * @param ua the user agent
     */
    public IFContext(FOUserAgent ua) {
        setUserAgent(ua);
    }

    /**
     * Set the user agent.
     * @param ua the user agent
     */
    public void setUserAgent(FOUserAgent ua) {
        if (this.userAgent != null) {
            throw new IllegalStateException("The user agent was already set");
        }
        this.userAgent = ua;
    }

    /**
     * Returns the associated user agent.
     * @return the user agent
     */
    public FOUserAgent getUserAgent() {
        return this.userAgent;
    }

    /**
     * Returns the currently applicable foreign attributes.
     * @return a Map&lt;QName, Object&gt;
     */
    public Map getForeignAttributes() {
        return this.foreignAttributes;
    }

    /**
     * Returns a foreign attribute.
     * @param qName the qualified name of the foreign attribute
     * @return the value of the foreign attribute or null if the attribute isn't specified
     */
    public Object getForeignAttribute(QName qName) {
        return this.foreignAttributes.get(qName);
    }

    /**
     * Sets the currently applicable foreign attributes.
     * @param foreignAttributes a Map&lt;QName, Object&gt; or null to reset
     */
    public void setForeignAttributes(Map foreignAttributes) {
        if (foreignAttributes != null) {
            this.foreignAttributes = foreignAttributes;
        } else {
            //Make sure there is always at least an empty map so we don't have to check
            //in the implementation code
            this.foreignAttributes = Collections.EMPTY_MAP;
        }
    }

    /**
     * Resets the foreign attributes to "no foreign attributes".
     */
    public void resetForeignAttributes() {
        setForeignAttributes(null);
    }

    /**
     * Sets the currently applicable language.
     * @param lang the language
     */
    public void setLanguage(Locale lang) {
        this.language = lang;
    }

    /**
     * Returns the currently applicable language.
     * @return the language (or null if the language is undefined)
     */
    public Locale getLanguage() {
        return this.language;
    }

    /**
     * Sets the structure tree element to which the subsequently painted marks
     * will correspond. This method is used when accessibility features are
     * enabled.
     *
     * @param structureTreeElement the structure tree element
     */
    public void setStructureTreeElement(StructureTreeElement structureTreeElement) {
        this.structureTreeElement = structureTreeElement;
    }

    /**
     * Resets the current structure tree element.
     * @see #setStructureTreeElement(StructureTreeElement)
     */
    public void resetStructureTreeElement() {
        setStructureTreeElement(null);
    }

    /**
     * Returns the current structure tree element.
     * @return the structure tree element (or null if no element is active)
     * @see #setStructureTreeElement(StructureTreeElement)
     */
    public StructureTreeElement getStructureTreeElement() {
        if (structureTreeElement instanceof PDFStructureTreeBuilder.Factory) {
            return ((PDFStructureTreeBuilder.Factory)structureTreeElement).createStructureElement(pageNumber);
        }
        return this.structureTreeElement;
    }

    /**
     * Sets the ID of the object enclosing the content that will follow.
     *
     * @param id the ID of the nearest ancestor object for which the id property was set
     */
    void setID(String id) {
        assert id != null;
        this.id = id;
    }

    /**
     * Returns the ID of the object enclosing the current content.
     *
     * @return the ID of the nearest ancestor object for which the id property was set
     */
    String getID() {
        return id;
    }

    /**
     * Sets the location of the object enclosing the current content.
     *
     * location the line and column location of the object in the source FO file
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the location of the object enclosing the current content.
     *
     * @return the line and column location of the object in the source FO file,
     * {@code null} if that information is not available
     */
    public String getLocation() {
        return location;
    }

    /**
     * Records that the last text in the currently processed text area is hyphenated.
     */
    public void setHyphenated(boolean hyphenated) {
        this.hyphenated = hyphenated;
    }

    /**
     * Returns {@code true} if the last text in the currently processed text area is hyphenated.
     */
    public boolean isHyphenated() {
        return hyphenated;
    }

    /**
     * Record current page index.
     * @param pageIndex a zero based page index or -1 (no page)
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * Obtain current page index.
     * @return a zero based page index or -1 (no page)
     */
    public int getPageIndex() {
        return this.pageIndex;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    private enum RegionType {
        Footer,
        Header
    }

    public String getRegionType() {
        if (regionType != null) {
            return regionType.name();
        }
        return null;
    }

    public void setRegionType(String type) {
        regionType = null;
        if (type != null) {
            regionType = RegionType.valueOf(type);
        }
    }

    public void setRegionType(int type) {
        regionType = null;
        if (type == Constants.FO_REGION_AFTER) {
            regionType = RegionType.Footer;
        } else if (type == Constants.FO_REGION_BEFORE) {
            regionType = RegionType.Header;
        }
    }
}
