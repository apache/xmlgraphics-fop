/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */

package org.apache.fop.mif;

// images are the one place that FOP classes outside this package get
// referenced and I'd rather not do it

import org.apache.fop.messaging.MessageHandler;

// Java
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * class representing a MIF document.
 *
 * The document is built up by calling various methods and then finally
 * output to given filehandle using output method.
 *
 * @author Seshadri G
 */
public class MIFDocument {

    /**
     * the version of MIF supported
     */


    protected static final String mifVersion = "5.5";
    protected BookComponent bookComponent;
    private Flow curFlow;    // this is a ref to the current flow which could be a textflow or
    // a table
    private ID curIDCounter = new ID();

    public static final String MIFEncode(String val) {
        int len = val.length();
        StringBuffer buf = new StringBuffer(len * 2);
        char c;

        for(int i = 0; i < len; i++) {
            c = val.charAt(i);
            switch(c) {
                case '\u00e0':  buf.append("\\x88 "); break;
                case '\u00e8':  buf.append("\\x8f "); break;
                case '\u00ec':  buf.append("\\x93 "); break;
                case '\u00f2':  buf.append("\\x98 "); break;
                case '\u00f9':  buf.append("\\x9d "); break;
                case '\u00c0':  buf.append("\\xcb "); break;
                case '\u00c8':  buf.append("\\xe9 "); break;
                case '\u00cc':  buf.append("\\xed "); break;
                case '\u00d2':  buf.append("\\xf1 "); break;
                case '\u00d9':  buf.append("\\xf4 "); break;

                case '\u00e1':  buf.append("\\x87 "); break;
                case '\u00e9':  buf.append("\\x8e "); break;
                case '\u00ed':  buf.append("\\x92 "); break;
                case '\u00f3':  buf.append("\\x97 "); break;
                case '\u00fa':  buf.append("\\x9c "); break;
                case '\u00c1':  buf.append("\\xe7 "); break;
                case '\u00c9':  buf.append("\\x83 "); break;
                case '\u00cd':  buf.append("\\xea "); break;
                case '\u00d3':  buf.append("\\xee "); break;
                case '\u00da':  buf.append("\\xf2 "); break;

                case '\u00f1':  buf.append("\\x96 "); break;
                case '\u00d1':  buf.append("\\x84 "); break;

                case '\u00e7':  buf.append("\\x8d "); break;
                case '\u00c7':  buf.append("\\x82 "); break;

                case '`':       buf.append("\\xd4 "); break;
                case '\'':      buf.append("\\xd5 "); break;
                case '\u00b4':  buf.append("\\xab "); break;
                case '\u00aa':  buf.append("\\xbb "); break;
                case '\u00ba':  buf.append("\\xbc "); break;

                case '>':       buf.append("\\>"); break;
                default:        buf.append(c);
            }
        }
        return buf.toString();
    }

    class ID {

        private int idCounter = 1;
        public int getnewID() {

            return idCounter++;
        }

    }

    class FontFormat {

        public FontFormat() {}

    }

    class ParagraphFormat extends FontFormat {

        public ParagraphFormat() {}

        int startIndent;
        int endIndent;



    }

    class Document {
        protected int height;
        protected int width;
        public Document() {}

        public void output(OutputStream stream) throws IOException {

            String mif = "\n<Document " + "\n<DPageSize " + width / 1000f
                         + " " + height / 1000f + " >\n>";
            byte buf[] = mif.getBytes();

            stream.write(buf);

        }

    }


    class PolyLine {

        public PolyLine() {}

    }

    class ImportObject {

        private String url;
        private int x, y, w, h;

        public ImportObject(String url, int x, int y, int w, int h) {

            this.url = url;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

        }


        public void output(OutputStream stream) throws IOException {


            String path = this.url;

            // Strip 'file:'
            path = path.substring(5);
            String result = "";
            int i;
            do {    // replace all matching '/'

                i = path.indexOf("/");
                if (i != -1) {
                    result = path.substring(0, i);
                    result += "<c\\>";
                    result += path.substring(i + 1);
                    path = result;
                }

            } while (i != -1);

            String mif = "\n<ImportObject" + "\n<ImportObFixedSize Yes>";
            mif += "\n\t<ImportObFileDI " + "`<c\\>" + path + "'" + " >";
            mif += "\n\t<ShapeRect " + this.x / 1000f + " " + this.y / 1000f
                   + " " + this.w / 1000f + " " + this.h / 1000f + " >";

            mif += "\n> #End ImportObj";
            stream.write(mif.getBytes());

        }

    }


