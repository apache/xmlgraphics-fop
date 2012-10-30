package org.apache.fop.render.ps;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.fo.Constants;

public class PSBorderPainterTestCase {

    private PSGenerator generator;
    private ByteArrayOutputStream outStream;
    private PSGraphicsPainter borderPainter;

    @Before
    public void setUp() {
        outStream = new ByteArrayOutputStream();
        generator = new PSGenerator(outStream);
        borderPainter = new PSGraphicsPainter(generator);
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
        assertTrue(outStream.toString().contains("[4.0 2.0] 0 setdash"));
    }

    public void tearDown() {
        generator = null;
        outStream= null;
    }
}
