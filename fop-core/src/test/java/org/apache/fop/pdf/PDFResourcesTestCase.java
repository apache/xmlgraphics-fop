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

/* $Id: PDFReferenceTestCase.java 1551536 2013-12-17 13:15:06Z vhennebert $ */

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link PDFResources}.
 */
public class PDFResourcesTestCase {

    private AtomicInteger patternCount = new AtomicInteger(0);
    private AtomicInteger objectNummerCount = new AtomicInteger(0);

    /**
     * Test PDF resources output with color space, pattern and shading.
     * @throws IOException
     */
    @Test
    public void testOutput() throws IOException {
        PDFDocument pdfDoc = new PDFDocument(null);
        PDFResources res = new PDFResources(pdfDoc);
        res.addColorSpace(this.createColorSpace());
        PDFResourceContext context = new PDFResourceContext(res);

        context.addPattern(this.createPDFPattern(res, pdfDoc));
        context.addShading(this.createPDFShading(res, pdfDoc));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        res.output(baos);

        String expectedShading = "/Shading << /Sh2 4 0 R >>";
        String expectedPattern = "/Pattern << /Pa1 2 0 R >>\n";
        String expectedColorspace = "/ColorSpace << /cs1 [/Separation /cs1 /DeviceRGB 1 0 R] >>\n";

        String outputString = baos.toString();

        assertTrue(outputString.contains(expectedShading));
        assertTrue(outputString.contains(expectedPattern));
        assertTrue(outputString.contains(expectedColorspace));
    }

    /**
     * Test PDF resources output with color space, pattern and shading,
     * if the PDF resource object has a parent resource object.
     * @throws IOException
     */
    @Test
    public void testOutputWithParent() throws IOException {
        PDFDocument pdfDoc = new PDFDocument(null);
        PDFResources res = new PDFResources(pdfDoc);
        PDFResources resParent = new PDFResources(pdfDoc);
        res.setParentResources(resParent);
        resParent.addColorSpace(this.createColorSpace());
        PDFResourceContext context = new PDFResourceContext(resParent);

        context.addPattern(this.createPDFPattern(resParent, pdfDoc));
        context.addShading(this.createPDFShading(resParent, pdfDoc));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        res.output(baos);

        String expectedShading = "/Shading << /Sh2 4 0 R >>";
        String expectedPattern = "/Pattern << /Pa1 2 0 R >>\n";
        String expectedColorspace = "/ColorSpace << /cs1 [/Separation /cs1 /DeviceRGB 1 0 R] >>\n";

        String outputString = baos.toString();

        assertTrue(outputString.contains(expectedShading));
        assertTrue(outputString.contains(expectedPattern));
        assertTrue(outputString.contains(expectedColorspace));
    }

    /**
     * Test PDF resources output with color space, pattern and shading,
     * if the PDF resource object has a parent resource object, that also has
     * color spaces, patterns and shadings.
     * @throws IOException
     */
    @Test
    public void testOutputWithParent2() throws IOException {
        PDFDocument pdfDoc = new PDFDocument(null);
        PDFResources res = new PDFResources(pdfDoc);
        PDFDictionary shadingDict = new PDFDictionary();
        shadingDict.put("Sh1-1718006973", new PDFReference("9 0 R"));
        res.put("Shading", shadingDict);
        PDFDictionary patternDict = new PDFDictionary();
        patternDict.put("Pa1-1718006973", new PDFReference("10 0 R"));
        res.put("Pattern", patternDict);
        PDFDictionary colorSpaceDict = new PDFDictionary();
        colorSpaceDict.put("DefaultRGB", new PDFReference("11 0 R"));
        res.put("ColorSpace", colorSpaceDict);
        PDFResources resParent = new PDFResources(pdfDoc);
        res.setParentResources(resParent);
        resParent.addColorSpace(this.createColorSpace());
        PDFResourceContext context = new PDFResourceContext(resParent);

        context.addPattern(this.createPDFPattern(resParent, pdfDoc));
        context.addShading(this.createPDFShading(resParent, pdfDoc));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        res.output(baos);

        String outputString = baos.toString();

        String expectedShading = "/Shading << /Sh1-1718006973 9 0 R /Sh2 4 0 R >>";
        String expectedPattern = "/Shading << /Sh1-1718006973 9 0 R /Sh2 4 0 R >>";
        String expectedColorspace = "/ColorSpace << /DefaultRGB 11 0 R"
                + " /cs1 [/Separation /cs1 /DeviceRGB 1 0 R] >>";

        assertTrue(outputString.contains(expectedShading));
        assertTrue(outputString.contains(expectedPattern));
        assertTrue(outputString.contains(expectedColorspace));
    }

    private PDFShading createPDFShading(PDFResources res, PDFDocument pdfDoc) {
        List<Double> coords = new ArrayList<Double>(4);
        coords.add(1d);
        coords.add(1d);
        coords.add(1d);
        coords.add(1d);
        PDFFunction pdfFunction = createPDFFunction();
        PDFDeviceColorSpace deviceColorspace = new PDFDeviceColorSpace(PDFDeviceColorSpace.DEVICE_RGB);
        PDFShading shading = new PDFShading(2, deviceColorspace, coords, pdfFunction);
        shading.setObjectNumber(objectNummerCount.incrementAndGet());
        shading.setDocument(pdfDoc);
        shading.setName("Sh" + patternCount.incrementAndGet());
        return shading;
    }

    private PDFColorSpace createColorSpace() {
        PDFFunction tintFunction = createPDFFunction();
        return new PDFSeparationColorSpace("cs1", tintFunction);
    }

    private PDFFunction createPDFFunction() {
        final Double zero = 0d;
        final Double one = 1d;
        List<Double> domain = Arrays.asList(new Double[] {zero, one});
        List<Double> range = Arrays.asList(new Double[] {zero, one, zero, one, zero, one});
        float[] cZero = new float[] {1f, 1f, 1f};
        float[] cOne = {0f, 0f, 0f};
        PDFFunction tintFunction = new PDFFunction(domain, range, cZero, cOne, 1.0d);
        tintFunction.setObjectNumber(objectNummerCount.incrementAndGet());
        return tintFunction;
    }

    private PDFPattern createPDFPattern(PDFResources res, PDFDocument pdfDoc) {
        List<Double> bbox = new ArrayList<Double>();
        bbox.add(1d);
        bbox.add(1d);
        bbox.add(1d);
        bbox.add(1d);
        List<Double> theMatrix = new ArrayList<Double>();
        for (int i = 0; i < 6; i++) {
            theMatrix.add(1d);
        }

        PDFPattern pattern = new PDFPattern(res, 1, 1, 1, bbox, 1, 1, theMatrix, null,
                new StringBuffer());
        pattern.setObjectNumber(objectNummerCount.incrementAndGet());
        pattern.setDocument(pdfDoc);
        pattern.setName("Pa" + patternCount.incrementAndGet());
        return pattern;
    }
}
