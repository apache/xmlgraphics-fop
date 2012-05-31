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
package org.apache.fop.render.ps;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PSPainterTestCase {

    private PSDocumentHandler docHandler;
    private PSPainter psPainter;
    private PSGenerator gen;
    private IFState state;

    @Before
    public void setup() {
        state = IFState.create();
        FOUserAgent userAgent = mock(FOUserAgent.class);
        when(userAgent.getRendererOptions()).thenReturn(Collections.EMPTY_MAP);
        IFContext context = mock(IFContext.class);
        when(context.getUserAgent()).thenReturn(userAgent);
        docHandler = new PSDocumentHandler(context);
        gen = mock(PSGenerator.class);
        docHandler.gen = gen;
        state = IFState.create();
        psPainter = new PSPainter(docHandler, state);
    }

    @Test
    public void testNonZeroFontSize() throws IOException {
        testFontSize(6, times(1));
    }

    @Test
    public void testZeroFontSize() throws IOException {
        testFontSize(0, never());
    }

    private void testFontSize(int fontSize, VerificationMode test) throws IOException {
        state.setFontSize(fontSize);
        try {
            psPainter.drawText(10, 10, 2, 2, null, "Test");
        } catch (Exception ex) {
            //Expected
        }
        verify(gen, test).useColor(state.getTextColor());
    }
}