    class Frame {

        private int ID;
        private int x, y, w, h;
        ArrayList content = new ArrayList();
        public Frame(int x, int y, int w, int h) {

            this.ID = curIDCounter.getnewID();
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

        }

        public void addContent(ImportObject obj) {

            content.add(obj);

        }

        public void output(OutputStream stream) throws IOException {

            String mif = "\n<Frame" + "\n\t<ID " + this.ID + " >";
            mif +=
                "\n\t<Pen 15>\n\t<Fill 7>\n\t<PenWidth  1.0 >\n\t<Separation 0>\n\t<ObColor `Black'>\n\t<DashedPattern \n\t <DashedStyle Solid> \n >";

            mif +=
                "\n\t<RunaroundGap  6.0 pt>\n\t<RunaroundType None>\n\t<Angle  360.0>\n\t<Float No>\n\t<NSOffset  0.0>\n\t<BLOffset  0>\n\t<Cropped No>\n\t<FrameType Below>\n\t<AnchorAlign Center>";

            mif += "\n\t<ShapeRect " + this.x / 1000f + " " + this.y / 1000f
                   + " " + this.w / 1000f + " " + this.h / 1000f + " >";


            stream.write(mif.getBytes());

            for (int i = 0; i < content.size(); i++) {
                ((ImportObject)content.get(i)).output(stream);
            }
            mif = "\n> #End Frame";
            stream.write(mif.getBytes());



        }

        public int getID() {

            return this.ID;
        }

    }

    class TextRect {
        private int rx, ry, w, h;
        private int numCols;
        private int curCol = 0;    // Current column being processed
        private int colGap = 0;
        private int textRectID;
        public TextRect(int numCols) {

            this.numCols = numCols;
            this.curCol = 0;
            this.textRectID = curIDCounter.getnewID();

        }


        public int getTextRectID() {

            return textRectID;

        }


        public void setTextRectProp(int left, int top, int width,
                                    int height) {

            if (curCol == 0) {

                // Use the left and top margins

                rx = left;
                ry = top;
                w = width;    // current column width , not the entire span
                h = height;
                curCol++;

            } else if (curCol == 1) {
                // Figure out the column gap and the span of the textrect
                colGap = left - rx - width;
                // Next the entire width
                w = numCols * width + (numCols - 1) * colGap;
                curCol++;


            }

        }


        public void output(OutputStream stream) throws IOException {

            String mif = "\n<TextRect" + "\n\t<ID " + textRectID + ">"
                         + "\n\t<ShapeRect " + rx / 1000f + " " + ry / 1000f
                         + " " + w / 1000f + " " + h / 1000f + ">";

            if (numCols > 1) {
                mif += "\n<TRNumColumns " + numCols + ">";
                mif += "\n<TRColumnGap " + colGap / 1000f + ">";
            }

            mif += "\n> #End TextRect";
            byte buf[] = mif.getBytes();
            stream.write(buf);
        }

    }

    class Page {
        private String pageType;
        private String pageTag;
        private String pageBackground;
        private ArrayList textRects;
        public Page(String pageType, String pageTag, String pageBackground) {

            this.pageType = pageType;
            this.pageTag = pageTag;
            this.pageBackground = pageBackground;
            this.textRects = new ArrayList();
        }

        public Page() {

            this.pageType = "BodyPage";
            this.pageBackground = "Default";
            this.textRects = new ArrayList();

        }


        public void addTextRect(int numCols) {

            TextRect textRect = new TextRect(numCols);
            this.textRects.add(textRect);
        }

        public TextRect curTextRect() {

            //temporary fix for NoSuchElementException
            if (textRects.isEmpty()) {
                TextRect textRect = new TextRect(1);
                this.textRects.add(textRect);
            }
            return (TextRect)textRects.get(textRects.size() - 1);
        }



