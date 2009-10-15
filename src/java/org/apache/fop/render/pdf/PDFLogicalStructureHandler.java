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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.InternalElementMapping;
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
    private final Map structTreeMap = new HashMap();

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
        final String tag;

        /**
         * The value for the MCID entry of the marked-content sequence's property list.
         */
        final int mcid;

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

    /**
     * Converts the given structure tree into PDF.
     *
     * @param structureTree the structure tree of the current page sequence
     * @param language language set on the page sequence
     */
    void processStructureTree(NodeList structureTree, Locale language) {
        pdfDoc.enforceLanguageOnRoot();
        PDFStructElem structElemPart = pdfDoc.getFactory().makeStructureElement(
                FOToPDFRoleMap.mapFormattingObject("page-sequence", rootStructureElement),
                rootStructureElement);
        rootStructureElement.addKid(structElemPart);
        if (language != null) {
            structElemPart.setLanguage(language);
        }

        for (int i = 0, n = structureTree.getLength(); i < n; i++) {
            Node node = structureTree.item(i);
            assert node.getLocalName().equals("flow")
                    || node.getLocalName().equals("static-content");
            PDFStructElem structElemSect = pdfDoc.getFactory().makeStructureElement(
                    FOToPDFRoleMap.mapFormattingObject(node.getLocalName(), structElemPart),
                    structElemPart);
            structElemPart.addKid(structElemSect);
            NodeList childNodes = node.getChildNodes();
            for (int j = 0, m = childNodes.getLength(); j < m; j++) {
                processNode(childNodes.item(j), structElemSect, true);
            }
        }
    }

    private void processNode(Node node, PDFStructElem parent, boolean addKid) {
        Node attr = node.getAttributes().getNamedItemNS(InternalElementMapping.URI, "ptr");
        assert attr != null;
        String ptr = attr.getNodeValue();
        String nodeName = node.getLocalName();
        PDFStructElem structElem = pdfDoc.getFactory().makeStructureElement(
                FOToPDFRoleMap.mapFormattingObject(nodeName, parent), parent);
        // TODO necessary? If a page-sequence is empty (e.g., contains a single
        // empty fo:block), should the block still be added to the structure
        // tree? This is not being done for descendant empty elements...
        if (addKid) {
            parent.addKid(structElem);
        }
        if (nodeName.equals("external-graphic") || nodeName.equals("instream-foreign-object")) {
            Node altTextNode = node.getAttributes().getNamedItemNS(
                    ExtensionElementMapping.URI, "alt-text");
            if (altTextNode != null) {
                structElem.put("Alt", altTextNode.getNodeValue());
            } else {
                // TODO route that to event notification system
//                log.warn("fo:" + s
//                 + " requires an alternative text attribute fox:alt-text for accessibility");
                structElem.put("Alt", "No alternate text specified");
            }
        }
        structTreeMap.put(ptr, structElem);
        NodeList nodes = node.getChildNodes();
        for (int i = 0, n = nodes.getLength(); i < n; i++) {
            processNode(nodes.item(i), structElem, false);
        }
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

    private MarkedContentInfo addToParentTree(String reference) {
        PDFStructElem parent = (PDFStructElem) structTreeMap.get(reference);
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
     * @param parentReference reference to the parent structure element of the
     * piece of text
     * @return the necessary information for bracketing the content as a
     * marked-content sequence. If there is no element in the structure tree
     * associated to that content, returns an instance whose
     * {@link MarkedContentInfo#tag} value is <code>null</code>. The content
     * must then be treated as an artifact.
     */
    MarkedContentInfo addTextContentItem(String parentReference) {
        MarkedContentInfo mci = addToParentTree(parentReference);
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
     * @param parentReference reference to the parent structure element of the
     * image
     * @return the necessary information for bracketing the content as a
     * marked-content sequence. If there is no element in the structure tree
     * associated to that image, returns an instance whose
     * {@link MarkedContentInfo#tag} value is <code>null</code>. The image
     * must then be treated as an artifact.
     */
    MarkedContentInfo addImageContentItem(String parentReference) {
        MarkedContentInfo mci = addToParentTree(parentReference);
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
     * @param reference reference to the corresponding parent structure element
     */
    void addLinkContentItem(PDFLink link, String reference) {
        int structParent = getNextParentTreeKey();
        link.setStructParent(structParent);
        parentTree.getNums().put(structParent, link);
        PDFDictionary contentItem = new PDFDictionary();
        contentItem.put("Type", OBJR);
        contentItem.put("Pg", this.currentPage);
        contentItem.put("Obj", link);
        PDFStructElem parent = (PDFStructElem) structTreeMap.get(reference);
        parent.addKid(contentItem);
    }

}
