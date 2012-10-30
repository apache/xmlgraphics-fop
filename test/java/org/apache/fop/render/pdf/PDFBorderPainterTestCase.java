package org.apache.fop.render.pdf;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import org.apache.fop.fo.Constants;
import org.apache.fop.pdf.PDFDocument;

public class PDFBorderPainterTestCase {

    private PDFContentGenerator generator;
    private ByteArrayOutputStream outStream;
    private PDFGraphicsPainter borderPainter;

    @Before
    public void setUp() {
        outStream = new ByteArrayOutputStream();
        generator = new PDFContentGenerator(new PDFDocument("test"), outStream, null);
        borderPainter = new PDFGraphicsPainter(generator);
    }

    /**
     * This test will fail if either of the below statements isn't true:
     * org.apache.fop.render.intermediate.BorderPainter.DASHED_BORDER_SPACE_RATIO = 0.5f:q
     * org.apache.fop.render.intermediate.BorderPainter.DASHED_BORDER_LENGTH_FACTOR = 4.0f.
     */
    @Test
    public void testDrawBorderLine() throws Exception {
        borderPainter.drawBorderLine(0, 0, 40000, 1000, true, true,
                Constants.EN_DASHED, Color.BLACK);
        generator.flushPDFDoc();
        OutputStream outStream = new ByteArrayOutputStream();
        outStream = generator.getStream().getBufferOutputStream();
        assertTrue(((ByteArrayOutputStream) outStream).toString().contains("[4 2] 0 d 1 w"));
    }

    public void tearDown() {
        generator = null;
        outStream= null;
    }
}