        public void output(OutputStream stream) throws IOException {
            String mif = "\n<Page" + "\n\t<PageType " + pageType + ">"
                         + "\n\t<PageBackground " + "`" + pageBackground
                         + "'" + ">";
            byte buf[] = mif.getBytes();
            stream.write(buf);
            for (int i = 0; i < textRects.size(); i++) {
                ((TextRect)textRects.get(i)).output(stream);
            }
            mif = "\n>  #End Page\n";
            stream.write(mif.getBytes());
        }
    }

    abstract class Flow {

        public Flow() {}

        public abstract Para curPara();
        public abstract void startPara();
    }


    class TextFlow extends Flow {

        ArrayList paras;
        private int ID;    // This ID is used within ParaLine, however it is
        // logical to keep it unique to a textflow

        public TextFlow() {


            // The current textrect into which the textflow goes
            // is the last created.

            this.ID =
                ((bookComponent.curPage()).curTextRect()).getTextRectID();
            this.paras = new ArrayList();

        }


        public int getTextRectID() {

            return ID;

        }

        public Para curPara() {
            return (Para)paras.get(paras.size() - 1);
        }

        public void startPara() {
            this.paras.add(new Para(ID));
        }

        public void output(OutputStream stream) throws IOException {
            String mif = "\n<TextFlow";
            stream.write(mif.getBytes());
            for (int i = 0; i < paras.size(); i++) {
                ((Para)paras.get(i)).output(stream);
            }
            mif = "\n> #End TextFlow";
            stream.write(mif.getBytes());
        }
    }


    class Para {

        ArrayList paraLines;
        int ID;      // Same as TextRectID
        ParagraphFormat pgf =
            null;    // This corresponds to to the block properties
        public Para() {

            this.ID = 0;
            this.paraLines = new ArrayList();
        }


        public Para(int ID) {

            this.ID = ID;
            this.paraLines = new ArrayList();

        }

        public ParaLine curParaLine() {
            if (paraLines.isEmpty()) {
                return null;
            } else {
                return (ParaLine)paraLines.get(paraLines.size() - 1);
            }
        }

        void startParaLine() {

            this.paraLines.add(new ParaLine(ID));

        }


        public void setBlockProp(int startIndent, int endIndent) {

            pgf = new ParagraphFormat();
            pgf.startIndent = startIndent;
            pgf.endIndent = endIndent;


        }


        public void output(OutputStream stream) throws IOException {

            String mif = "\n<Para";
            // Is there a block property?

            if (pgf != null) {
                mif += "\n<Pgf";
                mif += "\n<PgfTag `Body'>";
                mif += "\n<PgfLIndent " + pgf.startIndent / 1000f + ">";
                mif += "\n<PgfRIndent " + pgf.endIndent / 1000f + ">";
                mif += "\n>";
            }
            stream.write(mif.getBytes());
            for (int i = 0; i < paraLines.size(); i++) {
                ((ParaLine)paraLines.get(i)).output(stream);
            }
            mif = "\n> #End ParaLine";
            stream.write(mif.getBytes());
        }

    }

    class ParaLine {
        ArrayList content;
        int textRectID;
        String tableID;
        String aFrameID;
        public ParaLine(int textRectID) {

            this.textRectID = textRectID;
            this.content = new ArrayList();

        }

        public ParaLine() {

            this.textRectID = 0;    // There is no ID used, in tables
            this.content = new ArrayList();
        }

        public void addContent(Object obj) {

            this.content.add(obj);

        }

        public void output(OutputStream stream) throws IOException {

            String mif = "\n<ParaLine";

            // tables dont need the textrectid
            if (textRectID != 0) {
                mif += "\n\t<TextRectID " + textRectID + ">";
            }
            stream.write(mif.getBytes());
            for (int i = 0; i < content.size(); i++) {
                Object elem = content.get(i);
                if (elem instanceof String) {
                    // Output newlines as char hard return
                    if (elem == "\n") {
                        mif = "\n<Char HardReturn>";
                    } else {
                        mif = "\n\t<String `" + MIFEncode((String)elem) + "'>";
                    }
                    stream.write(mif.getBytes());
                } else if (elem instanceof Frame) {
                    mif = "\n\t<AFrame " + ((Frame)elem).getID() + " >";
                    stream.write(mif.getBytes());
                } else if (elem instanceof Tbl) {
                    mif = "\n\t<ATbl " + ((Tbl)elem).getID() + " >";
                    stream.write(mif.getBytes());
                }
            }
            mif = "\n> #End ParaLine";
            stream.write(mif.getBytes());

        }

    }

