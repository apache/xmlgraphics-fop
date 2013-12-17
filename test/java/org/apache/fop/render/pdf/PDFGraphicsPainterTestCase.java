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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.fop.pdf.PDFNumber;

public class PDFGraphicsPainterTestCase {

    private PDFGraphicsPainter sut;

    private PDFContentGenerator generator;

    @Before
    public void setup() {
        generator = mock(PDFContentGenerator.class);
        sut = new PDFGraphicsPainter(generator);
    }

    @Test
    public void moveTo() {
        int x = 10;
        int y = 20;
        sut.moveTo(x, y);
        verify(generator).add(op("m", x, y));
    }

    @Test
    public void lineTo() {
        int x = 10;
        int y = 20;
        sut.lineTo(x, y);
        verify(generator).add(op("l", x, y));
    }

    @Test
    public void arcTo() throws IOException {
        int width = 10;
        int height = 10;
        int x = 0;
        int y = 0;
        double startAngle = 0;
        double endAngle = Math.PI / 2;
        sut.arcTo(startAngle, endAngle, x, y, width, height);
        //TODO stricter verification
        verify(generator).add(endsWith(" c "));
    }

    @Test
    public void closePath() {
        sut.closePath();
        verify(generator).add(op("h"));
    }

    @Test
    public void clip() {
        sut.clip();
        verify(generator).add(opln("W\nn"));
    }

    @Test
    public void saveGraphicsState() {
        sut.saveGraphicsState();
        verify(generator).add(opln("q"));
    }

    @Test
    public void restoreGraphicsState() {
        sut.restoreGraphicsState();
        verify(generator).add(opln("Q"));
    }

    @Test
    public void rotateCoordinates() throws IOException {
        double angle = 0;
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        sut.rotateCoordinates(angle);
        testTransformCoordinatesF(c, s, -s, c, 0, 0);
    }

    @Test
    public void translateCoordinates() throws IOException {
        int x = 10;
        int y = 20;
        sut.translateCoordinates(x, y);
        testTransformCoordinates(1000, 0, 0, 1000, x, y);
    }

    @Test
    public void scaleCoordinates() throws IOException {
        float xScaleFactor = 10f;
        float yScaleFactor = 2f;
        sut.scaleCoordinates(xScaleFactor, yScaleFactor);
        testTransformCoordinatesF(xScaleFactor, 0f, 0f, yScaleFactor, 0f, 0f);
    }

    @Test
    public void cubicBezierTo() {
        int[] args = new int[]{1, 2, 3, 4, 5, 6};
        sut.cubicBezierTo(args[0], args[1], args[2], args[3], args[4], args[5]);
        verify(generator).add(op("c", args));
    }

    private void testTransformCoordinatesF(float... args) {
        verify(generator).add(opf("cm", args));
    }

    private void testTransformCoordinates(int... args) {
        verify(generator).add(op("cm", args));
    }

    private String opf(String op, float... args) {
        return opf(op, " ", args);
    }

    private String op(String op, int... args) {
        return op(op, " ", args);
    }

    private String opln(String op, int... args) {
        return op(op, "\n", args);
    }

    private String opf(String op, String ending, float... args) {
        StringBuilder sb = new StringBuilder();
        for (float arg : args) {
            sb.append("" +  PDFNumber.doubleOut(arg) + " ");
        }
        return sb.append(op.trim()).append(ending).toString();
    }

    private String op(String op, String ending, int... args) {
        float[] formattedArgs = new float[args.length];
        for (int i = 0; i < args.length; i++) {
            formattedArgs[i] = format(args[i]);
        }
        return opf(op, ending, formattedArgs);
    }

    private float format(int i) {
        return (float) i / 1000;
    }

}
