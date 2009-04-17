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

/**
 * Class representing a PDF Structure Element.
 */
public class PDFStructElem extends PDFDictionary {

    private PDFObject parentObject = null;
    private boolean level1 = false;

    /**
     * Create the /StructTreeRoot dictionary
     * @param parent Parent of this PDFStructElem
     * @param structureType the structure type for the element
     */
    public PDFStructElem(PDFObject parent, PDFName structureType) {
        super();
        if (parent instanceof PDFStructElem) {
            parentObject = (PDFStructElem) parent;
        }
        put("Type", new PDFName("StructElem"));
        setStructureType(structureType);
        setParent(parent);
    }

    /**
     * This method is called for PDFStructElements which are direct children of
     * fo:static-content or fo:flow-section
     */
    public void setLevel1() {
        this.level1 = true;
    }

    /**
     *
     * @return true if the PDFStructElement is a direct child of
     * fo:static-content or fo:flow-section
     */
    public boolean getLevel1() {
        return this.level1;
    }

    /**
     * Get the parent
     * @return PDFStructElem of parent
     */
    public PDFObject getParentStructElem() {
        return (PDFStructElem)this.parentObject;
    }

    /**
     * Set the parent for this StructElem
     * @param parent to be added
     */
    public void setParent(PDFObject parent) {
        if (parent != null) {
           put("P", new PDFReference(parent));
        }
    }

    /**
     * The kids of this StructElem
     * @return the kids
     */
    public PDFArray getKids() {
        return (PDFArray)get("K");
    }

    /**
     * Add a kid to this strucElem
     * @param kid to be added
     */
    public void addKid(PDFObject kid) {
        PDFArray kids = getKids();
        if (kids == null) {
            kids = new PDFArray();
            put("K", kids);
        }
        kids.add(kid);
    }

    /**
     * Add a kid, but only if it does not already exist.
     * @param kid to be added
     * @return true if kid did not already exist
     */
    public boolean addUniqueKid(PDFObject kid) {
        PDFArray mArray = getKids();
        if (mArray == null || !mArray.contains(kid)) {
            addKid(kid);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add kid referenced through mcid integer. Used for images.
     * @param mcid of this kid
     */
    public void addMCIDKid(int mcid) {
        put("K", mcid);
    }

    /**
     * Add a page reference to this structElem
     * @param pageObject to be added
     */
    public void addPage(Object pageObject) {
        put("Pg", (PDFObject) pageObject);
    }

    /**
     * Sets the structure type (the "S" entry).
     * @param type the structure type
     */
    public void setStructureType(PDFName type) {
        put("S", type);
    }

    /**
     * Returns the structure type of this structure element.
     * @return the structure type
     */
    public PDFName getStructureType() {
        return (PDFName)get("S");
    }
}
