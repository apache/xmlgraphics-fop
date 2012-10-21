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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.util.LanguageTags;

/**
 * Class representing a PDF Structure Element.
 */
public class PDFStructElem extends PDFDictionary implements StructureTreeElement, CompressedObject {

    private PDFStructElem parentElement;

    /**
     * Elements to be added to the kids array.
     */
    protected List<PDFObject> kids;

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
        if (parent != null && parent.hasObjectNumber()) {
           put("P", new PDFReference(parent));
        }
    }

    /**
     * Adds a kid to this structure element.
     *
     * @param kid element to be added
     */
    public void addKid(PDFObject kid) {
        if (kids == null) {
            kids = new ArrayList<PDFObject>();
        }
        kids.add(kid);
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
        return (PDFName) get("S");
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
        return (String) get("Lang");
    }

    @Override
    protected void writeDictionary(OutputStream out, StringBuilder textBuffer) throws IOException {
        attachKids();
        super.writeDictionary(out, textBuffer);
    }

    /**
     * Attaches all valid kids to the kids array.
     *
     * @return true iff 1+ kids were added to the kids array
     */
    protected boolean attachKids() {
        List<PDFObject> validKids = new ArrayList<PDFObject>();
        if (kids != null) {
            for (PDFObject kid : kids) {
                if (kid instanceof Placeholder)  {
                    if (((Placeholder) kid).attachKids()) {
                        validKids.add(kid);
                    }
                } else {
                    validKids.add(kid);
                }
            }
        }
        boolean kidsAttached = !validKids.isEmpty();
        if (kidsAttached) {
            PDFArray array = new PDFArray();
            for (PDFObject ob : validKids) {
                array.add(ob);
            }
            put("K", array);
        }
        return kidsAttached;
    }

    /**
     * Class representing a placeholder for a PDF Structure Element.
     */
    public static class Placeholder extends PDFStructElem {

        @Override
        public void outputInline(OutputStream out, StringBuilder textBuffer) throws IOException {
            if (kids != null) {
                assert kids.size() > 0;
                for (int i = 0; i < kids.size(); i++) {
                    if (i > 0) {
                        textBuffer.append(' ');
                    }
                    Object obj = kids.get(i);
                    formatObject(obj, out, textBuffer);
                }
            }
        }

        /**
         * Constructor
         * @param parent -
         * @param name -
         */
        public Placeholder(PDFObject parent, String name) {
            super(parent, new PDFName(name));
        }
    }

}
