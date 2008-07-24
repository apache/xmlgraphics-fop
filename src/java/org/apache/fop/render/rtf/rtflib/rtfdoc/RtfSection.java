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

import java.io.Writer;
import java.io.IOException;

/**  Models a section in an RTF document
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfSection
extends RtfContainer
implements
    IRtfParagraphContainer,
    IRtfTableContainer,
    IRtfListContainer,
    IRtfExternalGraphicContainer,
    IRtfBeforeContainer,
    IRtfParagraphKeepTogetherContainer,
    IRtfAfterContainer,
    IRtfJforCmdContainer,
    IRtfTextrunContainer {
    private RtfParagraph paragraph;
    private RtfTable table;
    private RtfList list;
    private RtfExternalGraphic externalGraphic;
    private RtfBefore before;
    private RtfAfter after;
    private RtfJforCmd jforCmd;

    /** Create an RTF container as a child of given container */
    RtfSection(RtfDocumentArea parent, Writer w) throws IOException {
        super(parent, w);
    }

    /**
     * Start a new external graphic after closing current paragraph, list and table
     * @return new RtfExternalGraphic object
     * @throws IOException for I/O problems
     */
    public RtfExternalGraphic newImage() throws IOException {
        closeAll();
        externalGraphic = new RtfExternalGraphic(this, writer);
        return externalGraphic;
    }

    /**
     * Start a new paragraph after closing current paragraph, list and table
     * @param attrs attributes for new RtfParagraph
     * @return new RtfParagraph object
     * @throws IOException for I/O problems
     */
    public RtfParagraph newParagraph(RtfAttributes attrs) throws IOException {
        closeAll();
        paragraph = new RtfParagraph(this, writer, attrs);
        return paragraph;
    }

    /**
     * Close current paragraph if any and start a new one with default attributes
     * @return new RtfParagraph
     * @throws IOException for I/O problems
     */
    public RtfParagraph newParagraph() throws IOException {
        return newParagraph(null);
    }

    /**
     * Close current paragraph if any and start a new one
     * @return new RtfParagraphKeepTogether
     * @throws IOException for I/O problems
     */
    public RtfParagraphKeepTogether newParagraphKeepTogether() throws IOException {
        return new RtfParagraphKeepTogether(this, writer);
    }

    /**
     * Start a new table after closing current paragraph, list and table
     * @param tc Table context used for number-columns-spanned attribute (added by
     * Boris Poudérous on july 2002)
     * @return new RtfTable object
     * @throws IOException for I/O problems
     */
    public RtfTable newTable(ITableColumnsInfo tc) throws IOException {
        closeAll();
        table = new RtfTable(this, writer, tc);
        return table;
    }

    /**
     * Start a new table after closing current paragraph, list and table
     * @param attrs attributes of new RtfTable
     * @param tc Table context used for number-columns-spanned attribute (added by
     * Boris Poudérous on july 2002)
     * @return new RtfTable object
     * @throws IOException for I/O problems
     */
    public RtfTable newTable(RtfAttributes attrs, ITableColumnsInfo tc) throws IOException {
        closeAll();
        table = new RtfTable(this, writer, attrs, tc);
        return table;
    }

    /**
     * Start a new list after closing current paragraph, list and table
     * @param attrs attributes of new RftList object
     * @return new RtfList
     * @throws IOException for I/O problems
     */
    public RtfList newList(RtfAttributes attrs) throws IOException {
        closeAll();
        list = new RtfList(this, writer, attrs);
        return list;
    }

    /**
     * IRtfBeforeContainer
     * @param attrs attributes of new RtfBefore object
     * @return new RtfBefore object
     * @throws IOException for I/O problems
     */
    public RtfBefore newBefore(RtfAttributes attrs) throws IOException {
        closeAll();
        before = new RtfBefore(this, writer, attrs);
        return before;
    }

    /**
     * IRtfAfterContainer
     * @param attrs attributes of new RtfAfter object
     * @return new RtfAfter object
     * @throws IOException for I/O problems
     */
    public RtfAfter newAfter(RtfAttributes attrs) throws IOException {
        closeAll();
        after = new RtfAfter(this, writer, attrs);
        return after;
    }

    /**
     *
     * @param attrs attributes of new RtfJforCmd
     * @return the new RtfJforCmd
     * @throws IOException for I/O problems
     */
    public RtfJforCmd newJforCmd(RtfAttributes attrs) throws IOException {
        jforCmd  = new RtfJforCmd(this, writer, attrs);
        return jforCmd;
    }



    /**
     * Can be overridden to write RTF prefix code, what comes before our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix() throws IOException {
        writeAttributes(attrib, RtfPage.PAGE_ATTR);
        newLine();
        writeControlWord("sectd");
    }

    /**
     * Can be overridden to write RTF suffix code, what comes after our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix() throws IOException {
        writeControlWord("sect");
    }

    private void closeCurrentTable() throws IOException {
        if (table != null) {
            table.close();
        }
    }

    private void closeCurrentParagraph() throws IOException {
        if (paragraph != null) {
            paragraph.close();
        }
    }

    private void closeCurrentList() throws IOException {
        if (list != null) {
            list.close();
        }
    }

    private void closeCurrentExternalGraphic() throws IOException {
        if (externalGraphic != null) {
            externalGraphic.close();
        }
    }

    private void closeCurrentBefore() throws IOException {
        if (before != null) {
            before.close();
        }
    }

    private void closeAll()
    throws IOException {
        closeCurrentTable();
        closeCurrentParagraph();
        closeCurrentList();
        closeCurrentExternalGraphic();
        closeCurrentBefore();
    }

    /**
     * Returns the current RtfTextrun.
     * @return Current RtfTextrun
     * @throws IOException Thrown when an IO-problem occurs.
     */
    public RtfTextrun getTextrun()
    throws IOException {
        return RtfTextrun.getTextrun(this, writer, null);
    }
}