    class PgfCatalog {

        ArrayList pgfs;    // Paragraph formats
        public PgfCatalog() {}

        public void output(OutputStream stream) throws IOException {
            String mif = "\n<PgfCatalog" + "\n<Pgf" + "\n<PgfTag `Body'>"
                         + "\n>" + "\n>";
            stream.write(mif.getBytes());
        }

    }

    class Color {

        public Color() {}

    }

    class ColorCatalog {

        public ColorCatalog() {}

    }

    class Ruling {

        int penWidth;
        int pen;
        int lines;
        public Ruling() {
            // Default ruling
            penWidth = 1;
            pen = 0;
            lines = 1;


        }

        public void output(OutputStream stream) throws IOException {

            String mif = "\n<Ruling \n<RulingTag `Default'>";
            mif += "\n<RulingPenWidth " + penWidth + ">";
            mif += "\n<RulingPen " + pen + ">";
            mif += "\n<RulingLines " + lines + ">";
            mif += "\n>";
            stream.write(mif.getBytes());
        }

    }

    class RulingCatalog {
        // Contains multiple rulings

        ArrayList ruling = new ArrayList();
        public RulingCatalog() {

            // Add the defualt ruling to the catalog

            ruling.add(new Ruling());

        }

        public void output(OutputStream stream) throws IOException {

            String mif = "\n<RulingCatalog";
            stream.write(mif.getBytes());
            for (int i = 0; i < ruling.size(); i++) {
                ((Ruling)ruling.get(i)).output(stream);
            }
            mif = "\n> #End RulingCatalog";
            stream.write(mif.getBytes());

        }

    }

    class TblFormat {
        public TblFormat() {}

    }

    class TblCatalog {
        public TblCatalog() {}

    }

    class Tbl extends Flow {

        class TblColumn {
            private int width;
            public TblColumn(int width) {

                this.width = width;

            }

            public void output(OutputStream stream) throws IOException {

                String mif = "\n\t<TblColumnWidth " + width + " >";
                stream.write(mif.getBytes());

            }


        }

        class Row {

            class Cell {

                private int rowSpan, colSpan;
                private ArrayList paras;    // Paras
                public Cell(int rowSpan, int colSpan) {

                    this.rowSpan = rowSpan;
                    this.colSpan = colSpan;
                    paras = new ArrayList();
                }

                public void startPara() {
                    this.paras.add(new Para());
                }

                public void output(OutputStream stream) throws IOException {

                    String mif = "\n\t\t<Cell" + "\n\t\t<CellContent";
                    stream.write(mif.getBytes());
                    for (int i = 0; i < paras.size(); i++) {
                        ((Para)paras.get(i)).output(stream);
                    }
                    mif = "\n\t\t> #End CellContent";
                    mif += "\n\t> #End Cell";
                    stream.write(mif.getBytes());
                }
            }

            private ArrayList cells;

            public void addCell(int rowSpan, int colSpan) {
                cells.add(new Cell(rowSpan, colSpan));
            }

            public Row() {
                cells = new ArrayList();
            }

            public Cell curCell() {
                return (Cell)this.cells.get(cells.size() - 1);
            }

            public void output(OutputStream stream) throws IOException {
                String mif = "\n\t<Row";
                stream.write(mif.getBytes());
                for (int i = 0; i < cells.size(); i++) {
                    ((Cell)cells.get(i)).output(stream);
                }
                mif = "\n\t> #End Row";
                stream.write(mif.getBytes());
            }
        }

        private int ID;
        private ArrayList tblColumns = new ArrayList();
        private ArrayList tblBody, tblHead, tblFoot;
        private ArrayList current;    // is a reference to one of tblHead,tblBody or tblFoot
        public void addColumn(int colWidth) {

            tblColumns.add(new TblColumn(colWidth));

        }

        public void setCurrent(String current) {

            if (current == "fo:table-body") {
                this.current = this.tblBody;
            } else if (current == "tablehead") {
                this.current = this.tblHead;
            } else if (current == "tablefoot") {
                this.current = this.tblFoot;
            }
        }

        public void startRow() {

            this.current.add(new Row());
        }

