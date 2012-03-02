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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

//Java
import java.io.IOException;

/**
 * Class to handle bullet list style.
 */
public class RtfListStyleBullet extends RtfListStyle {

    /**
     * Gets called before a RtfListItem has to be written.
     * @param item RtfListItem whose prefix has to be written
     * {@inheritDoc}
     * @throws IOException Thrown when an IO-problem occurs
     */
    public void writeListPrefix(RtfListItem item) throws IOException {
        // bulleted list
        item.writeControlWord("pnlvlblt");
        item.writeControlWord("ilvl0");
        item.writeOneAttribute(RtfListTable.LIST_NUMBER, new Integer(item.getNumber()));
        item.writeOneAttribute("pnindent",
                item.getParentList().attrib.getValue(RtfListTable.LIST_INDENT));
        item.writeControlWord("pnf1");
        item.writeGroupMark(true);
        item.writeControlWord("pndec");
        item.writeOneAttribute(RtfListTable.LIST_FONT_TYPE, "2");
        item.writeControlWord("pntxtb");
        item.writeControlWord("'b7");
        item.writeGroupMark(false);
    }

    /**
     * Gets called before a paragraph, which is contained by a RtfListItem has to be written.
     * @param element RtfElement in whose context is to be written
     * {@inheritDoc}
     * @throws IOException Thrown when an IO-problem occurs
     */
    public void writeParagraphPrefix(RtfElement element) throws IOException {
        element.writeGroupMark(true);
        element.writeControlWord("pntext");
        element.writeGroupMark(false);
    }

    /**
     * Gets called when the list table has to be written.
     *
     * @param element RtfElement in whose context is to be written
     * {@inheritDoc}
     * @throws IOException Thrown when an IO-problem occurs
     */
    public void writeLevelGroup(RtfElement element) throws IOException {
        element.attrib.set(RtfListTable.LIST_NUMBER_TYPE, 23);
        element.writeGroupMark(true);
        element.writeOneAttributeNS(RtfListTable.LIST_TEXT_FORM, "\\'01\\'b7");
        element.writeGroupMark(false);

        element.writeGroupMark(true);
        element.writeOneAttributeNS(RtfListTable.LIST_NUM_POSITION, null);
        element.writeGroupMark(false);

        element.attrib.set(RtfListTable.LIST_FONT_TYPE, 2);
    }

}
