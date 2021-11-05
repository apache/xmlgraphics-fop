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
package org.apache.fop.render.afp;

import java.io.InputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;

import org.apache.fop.afp.AFPPaintingState;

public class AFPImageHandlerRawStreamTestCase {
    @Test
    public void testIsCompatible() {
        AFPPaintingState state = new AFPPaintingState();
        state.setNativeImagesSupported(true);
        AFPRenderingContext context = new AFPRenderingContext(null, null, state, null, null);
        ImageRawStream stream = new ImageRawStream(null, ImageFlavor.RAW_PDF, (InputStream) null);
        Assert.assertTrue(new AFPImageHandlerRawStream().isCompatible(context, stream));
        Assert.assertFalse(Arrays.asList(
                new AFPImageHandlerRawStream().getSupportedImageFlavors()).contains(ImageFlavor.RAW));
    }
}
