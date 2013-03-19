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

package org.apache.fop.render.intermediate;

import java.awt.Rectangle;

import javax.xml.transform.sax.SAXResult;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.helpers.DefaultHandler;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

import org.apache.fop.apps.FOUserAgent;

public class IFSerializerTestCase {

    private static final String IMAGE = "image.png";

    private IFSerializer sut;

    private ImageManager imageManager;

    @Before
    public void setUp() throws IFException {
        imageManager = mock(ImageManager.class);
        IFContext context = mockContext();
        sut = new IFSerializer(context);
    }

    private IFContext mockContext() {
        FOUserAgent userAgent = mock(FOUserAgent.class);
        when(userAgent.getImageManager()).thenReturn(imageManager);
        return new IFContext(userAgent);
    }

    @Test
    public void drawImageShouldCloseResources() throws IFException {
        sut.setResult(new SAXResult(new DefaultHandler()));
        whenDrawImageIsCalled(true);
        thenImageResourcesMustBeClosed();
    }

    @Test
    public void failingDrawImageShouldCloseResources() throws IFException {
        // Make drawImage artificially fail by not calling setResult
        whenDrawImageIsCalled(false);
        thenImageResourcesMustBeClosed();
    }

    private void whenDrawImageIsCalled(boolean terminatesNormally) throws IFException {
        boolean exceptionThrown = false;
        try {
            sut.drawImage(IMAGE, new Rectangle(10, 10));
        } catch (Exception e) {
            exceptionThrown = true;
        }
        if (!terminatesNormally) {
            assertTrue(exceptionThrown);
        }
    }

    private void thenImageResourcesMustBeClosed() {
        verify(imageManager).closeImage(eq(IMAGE), any(ImageSessionContext.class));
    }

}
