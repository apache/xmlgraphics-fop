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

package org.apache.fop.render.shading;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.pdf.PDFFunction;
import org.apache.fop.pdf.PDFPattern;
import org.apache.fop.pdf.PDFShading;
import org.apache.fop.svg.PDFGraphics2D;

public class PDFGradientFactory extends GradientFactory<PDFPattern> {

    private final PDFGraphics2D graphics2D;

    public PDFGradientFactory(PDFGraphics2D pdfGraphics2D) {
        this.graphics2D = pdfGraphics2D;
    }

    @Override
    public Shading makeShading(int shadingType,
            PDFDeviceColorSpace colorSpace, List<Double> background, List<Double> bbox,
            boolean antiAlias, List<Double> coords, List<Double> domain,
            Function function, List<Integer> extend) {
        List<PDFFunction> pdfFunctions = new ArrayList<PDFFunction>(function.getFunctions().size());
        for (Function f : function.getFunctions()) {
            pdfFunctions.add(graphics2D.registerFunction(new PDFFunction(f)));
        }
        PDFFunction pdfFunction = graphics2D.registerFunction(new PDFFunction(function, pdfFunctions));
        PDFShading newShading = new PDFShading(shadingType, colorSpace, background,
                    bbox, antiAlias, coords, domain, pdfFunction, extend);
        newShading = graphics2D.registerShading(newShading);
        return newShading;
    }

    @Override
    public PDFPattern makePattern(int patternType, Shading shading, List xuid,
            StringBuffer extGState, List<Double> matrix) {
        PDFPattern newPattern = new PDFPattern(patternType, shading, xuid, extGState, matrix);
        newPattern = graphics2D.registerPattern(newPattern);
        return newPattern;
    }

}