        public void startCell(int rowSpan, int colSpan) {

            // Add a cell into the current row
            ((Row)this.current.get(current.size() - 1)).addCell(rowSpan, colSpan);

        }

        public Tbl() {

            this.ID = curIDCounter.getnewID();
            tblBody = new ArrayList();
            tblHead = new ArrayList();
            tblFoot = new ArrayList();

        }

        public int getID() {

            return this.ID;
        }

        public Para curPara() {

            // Return the last para of the current cell
            Row curRow = (Row)this.current.get(current.size() - 1);
            ArrayList paras = curRow.curCell().paras;
            return (Para)paras.get(paras.size() - 1);
        }

        public void startPara() {

            // start a new para in the current cell
            Row curRow;
            curRow = (Row)this.current.get(current.size() - 1);
            curRow.curCell().startPara();

        }

        public void output(OutputStream stream) throws IOException {

            String mif = "\n<Tbl" + "\n\t<TblID " + ID + " >";

            // note tbl format to be added in a later release
            mif += "\n<TblTag Body>" + "\n<TblFormat";
            mif += "\n<TblColumnRuling `Default'>";
            mif += "\n<TblBodyRowRuling `Default'>";
            mif += "\n<TblLRuling `Default'>";
            mif += "\n<TblBRuling `Default'>";
            mif += "\n<TblRRuling `Default'>";
            mif += "\n<TblTRuling `Default'>";

            mif += "\n> #End TblFormat";
            ;
            mif += "\n\t<TblNumColumns " + tblColumns.size() + " >";
            stream.write(mif.getBytes());

            if (!tblHead.isEmpty()) {
                for (int i = 0; i < tblHead.size(); i++) {
                    ((Row)tblHead.get(i)).output(stream);
                }
            }
            if (!tblFoot.isEmpty()) {
                for (int i = 0; i < tblFoot.size(); i++) {
                    ((Row)tblFoot.get(i)).output(stream);
                }
            }
            if (!tblBody.isEmpty()) {
                mif = "\n\t<TblBody";
                stream.write(mif.getBytes());
                for (int i = 0; i < tblBody.size(); i++) {
                    ((Row)tblBody.get(i)).output(stream);
                }
                mif = "\n\t> #End tblBody";
            }
            mif += "\n> #End Table";
            stream.write(mif.getBytes());
        }

    }

    class XRefFormat {

        public XRefFormat() {}

    }

    class CrossRefInfo {

        public CrossRefInfo() {}

    }

    class XRef {

        public XRef() {}

    }

    class Marker {

        public Marker() {}

    }

    class BookComponent {

        Document document;
        ColorCatalog colorCatalog;
        RulingCatalog rulingCatalog;
        PgfCatalog pgfCatalog;
        TblCatalog tblCatalog;
        ArrayList aFrames = new ArrayList();
        ArrayList tables = new ArrayList();
        ArrayList pages = new ArrayList();
        ArrayList textFlows = new ArrayList();


        public BookComponent() {

            document = null;    // Initially no values are available
            pgfCatalog = new PgfCatalog();
            rulingCatalog = new RulingCatalog();
        }


        public Frame createFrame(int x, int y, int w, int h) {
            Frame frame = new Frame(x, y, w, h);
            aFrames.add(frame);
            return frame;
        }

        public Frame curFrame() {
            return (Frame)aFrames.get(aFrames.size() - 1);
        }

        public TextFlow curTextFlow() {
            return (TextFlow)textFlows.get(textFlows.size() - 1);
        }

        public Tbl createTable() {

            Tbl table = new Tbl();
            tables.add(table);
            return table;
        }

        public Tbl curTable() {

            return (Tbl)tables.get(tables.size() - 1);
        }

