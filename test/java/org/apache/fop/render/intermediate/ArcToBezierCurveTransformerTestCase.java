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

package org.apache.fop.render.intermediate;

import java.io.IOException;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ArcToBezierCurveTransformerTestCase {

    @Test
    public void arcTo() throws Exception {
        testArcTo(Math.PI / 3, Math.PI / 2, 100, 200, 1000, 1000);
    }

    private void testArcTo(double startAngle, double endAngle, int xCenter, int yCenter, int width,
            int height) throws IOException {
        assertAngleWithinFirstQuadrant(startAngle);
        assertAngleWithinFirstQuadrant(endAngle);
        BezierCurvePainter bezierCurvePainter = mock(BezierCurvePainter.class);
        ArcToBezierCurveTransformer sut = new ArcToBezierCurveTransformer(bezierCurvePainter);
        sut.arcTo(startAngle, endAngle, xCenter, yCenter, width, height);
        double tan1 = Math.tan(startAngle);
        double tan2 = Math.tan(endAngle);
        double lambda1 = Math.atan(height * tan1 / width);
        double lambda2 = Math.atan(height * tan2 / width);
        double xStart = width * Math.cos(lambda1) + xCenter;
        double yStart = height * Math.sin(lambda1) + yCenter;
        double xEnd = width * Math.cos(lambda2) + xCenter;
        double yEnd = height * Math.sin(lambda2) + yCenter;
        ArgumentCaptor<Integer> xP1Captor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> yP1Captor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> xP2Captor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> yP2Captor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> xP3Captor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> yP3Captor = ArgumentCaptor.forClass(Integer.class);
        verify(bezierCurvePainter).cubicBezierTo(xP1Captor.capture(), yP1Captor.capture(),
                xP2Captor.capture(), yP2Captor.capture(), xP3Captor.capture(), yP3Captor.capture());
        int xP1 = xP1Captor.getValue();
        int yP1 = yP1Captor.getValue();
        int xP2 = xP2Captor.getValue();
        int yP2 = yP2Captor.getValue();
        int xP3 = xP3Captor.getValue();
        int yP3 = yP3Captor.getValue();
        // TODO do more than check the direction of the tangents at the end
        // points
        assertEquals((yP1 - yStart) / (xP1 - xStart), -width * width / height / height / tan1, 0.01);
        assertEquals((yP2 - yEnd) / (xP2 - xEnd), -width * width / height / height / tan2, 0.01);
        assertEquals((int) xEnd, xP3);
        assertEquals((int) yEnd, yP3);
    }

    private void assertAngleWithinFirstQuadrant(double angle) {
        if (angle <= 0 || angle > Math.PI / 2) {
            fail("Angle " + angle + " is in (0, " + Math.PI / 2 + ")");
        }
    }
}