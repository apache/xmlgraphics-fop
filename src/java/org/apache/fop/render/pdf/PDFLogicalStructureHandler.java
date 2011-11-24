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

package org.apache.fop.render.pdf;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFLink;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFParentTree;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFStructTreeRoot;


/**
 * Handles the creation of the logical structure in the PDF document.
 */
class PDFLogicalStructureHandler {

    private static final PDFName MCR = new PDFName("MCR");

    private static final PDFName OBJR = new PDFName("OBJR");

    private static final MarkedContentInfo ARTIFACT = new MarkedContentInfo(null, -1, null);

    private final PDFDocument pdfDoc;

    /**
     * Map of references to the corresponding structure elements.
     */
    private final Map<String, PDFStructElem> structTreeMap = new HashMap<String, PDFStructElem>();

    private final PDFParentTree parentTree = new PDFParentTree();

    private int parentTreeKey;

    private PDFPage currentPage;

    /**
     * The array of references, from marked-content sequences in the current
     * page, to their parent structure elements. This will be a value in the
     * structure parent tree, whose corresponding key will be the page's
     * StructParents entry.
     */
    private PDFArray pageParentTreeArray;

    private PDFStructElem rootStructureElement;

    /**
     * Class providing the necessary information for bracketing content
     * associated to a structure element as a marked-content sequence.
     */
    static final class MarkedContentInfo {

        /**
         * A value that can be used for the tag operand of a marked-content
         * operator. This is the structure type of the corresponding structure
         * element.
         */
        final String tag;                                       // CSOK: VisibilityModifier

        /**
         * The value for the MCID entry of the marked-content sequence's property list.
         */
        final int mcid;                                         // CSOK: VisibilityModifier

        private final PDFStructElem parent;

        private MarkedContentInfo(String tag, int mcid, PDFStructElem parent) {
            this.tag = tag;
            this.mcid = mcid;
            this.parent = parent;
        }
    }

    /**
     * Creates a new instance for handling the logical structure of the given document.
     *
     * @param pdfDoc a document
     */
    PDFLogicalStructureHandler(PDFDocument pdfDoc) {
        this.pdfDoc = pdfDoc;
        PDFStructTreeRoot structTreeRoot = pdfDoc.getFactory().makeStructTreeRoot(parentTree);
        rootStructureElement = pdfDoc.getFactory().makeStructureElement(
                FOToPDFRoleMap.mapFormattingObject("root", structTreeRoot), structTreeRoot);
        structTreeRoot.addKid(rootStructureElement);
    }


    PDFStructElem createPageSequence(Locale language) {
        PDFStructElem structElemPart = pdfDoc.getFactory().makeStructureElement(
                FOToPDFRoleMap.mapFormattingObject("page-sequence", rootStructureElement),
                rootStructureElement);
        rootStructureElement.addKid(structElemPart);
        if (language != null) {
            structElemPart.setLanguage(language);
        }
        return structElemPart;
    }

    private int getNextParentTreeKey() {
        return parentTreeKey++;
    }

    /**
     * Receive notification of the beginning of a new page.
     *
     * @param page the page that will be rendered in PDF
     */
    void startPage(PDFPage page) {
        currentPage = page;
        currentPage.setStructParents(getNextParentTreeKey());
        pageParentTreeArray = new PDFArray();
    }

    /**
     * Receive notification of the end of the current page.
     */
    void endPage() {
        // TODO
        // Values in a number tree must be indirect references to the PDF
        // objects associated to the keys. To enforce that the array is
        // registered to the PDF document. Unfortunately that can't be done
        // earlier since a call to PDFContentGenerator.flushPDFDoc can be made
        // before the array is complete, which would result in only part of it
        // being output to the PDF.
        // This should really be handled by PDFNumsArray
        pdfDoc.registerObject(pageParentTreeArray);
        parentTree.getNums().put(currentPage.getStructParents(), pageParentTreeArray);
    }

    private MarkedContentInfo addToParentTree(String structurePointer) {
        PDFStructElem parent = (PDFStructElem) structTreeMap.get(structurePointer);
        if (parent == null) {
            return ARTIFACT;
        } else {
            pageParentTreeArray.add(parent);
            String type = parent.getStructureType().toString();
            int mcid = pageParentTreeArray.length() - 1;
            return new MarkedContentInfo(type, mcid, parent);
        }
    }

    /**
     * Adds a content item corresponding to text into the structure tree, if
     * there is a structure element associated to it.
     *
     * @param structurePointer reference to the parent structure element of the
     * piece of text
     * @return the necessary information for bracketing the content as a
     * marked-content sequence. If there is no element in the structure tree
     * associated to that content, returns an instance whose
     * {@link MarkedContentInfo#tag} value is <code>null</code>. The content
     * must then be treated as an artifact.
     */
    MarkedContentInfo addTextContentItem(String structurePointer) {
        MarkedContentInfo mci = addToParentTree(structurePointer);
        if (mci != ARTIFACT) {
            PDFDictionary contentItem = new PDFDictionary();
            contentItem.put("Type", MCR);
            contentItem.put("Pg", this.currentPage);
            contentItem.put("MCID", mci.mcid);
            mci.parent.addKid(contentItem);
        }
        return mci;
    }

    /**
     * Adds a content item corresponding to an image into the structure tree, if
     * there is a structure element associated to it.
     *
     * @param structurePointer reference to the parent structure element of the
     * image
     * @return the necessary information for bracketing the content as a
     * marked-content sequence. If there is no element in the structure tree
     * associated to that image, returns an instance whose
     * {@link MarkedContentInfo#tag} value is <code>null</code>. The image
     * must then be treated as an artifact.
     */
    MarkedContentInfo addImageContentItem(String structurePointer) {
        MarkedContentInfo mci = addToParentTree(structurePointer);
        if (mci != ARTIFACT) {
            mci.parent.setMCIDKid(mci.mcid);
            mci.parent.setPage(this.currentPage);
        }
        return mci;
    }

    // While the PDF spec allows images to be referred as PDF objects, this
    // makes the Acrobat Pro checker complain that the image is not accessible.
    // Its alt-text is still read aloud though. Using marked-content sequences
    // like for text works.
//    MarkedContentInfo addImageObject(String parentReference) {
//        MarkedContentInfo mci = addToParentTree(parentReference);
//        if (mci != ARTIFACT) {
//            PDFDictionary contentItem = new PDFDictionary();
//            contentItem.put("Type", OBJR);
//            contentItem.put("Pg", this.currentPage);
//            contentItem.put("Obj", null);
//            mci.parent.addKid(contentItem);
//        }
//        return mci;
//    }

    /**
     * Adds a content item corresponding to the given link into the structure
     * tree.
     *
     * @param link a link
     * @param structurePointer reference to the corresponding parent structure element
     */
    void addLinkContentItem(PDFLink link, String structurePointer) {
        int structParent = getNextParentTreeKey();
        link.setStructParent(structParent);
        PDFDictionary contentItem = new PDFDictionary();
        contentItem.put("Type", OBJR);
        contentItem.put("Pg", this.currentPage);
        contentItem.put("Obj", link);
        PDFStructElem parent = (PDFStructElem) structTreeMap.get(structurePointer);
        parentTree.getNums().put(structParent, parent);
        parent.addKid(contentItem);
    }

    void addStructurePointer(String ptr, PDFStructElem structElem) {
        structTreeMap.put(ptr, structElem);
    }

}
