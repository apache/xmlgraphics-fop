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

/* $Id: ActiveLayouts.java 99 2008-11-24 11:06:55Z vincent $ */

package org.apache.fop.area;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;

public class TraitTestCase {

    @Test
    public void testImageTargetWidthAndHeight() {
        int width = 2911;
        int height = 1911;
        Trait.Background background = new Trait.Background();
        background.setImageTargetWidth(width);
        background.setImageTargetHeight(height);
        assertEquals(width, background.getImageTargetWidth());
        assertEquals(height, background.getImageTargetHeight());
        assertTrue(background.toString().contains(Integer.toString(width)));
        assertTrue(background.toString().contains(Integer.toString(height)));
    }

    @Test
    public void testCaching() {
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        Area area = new Area();
        area.addTrait(1, "v");
        area.completeTraits(userAgent);
        Area area2 = new Area();
        area2.addTrait(1, "x");
        area2.completeTraits(userAgent);
        Area area3 = new Area();
        area3.addTrait(1, "v");
        area3.completeTraits(userAgent);
        Assert.assertNotEquals(area.getTraits(), area2.getTraits());
        Assert.assertSame(area.getTraits(), area3.getTraits());
        assertEquals("v", area.getTraits().get(1));
        assertEquals(1, area.getTraits().size());
        assertEquals("x", area2.getTraits().get(1));
        assertEquals(1, area2.getTraits().size());
    }
}
