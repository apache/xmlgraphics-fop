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

import org.apache.fop.pdf.PDFObject;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.StructureType;

class TableStructElem extends PDFStructElem {

    private static final long serialVersionUID = -3550873504343680465L;
    private PDFStructElem tableFooter;

    public TableStructElem(PDFObject parent, StructureType structureType) {
        super(parent, structureType);
    }

    void addTableFooter(PDFStructElem footer) {
        assert tableFooter == null;
        tableFooter = footer;
    }

    @Override
    protected boolean attachKids() {
        if (tableFooter != null) {
            addKid(tableFooter);
        }
        return super.attachKids();
    }

}
