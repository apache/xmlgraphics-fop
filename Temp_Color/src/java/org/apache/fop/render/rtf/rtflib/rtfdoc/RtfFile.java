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

import org.apache.fop.render.rtf.rtflib.exceptions.RtfStructureException;
import java.io.Writer;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

/**
 * Models the top-level structure of an RTF file.
 * @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 * @author Andreas Putz a.putz@skynamics.com
 * @author Christopher Scott scottc@westinghouse.com
 */

public class RtfFile
extends RtfContainer {
    private RtfHeader header;
    private RtfPageArea pageArea;
    private RtfListTable listTable;
    private RtfDocumentArea docArea;
//    private ConverterLogChannel m_log;
    private RtfContainer listTableContainer;
    private int listNum = 0;

    /**
     * Create an RTF file that outputs to the given Writer
     * @param w the Writer to write to
     * @throws IOException for I/O problems
     */
    public RtfFile(Writer w) throws IOException {
        super(null, w);
    }

    /** optional log channel */
//    public void setLogChannel(ConverterLogChannel log)
//    {
//        m_log = log;
//    }

    /**
     * Gets the log channel.
     * If logchannel not set, it will return a empty log channel.
     * @return our log channel, it is never null */
//    ConverterLogChannel getLog()
//    {
//        if (m_log == null)
//            m_log = new ConverterLogChannel (null);
//        return m_log;
//    }

    /**
     * If called, must be called before startDocumentArea
     * @return the new RtfHeader
     * @throws IOException for I/O problems
     * @throws RtfStructureException for illegal RTF structure
     */
    public RtfHeader startHeader()
    throws IOException, RtfStructureException {
        if (header != null) {
            throw new RtfStructureException("startHeader called more than once");
        }
        header = new RtfHeader(this, writer);
        listTableContainer = new RtfContainer(this, writer);
        return header;
    }

    /**
     * Creates the list table.
     * @param attr attributes for the RtfListTable
     * @return the new RtfListTable
     * @throws IOException for I/O problems
     */
    public RtfListTable startListTable(RtfAttributes attr)
    throws IOException {
        listNum++;
        if (listTable != null) {
            return listTable;
        } else {
            listTable = new RtfListTable(this, writer, new Integer(listNum), attr);
            listTableContainer.addChild(listTable);
        }

        return listTable;
    }

    /**
     * Get the list table.
     * @return the RtfListTable
     */
    public RtfListTable getListTable() {
        return listTable;
    }

    /**
     * Closes the RtfHeader if not done yet, and starts the docment area.
     * Like startDocumentArea, is only called once. This is not optimal,
     * must be able to have multiple page definition, and corresponding
     * Document areas
     * @return the RtfPageArea
     * @throws IOException for I/O problems
     * @throws RtfStructureException for illegal RTF structure
     */
    public RtfPageArea startPageArea()
    throws IOException, RtfStructureException {
        if (pageArea != null) {
            throw new RtfStructureException("startPageArea called more than once");
        }
        // create an empty header if there was none
        if (header == null) {
            startHeader();
        }
        header.close();
        pageArea = new RtfPageArea(this, writer);
        addChild(pageArea);
        return pageArea;
    }

    /**
     * Call startPageArea if needed and return the page area object.
     * @return the RtfPageArea
     * @throws IOException for I/O problems
     * @throws RtfStructureException for illegal RTF structure
     */
    public RtfPageArea getPageArea()
    throws IOException, RtfStructureException {
        if (pageArea == null) {
            return startPageArea();
        }
        return pageArea;
    }

    /**
     * Closes the RtfHeader if not done yet, and starts the document area.
     * Must be called once only.
     * @return the RtfDocumentArea
     * @throws IOException for I/O problems
     * @throws RtfStructureException for illegal RTF structure
     */
    public RtfDocumentArea startDocumentArea()
        throws IOException, RtfStructureException {
        if (docArea != null) {
            throw new RtfStructureException("startDocumentArea called more than once");
        }
        // create an empty header if there was none
        if (header == null) {
            startHeader();
        }
        header.close();
        docArea = new RtfDocumentArea(this, writer);
        addChild(docArea);
        return docArea;
    }



    /**
     * Call startDocumentArea if needed and return the document area object.
     * @return the RtfDocumentArea
     * @throws IOException for I/O problems
     * @throws RtfStructureException for illegal RTF structure
     */
    public RtfDocumentArea getDocumentArea()
    throws IOException, RtfStructureException {
        if (docArea == null) {
            return startDocumentArea();
        }
        return docArea;
    }

    /**
     * overridden to write RTF prefix code, what comes before our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix() throws IOException {
        writeGroupMark(true);
        writeControlWord("rtf1");
    }

    /**
     * overridden to write RTF suffix code, what comes after our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix() throws IOException {
        writeGroupMark(false);
    }

    /**
     * must be called when done creating the document
     * @throws IOException for I/O problems
     */
    public synchronized void flush() throws IOException {
        writeRtf();
        writer.flush();
    }

    /**
     * minimal test and usage example
     * @param args command-line arguments
     * @throws Exception for problems
     */
    public static void main(String[] args)
    throws Exception {
        Writer w = null;
        if (args.length != 0) {
            final String outFile = args[0];
            System.err.println("Outputting RTF to file '" + outFile + "'");
            w = new BufferedWriter(new FileWriter(outFile));
        } else {
            System.err.println("Outputting RTF code to standard output");
            w = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        final RtfFile f = new RtfFile(w);
        final RtfSection sect = f.startDocumentArea().newSection();

        final RtfParagraph p = sect.newParagraph();
        p.newText("Hello, RTF world.\n", null);
        final RtfAttributes attr = new RtfAttributes();
        attr.set(RtfText.ATTR_BOLD);
        attr.set(RtfText.ATTR_ITALIC);
        attr.set(RtfText.ATTR_FONT_SIZE, 36);
        p.newText("This is bold, italic, 36 points", attr);

        f.flush();
        System.err.println("RtfFile test: all done.");
    }
}