        public void output(OutputStream stream) throws IOException {
            String mif = "<MIFFile 5.00>" + "\n<Units Upt>";
            stream.write(mif.getBytes());
            pgfCatalog.output(stream);
            rulingCatalog.output(stream);
            document.output(stream);

            if (!aFrames.isEmpty()) {
                mif = "\n<AFrames";
                stream.write(mif.getBytes());
                for (int i = 0; i < aFrames.size(); i++) {
                    ((Frame)aFrames.get(i)).output(stream);
                }
                mif = "\n>";
                stream.write(mif.getBytes());
            }

            if (!tables.isEmpty()) {
                mif = "\n<Tbls";
                stream.write(mif.getBytes());
                for (int i = 0; i < tables.size(); i++) {
                    ((Tbl)tables.get(i)).output(stream);
                }
                mif = "\n>";
                stream.write(mif.getBytes());
            }

            for (int i = 0; i < pages.size(); i++) {
                ((Page)pages.get(i)).output(stream);
            }

            for (int i = 0; i < textFlows.size(); i++) {
                ((TextFlow)textFlows.get(i)).output(stream);
            }
        }

        private Page curPage() {
            return (Page)pages.get(pages.size() - 1);
        }

    }

    class ElementSet {
        public ElementSet() {}

    }

    /**
     * creates an empty MIF document
     */
    public MIFDocument() {

        bookComponent = new BookComponent();

    }

    public void createPage() {

        bookComponent.pages.add(new Page());

    }

    public void addToStream(String s) {

        // Add this string to the curent flow

        Para para = curFlow.curPara();
        ParaLine paraLine = para.curParaLine();
        paraLine.addContent(s);

    }

    public void output(OutputStream stream) throws IOException {
        // Output the contents of bookComponent

        this.bookComponent.output(stream);

    }

    public void setDocumentHeightWidth(int height, int width) {

        if (bookComponent.document == null) {

            bookComponent.document = new Document();
            bookComponent.document.height = height;
            bookComponent.document.width = width;
        } else if (bookComponent.document.height != height
                   || bookComponent.document.width != width) {

            MessageHandler.logln("Warning : FrameMaker doesnt support different page-sizes   in a document");
        }

    }

    public void createTextRect(int numCols) {

        // Create a textrect on the bodypage with these dimensions
        // This default behaviour will later be changed to reflect on
        // the master-page


        (bookComponent.curPage()).addTextRect(numCols);


        // Then create a textflow corresponding to this textrect

        curFlow = new TextFlow();
        bookComponent.textFlows.add(curFlow);
    }

    public void setTextRectProp(int left, int top, int width, int height) {

        (bookComponent.curPage()).curTextRect().setTextRectProp(left, top,
                width, height);


    }

    public void startLine() {

        if (curFlow.curPara().curParaLine() != null) {
            this.addToStream("\n");
            curFlow.curPara().startParaLine();
        } else
            curFlow.curPara().startParaLine();


    }

    public void setBlockProp(int startIndent, int endIndent) {


        curFlow.startPara();    // Start a para
        curFlow.curPara().setBlockProp(startIndent, endIndent);

    }

    public void createFrame(int x, int y, int w, int h) {

        // Create a new anchored frame

        bookComponent.createFrame(x, y, w, h);

    }

    public void addImage(String url, int x, int y, int w, int h) {

        Frame frame = bookComponent.createFrame(x, y, w, h);
        ImportObject imageObject = new ImportObject(url, 0, 0, w, h);
        frame.addContent(imageObject);
        if (curFlow.curPara().curParaLine() == null) {
            curFlow.curPara().startParaLine();

        }
        curFlow.curPara().curParaLine().addContent(frame);


    }

    public void createTable() {

        // First create a table with an ID, then add it to the textflow

        Tbl table = bookComponent.createTable();
        if (curFlow.curPara().curParaLine() == null) {
            curFlow.curPara().startParaLine();

        }
        curFlow.curPara().curParaLine().addContent(table);

        /*
         * The above would have added the table to the textflow
         * But now the flow goes into the table, so ...
         */

        curFlow = table;

    }

    public void setColumnProp(int colWidth) {


        // Get the current table

        Tbl table = bookComponent.curTable();
        table.addColumn(colWidth);


    }

    public void setCurrent(String current) {

        // Start the table body or header or footer
        Tbl table = bookComponent.curTable();
        table.setCurrent(current);

    }

    public void startRow() {

        Tbl table = bookComponent.curTable();
        table.startRow();


    }

    public void startCell(int rowSpan, int colSpan) {

        Tbl table = bookComponent.curTable();
        table.startCell(rowSpan, colSpan);

    }

    public void endTable() {

        // Switch the ref back to the current textflow

        curFlow = bookComponent.curTextFlow();

    }

}
