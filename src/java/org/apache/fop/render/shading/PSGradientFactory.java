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

import java.util.List;

import org.apache.fop.pdf.PDFDeviceColorSpace;
import org.apache.fop.render.ps.svg.PSPattern;
import org.apache.fop.render.ps.svg.PSShading;

public class PSGradientFactory extends GradientFactory<PSPattern> {

    @Override
    protected Shading makeShading(int shadingType,
            PDFDeviceColorSpace colorSpace, List<Double> coords, Function function) {
        return new PSShading(shadingType, colorSpace, null, null, false, coords, null, function, null);
    }

    @Override
    protected PSPattern makePattern(int patternType, Shading shading, List<Double> matrix) {
        return new PSPattern(patternType, shading, null, null, matrix);
    }
}
