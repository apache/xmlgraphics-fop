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

package org.apache.fop.render.rtf;

import org.apache.fop.render.rtf.rtflib.exceptions.RtfException;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfContainer;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.render.rtf.rtflib.tools.BuilderContext;

/**
 * This class creates objects which are missing from the XSL:FO but are required
 * by the RTF format.
 */
public class RTFPlaceHolderHelper {
    /** The context object for building the RTF */
    private BuilderContext builderContext;

    /**
     * Creates a new instance for the RTF place holder which attempts to resolve
     * mismatches in structure between XSL:FO and RTF.
     * @param builderContext The builder context
     */
    public RTFPlaceHolderHelper(BuilderContext builderContext) {
        this.builderContext = builderContext;
    }

    /**
     * A method to create an object which is missing and required from the
     * RTF structure.
     * @param containerClass The class which is missing
     * @throws Exception
     */
    public void createRTFPlaceholder(Class containerClass) throws RtfException {
        if (containerClass == RtfTableRow.class) {
            createRtfTableRow();
        }
    }

    private void createRtfTableRow() throws RtfException {
        try {
            RtfContainer element = builderContext.getContainer(RtfTable.class, true, null);
            if (element != null && element instanceof RtfTable) {
                RtfTable table = (RtfTable)element;
                RtfAttributes attribs = new RtfAttributes();
                RtfTableRow newRow = table.newTableRow(attribs);
                builderContext.pushContainer(newRow);
                builderContext.getTableContext().selectFirstColumn();
            }
        } catch (Exception ex) {
            throw new RtfException(ex.getMessage());
        }
    }
}
