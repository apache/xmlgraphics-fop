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

package org.apache.fop.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;

import junit.framework.Assert;

import org.junit.Test;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.xmlgraphics.util.UnitConv;

import org.apache.fop.svg.PDFDocumentGraphics2D;

/**
 * Tests for {@link PDFDocumentGraphics2D}.
 */
public class PDFDocumentGraphics2DTestCase {

    /**
     * Does a smoke test on PDFDocumentGraphics2D making sure that nobody accidentally broke
     * anything serious. It does not check the correctness of the produced PDF.
     * @throws Exception if an error occurs
     */
    @Test
    public void smokeTest() throws Exception {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        PDFDocumentGraphics2D g2d = new PDFDocumentGraphics2D(false);
        g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

        //Set up the document size
        Dimension pageSize = new Dimension(
                (int)Math.ceil(UnitConv.mm2pt(210)),
                (int)Math.ceil(UnitConv.mm2pt(297))); //page size A4 (in pt)
        g2d.setupDocument(baout, pageSize.width, pageSize.height);

        //A few rectangles rotated and with different color
        Graphics2D copy = (Graphics2D)g2d.create();
        int c = 12;
        for (int i = 0; i < c; i++) {
            float f = ((i + 1) / (float)c);
            Color col = new Color(0.0f, 1 - f, 0.0f);
            copy.setColor(col);
            copy.fillRect(70, 90, 50, 50);
            copy.rotate(-2 * Math.PI / c, 70, 90);
        }
        copy.dispose();

        //Some text
        g2d.rotate(-0.25);
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("sans-serif", Font.PLAIN, 36));
        g2d.drawString("Hello world!", 140, 140);
        g2d.setColor(Color.RED.darker());
        g2d.setFont(new Font("serif", Font.PLAIN, 36));
        g2d.drawString("Hello world!", 140, 180);

        g2d.nextPage(); //Move to next page

        g2d.setFont(new Font("sans-serif", Font.PLAIN, 36));
        g2d.drawString("Welcome to page 2!", 140, 140);

        //Cleanup
        g2d.finish();

        String pdfString = baout.toString("ISO-8859-1");
        Assert.assertEquals("%%EOF not found",
                pdfString.substring(pdfString.length() - 6), "%%EOF\n");
    }

}
