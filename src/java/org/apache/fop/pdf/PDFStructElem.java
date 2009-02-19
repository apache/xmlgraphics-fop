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
    private String source = "";
    private boolean level1 = false;

    /**
     * Create the /StructTreeRoot dictionary
     * @param fo passed in fo object
     * @param parent Parent of this PDFStructElem
     */
    public PDFStructElem(String fo, PDFObject parent) {
        super();
        if (parent instanceof PDFStructElem) {
            parentObject = (PDFStructElem) parent;
        }
        put("Type", new PDFName("StructElem"));
        source = fo;
        //TODO Move this into the render/pdf package. The PDF library shall not contain FO knowledge
        if ("block".equals(fo)) {
            put("S", new PDFName("P"));
        } else if ("inline".equals(fo) || "wrapper".equals(fo) || "character".equals(fo)) {
            put("S", new PDFName("Span"));
        } else if ("table-cell".equals(fo)) {
            PDFStructElem grandParent = (PDFStructElem)
                ((PDFStructElem)parent).getParentStructElem();
            String s = grandParent.getSource();
            if ("table-header".equals(s)) {
                put("S", new PDFName("TH"));
            } else {
                put("S", new PDFName("TD"));
            }
        } else if ("table-row".equals(fo)) {
            put("S", new PDFName("TR"));
        } else if ("root".equals(fo)) {
            put("S", new PDFName("Document"));
        } else if ("page-sequence".equals(fo)) {
            put("S", new PDFName("Part"));
        } else if ("flow".equals(fo) || "static-content".equals(fo)) {
            put("S", new PDFName("Sect"));
        }   else if ("page-number".equals(fo) || "page-number-citation".equals(fo)
                || "page-number-citation-last".equals(fo)) {
            put("S", new PDFName("Quote"));
        } else if ("external-graphic".equals(fo) || "instream-foreign-object".equals(fo)) {
            put("S", new PDFName("Figure"));
        } else if ("table".equals(fo)) {
            put("S", new PDFName("Table"));
        } else if ("table-body".equals(fo)) {
            put("S", new PDFName("TBody"));
        } else if ("table-header".equals(fo)) {
            put("S", new PDFName("THead"));
        } else if ("table-footer".equals(fo)) {
            put("S", new PDFName("TFoot"));
        }  else if ("list-block".equals(fo)) {
            put("S", new PDFName("L"));
        } else if ("list-item".equals(fo)) {
            put("S", new PDFName("LI"));
        } else if ("list-item-label".equals(fo)) {
            put("S", new PDFName("Lbl"));
        } else if ("list-item-body".equals(fo)) {
            put("S", new PDFName("LBody"));
        } else if ("block-container".equals(fo)) {
            put("S", new PDFName("Div"));
        } else if ("basic-link".equals(fo)) {
            put("S", new PDFName("Link"));
        } else if ("footnote".equals(fo)) {
            put("S", new PDFName("Note"));
        } else if ("footnote-body".equals(fo)) {
            put("S", new PDFName("Sect"));
        } else if ("marker".equals(fo)) {
            put("S", new PDFName("Private"));
        }  else {
            log.error("Accessibility: PDFStructElem constructor is missing: " + fo);
        }
        setParent(parent);
        if (!"external-graphic".equals(fo) && !"instream-foreign-object".equals(fo)) {
            put("K", new PDFArray());
        }
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
     * Get the source of this StructElem
     * @return the source
     */
    public String getSource() {
        return source;
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
        getKids().add(kid);
    }

    /**
     * Add a kid, but only if it does not already exist
     * @param kid to be added
     * @return true if kid did not already exist
     */
    public boolean addUniqueKid(PDFObject kid) {
        PDFArray mArray = getKids();
        if (!mArray.contains(kid)) {
            getKids().add(kid);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add kid referenced through mcid integer
     * used fo:external-graphic
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

}
