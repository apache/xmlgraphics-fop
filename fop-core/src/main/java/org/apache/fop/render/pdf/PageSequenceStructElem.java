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

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFObject;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StructureType;

public class PageSequenceStructElem extends PDFStructElem {

    private List<PDFStructElem> regionBefores = new ArrayList<PDFStructElem>();

    private List<PDFStructElem> regionAfters = new ArrayList<PDFStructElem>();

    private List<PDFStructElem> regionStarts = new ArrayList<PDFStructElem>();

    private List<PDFStructElem> regionEnds = new ArrayList<PDFStructElem>();

    private List<PDFStructElem> footnoteSeparator = new ArrayList<PDFStructElem>();

    PageSequenceStructElem(PDFObject parent, StructureType structureType) {
        super(parent, structureType);
    }

    void addContent(String flowName, PDFStructElem content) {
        if (flowName.equals("xsl-region-before")) {
            regionBefores.add(content);
        } else if (flowName.equals("xsl-region-after")) {
            regionAfters.add(content);
        } else if (flowName.equals("xsl-region-start")) {
            regionStarts.add(content);
        } else if (flowName.equals("xsl-region-end")) {
            regionEnds.add(content);
        } else if (flowName.equals("xsl-footnote-separator")) {
            footnoteSeparator.add(content);
        } else {
            addKid(content);
        }
    }

    @Override
    protected boolean attachKids() {
        assert !kids.isEmpty();
        PDFArray k = new PDFArray();
        addRegions(k, regionBefores);
        addRegions(k, regionStarts);
        addRegions(k, kids);
        addRegions(k, regionEnds);
        addRegions(k, footnoteSeparator);
        addRegions(k, regionAfters);
        put("K", k);
        return true;
    }

    private void addRegions(PDFArray k, List<? extends PDFObject> regions) {
        if (!regions.isEmpty()) {
            for (PDFObject kid : regions) {
                k.add(kid);
            }
        }
    }

}
