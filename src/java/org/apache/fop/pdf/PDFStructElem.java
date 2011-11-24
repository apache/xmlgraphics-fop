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

package org.apache.fop.pdf;

import java.util.Locale;

import org.apache.fop.util.LanguageTags;

/**
 * Class representing a PDF Structure Element.
 */
public class PDFStructElem extends PDFDictionary {

    private PDFStructElem parentElement;

    /**
     * Creates a new structure element.
     *
     * @param parent parent of this element
     * @param structureType the structure type of this element
     */
    PDFStructElem(PDFObject parent, PDFName structureType) {
        if (parent instanceof PDFStructElem) {
            parentElement = (PDFStructElem) parent;
        }
        put("Type", new PDFName("StructElem"));
        put("S", structureType);
        setParent(parent);
    }

    /**
     * Returns the parent of this structure element.
     *
     * @return the parent, <code>null</code> if the parent is not a structure
     * element (i.e., is the structure tree root)
     */
    public PDFStructElem getParentStructElem() {
        return parentElement;
    }

    /** {@inheritDoc} */
    public void setParent(PDFObject parent) {
        if (parent != null) {
           put("P", new PDFReference(parent));
        }
    }

    /**
     * Returns the kids of this structure element.
     *
     * @return the value of the K entry
     */
    private PDFArray getKids() {
        return (PDFArray) get("K");
    }

    /**
     * Add a kid to this structure element. This element will then add itself to
     * its parent structure element if it has not already, and so will the
     * parent, and so on.
     *
     * @param kid element to be added
     */
    public void addKid(PDFObject kid) {
        PDFArray kids = getKids();
        if (kids == null) {
            kids = new PDFArray();
            put("K", kids);
        }
        kids.add(kid);
        joinHierarchy();
    }

    private boolean containsKid(PDFObject kid) {
        PDFArray kids = getKids();
        return kids != null && kids.contains(kid);
    }

    private void joinHierarchy() {
        if (parentElement != null && !parentElement.containsKid(this)) {
            parentElement.addKid(this);
        }
    }

    /**
     * Sets the given mcid as the kid of this structure element. This element
     * will then add itself to its parent structure element if it has not
     * already, and so will the parent, and so on.
     *
     * @param mcid mcid of the marked-content sequence corresponding to this
     * structure element's kid
     */
    public void setMCIDKid(int mcid) {
        put("K", mcid);
        joinHierarchy();
    }

    /**
     * Sets the page reference of this structure element.
     *
     * @param page value for the Pg entry
     */
    public void setPage(PDFPage page) {
        put("Pg", page);
    }

    /**
     * Returns the structure type of this structure element.
     *
     * @return the value of the S entry
     */
    public PDFName getStructureType() {
        return (PDFName)get("S");
    }

    /**
     * Sets the language of this structure element.
     * @param language the language (as defined in the section about
     *                          "Natural Language Specification")
     */
    private void setLanguage(String language) {
        put("Lang", language);
    }

    /**
     * Sets the language of this structure element.
     *
     * @param language a value for the Lang entry
     */
    public void setLanguage(Locale language) {
        setLanguage(LanguageTags.toLanguageTag(language));
    }

    /**
     * Returns the language of this structure element.
     *
     * @return the value of the Lang entry (<code>null</code> if no language was specified)
     */
    public String getLanguage() {
        return (String)get("Lang");
    }
}
