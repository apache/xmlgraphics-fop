/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

// Author : Seshadri G

package org.apache.fop.mif;

// images are the one place that FOP classes outside this package get
// referenced and I'd rather not do it

import org.apache.fop.image.FopImage;
import org.apache.fop.layout.LinkSet;
import org.apache.fop.datatypes.ColorSpace;

import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.FontMetric;
import org.apache.fop.layout.FontDescriptor;
// Java
import java.io.*;
import java.io.PrintWriter;
import java.util.*;
import java.awt.Rectangle;

/**
 * class representing a MIF document.
 *
 * The document is built up by calling various methods and then finally
 * output to given filehandle using output method.
 *
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
        Vector content = new Vector();
        public Frame(int x, int y, int w, int h) {

            this.ID = curIDCounter.getnewID();
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

        }

        public void addContent(ImportObject obj) {

            content.addElement(obj);

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

            Enumeration e = content.elements();
            while (e.hasMoreElements()) {

                ((ImportObject)e.nextElement()).output(stream);
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
        private Vector textRects;
        public Page(String pageType, String pageTag, String pageBackground) {

            this.pageType = pageType;
            this.pageTag = pageTag;
            this.pageBackground = pageBackground;
            this.textRects = new Vector();
        }

        public Page() {

            this.pageType = "BodyPage";
            this.pageBackground = "Default";
            this.textRects = new Vector();

        }


        public void addTextRect(int numCols) {

            TextRect textRect = new TextRect(numCols);
            this.textRects.addElement(textRect);
        }

        public TextRect curTextRect() {

            return (TextRect)textRects.lastElement();

        }



        public void output(OutputStream stream) throws IOException {


            String mif = "\n<Page" + "\n\t<PageType " + pageType + ">"
                         + "\n\t<PageBackground " + "`" + pageBackground
                         + "'" + ">";

            byte buf[] = mif.getBytes();

            stream.write(buf);

            Enumeration e = textRects.elements();

            while (e.hasMoreElements()) {

                ((TextRect)e.nextElement()).output(stream);

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

        Vector paras;
        private int ID;    // This ID is used within ParaLine, however it is
        // logical to keep it unique	to a textflow

        public TextFlow() {


            // The current textrect into which the textflow goes
            // is the last created.

            this.ID =
                ((bookComponent.curPage()).curTextRect()).getTextRectID();
            this.paras = new Vector();

        }


        public int getTextRectID() {

            return ID;

        }

        public Para curPara() {

            return (Para)paras.lastElement();
        }

        public void startPara() {

            this.paras.addElement(new Para(ID));
        }

        public void output(OutputStream stream) throws IOException {
            String mif = "\n<TextFlow";
            stream.write(mif.getBytes());
            Enumeration e = paras.elements();
            while (e.hasMoreElements()) {

                ((Para)e.nextElement()).output(stream);
            }
            mif = "\n> #End TextFlow";
            stream.write(mif.getBytes());
        }

    }


    class Para {

        Vector paraLines;
        int ID;      // Same as TextRectID
        ParagraphFormat pgf =
            null;    // This corresponds to to the block properties
        public Para() {

            this.ID = 0;
            this.paraLines = new Vector();
        }


        public Para(int ID) {

            this.ID = ID;
            this.paraLines = new Vector();

        }

        public ParaLine curParaLine() {
            if (paraLines.isEmpty()) {
                return null;
            } else {
                return (ParaLine)paraLines.lastElement();
            }
        }

        void startParaLine() {

            this.paraLines.addElement(new ParaLine(ID));

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
            Enumeration e = paraLines.elements();
            while (e.hasMoreElements()) {

                ((ParaLine)e.nextElement()).output(stream);
            }
            mif = "\n> #End ParaLine";
            stream.write(mif.getBytes());

        }

    }

    class ParaLine {
        Vector content;
        int textRectID;
        String tableID;
        String aFrameID;
        public ParaLine(int textRectID) {

            this.textRectID = textRectID;
            this.content = new Vector();

        }

        public ParaLine() {

            this.textRectID = 0;    // There is no ID used, in tables
            this.content = new Vector();
        }

        public void addContent(Object obj) {

            this.content.addElement(obj);

        }

        public void output(OutputStream stream) throws IOException {

            String mif = "\n<ParaLine";

            // tables dont need the textrectid
            if (textRectID != 0)
                mif += "\n\t<TextRectID " + textRectID + ">";

            stream.write(mif.getBytes());
            Enumeration e = this.content.elements();
            while (e.hasMoreElements()) {

                Object elem = (Object)e.nextElement();
                if (elem instanceof String) {

                    // Output newlines as char hard return

                    if (elem == "\n") {

                        mif = "\n<Char HardReturn>";
                    } else {
                        mif = "\n\t<String `" + elem + "'>";
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

        Vector pgfs;    // Paragraph formats
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

        Vector ruling = new Vector();
        public RulingCatalog() {

            // Add the defualt ruling to the catalog

            ruling.addElement(new Ruling());

        }

        public void output(OutputStream stream) throws IOException {

            String mif = "\n<RulingCatalog";
            stream.write(mif.getBytes());
            Enumeration e = ruling.elements();
            while (e.hasMoreElements()) {
                ((Ruling)e.nextElement()).output(stream);
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
                private Vector paras;    // Paras
                public Cell(int rowSpan, int colSpan) {

                    this.rowSpan = rowSpan;
                    this.colSpan = colSpan;
                    paras = new Vector();

                }

                public void startPara() {

                    this.paras.addElement(new Para());
                }

                public void output(OutputStream stream) throws IOException {

                    String mif = "\n\t\t<Cell" + "\n\t\t<CellContent";
                    stream.write(mif.getBytes());
                    Enumeration e = paras.elements();
                    while (e.hasMoreElements()) {
                        ((Para)e.nextElement()).output(stream);
                    }
                    mif = "\n\t\t> #End CellContent";
                    mif += "\n\t> #End Cell";
                    stream.write(mif.getBytes());
                }

            }
            private Vector cells;

            public void addCell(int rowSpan, int colSpan) {

                cells.addElement(new Cell(rowSpan, colSpan));
            }

            public Row() {

                cells = new Vector();

            }

            public Cell curCell() {

                return (Cell)this.cells.lastElement();
            }

            public void output(OutputStream stream) throws IOException {

                String mif = "\n\t<Row";
                stream.write(mif.getBytes());
                Enumeration e = cells.elements();
                while (e.hasMoreElements()) {
                    ((Cell)e.nextElement()).output(stream);
                }
                mif = "\n\t> #End Row";
                stream.write(mif.getBytes());

            }


        }

        private int ID;
        private Vector tblColumns = new Vector();
        private Vector tblBody, tblHead, tblFoot;
        private Vector current;    // is a reference to one of tblHead,tblBody or tblFoot
        public void addColumn(int colWidth) {

            tblColumns.addElement(new TblColumn(colWidth));

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

            this.current.addElement(new Row());
        }

        public void startCell(int rowSpan, int colSpan) {

            // Add a cell into the current row

            ((Row)this.current.lastElement()).addCell(rowSpan, colSpan);

        }

        public Tbl() {

            this.ID = curIDCounter.getnewID();
            tblBody = new Vector();
            tblHead = new Vector();
            tblFoot = new Vector();

        }

        public int getID() {

            return this.ID;
        }

        public Para curPara() {

            // Return the last para of the current cell

            Row curRow;
            curRow = (Row)this.current.lastElement();
            return (Para)curRow.curCell().paras.lastElement();


        }

        public void startPara() {

            // start a new para in the current cell
            Row curRow;
            curRow = (Row)this.current.lastElement();
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
                Enumeration e = tblHead.elements();
                while (e.hasMoreElements()) {
                    ((Row)e.nextElement()).output(stream);
                }
            }
            if (!tblFoot.isEmpty()) {
                Enumeration e = tblFoot.elements();
                while (e.hasMoreElements()) {
                    ((Row)e.nextElement()).output(stream);
                }
            }
            if (!tblBody.isEmpty()) {
                mif = "\n\t<TblBody";
                stream.write(mif.getBytes());
                Enumeration e = tblBody.elements();
                while (e.hasMoreElements()) {
                    ((Row)e.nextElement()).output(stream);
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
        Vector aFrames = new Vector();
        Vector tables = new Vector();
        Vector pages = new Vector();
        Vector textFlows = new Vector();


        public BookComponent() {

            document = null;    // Initially no values are available
            pgfCatalog = new PgfCatalog();
            rulingCatalog = new RulingCatalog();
        }


        public Frame createFrame(int x, int y, int w, int h) {

            Frame frame = new Frame(x, y, w, h);
            aFrames.addElement(frame);
            return frame;

        }

        public Frame curFrame() {

            return (Frame)aFrames.lastElement();

        }

        public TextFlow curTextFlow() {

            return (TextFlow)textFlows.lastElement();

        }

        public Tbl createTable() {

            Tbl table = new Tbl();
            tables.addElement(table);
            return table;
        }

        public Tbl curTable() {

            return (Tbl)tables.lastElement();
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
                Enumeration e = aFrames.elements();
                while (e.hasMoreElements()) {

                    ((Frame)e.nextElement()).output(stream);
                }

                mif = "\n>";
                stream.write(mif.getBytes());
            }

            if (!tables.isEmpty()) {

                mif = "\n<Tbls";
                stream.write(mif.getBytes());
                Enumeration e = tables.elements();
                while (e.hasMoreElements()) {

                    ((Tbl)e.nextElement()).output(stream);
                }

                mif = "\n>";
                stream.write(mif.getBytes());
            }


            Enumeration e = pages.elements();
            while (e.hasMoreElements()) {

                ((Page)e.nextElement()).output(stream);
            }

            e = textFlows.elements();
            while (e.hasMoreElements()) {

                ((TextFlow)e.nextElement()).output(stream);

            }
        }

        private Page curPage() {
            return (Page)pages.lastElement();
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

        bookComponent.pages.addElement(new Page());

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

            //log.warn("FrameMaker doesnt support different page-sizes   in a document");
        }

    }

    public void createTextRect(int numCols) {

        // Create a textrect on the bodypage with these dimensions
        // This default behaviour will later be changed to reflect on
        // the master-page


        (bookComponent.curPage()).addTextRect(numCols);


        // Then create a textflow corresponding to this textrect

        curFlow = new TextFlow();
        bookComponent.textFlows.addElement(curFlow);
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
