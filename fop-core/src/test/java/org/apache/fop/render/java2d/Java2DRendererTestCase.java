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

/* $Id: Java2DRenderer.java 1827168 2018-03-19 08:49:57Z ssteiner $ */
package org.apache.fop.render.java2d;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.fo.Constants;

public class Java2DRendererTestCase {
    @Test
    public void testPrint() throws Exception {
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        Java2DRenderer java2DRenderer = new Java2DRenderer(userAgent) {
            public String getMimeType() {
                return null;
            }
        };
        PageViewport pageViewport = new PageViewport(new Rectangle(), 0, null, null, true);
        pageViewport.setPageIndex(0);
        Page page = new Page();
        RegionViewport regionViewport = new RegionViewport(new Rectangle());
        BodyRegion bodyRegion = new BodyRegion(Constants.FO_REGION_BODY, null, regionViewport, 0, 0);
        bodyRegion.setCTM(new CTM());
        bodyRegion.getMainReference().createSpan(true);
        regionViewport.setRegionReference(bodyRegion);
        page.setRegionViewport(Constants.FO_REGION_BODY, regionViewport);
        pageViewport.setPage(page);
        java2DRenderer.renderPage(pageViewport);
        BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
        Assert.assertEquals(java2DRenderer.print(image.createGraphics(), null, 0), 0);
    }
}
