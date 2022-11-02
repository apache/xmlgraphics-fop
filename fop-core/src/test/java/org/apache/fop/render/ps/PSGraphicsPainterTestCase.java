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
package org.apache.fop.render.ps;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.fo.Constants;

public class PSGraphicsPainterTestCase {
    @Test
    public void testDrawBorderLineDashed() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PSGenerator generator = new PSGenerator(bos);
        PSGraphicsPainter sut = new PSGraphicsPainter(generator);
        sut.drawBorderLine(0, 0, 0, 0, true, true, Constants.EN_DASHED, Color.BLACK);
        Assert.assertEquals(bos.toString(), "0 LW\n0 0 M 0 0 L S N\n");
    }
}
