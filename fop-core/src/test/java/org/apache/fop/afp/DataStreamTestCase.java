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

package org.apache.fop.afp;

import java.awt.Color;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.fonts.CharacterSetBuilder;
import org.apache.fop.afp.modca.InterchangeSet;
import org.apache.fop.afp.modca.InvokeMediumMap;
import org.apache.fop.afp.modca.PageGroup;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.util.CharUtilities;

public class DataStreamTestCase {

    private DataStream ds;
    private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private AFPTextDataInfo textInfo = new AFPTextDataInfo();
    private CharacterSet cs1146;
    private AFPPaintingState paintState = mock(AFPPaintingState.class);

    @Before
    public void setUp() throws Exception {
        when(paintState.getRotation()).thenReturn(90);
        when(paintState.getPoint(50, 50)).thenReturn(new Point(50, 50));
        AFPUnitConverter unitConv = new AFPUnitConverter(paintState);
        when(paintState.getUnitConverter()).thenReturn(unitConv);
        ds = new DataStream(new Factory(), paintState, outStream);
        textInfo.setEncoding("WinAnsiEncoding");
        textInfo.setRotation(0);
        char ch = '\u3000';
        textInfo.setString("Test String" + CharUtilities.NBSPACE + "blah" + ch + "hello" + '\u2000'
                + "end.");
        textInfo.setX(50);
        textInfo.setY(50);
        textInfo.setColor(Color.black);
        CharacterSetBuilder csb = CharacterSetBuilder.getSingleByteInstance();
        cs1146 = csb.build("C0H200B0", "T1V10500", "Cp1146",
                Class.forName("org.apache.fop.fonts.base14.Helvetica").asSubclass(Typeface.class)
                        .getDeclaredConstructor().newInstance(), null);
        ds.startPage(1000, 1000, 0, 300, 300);
    }

    @Test
    public void testCreateText() throws Exception {
        Font font = mock(Font.class);
        ds.createText(textInfo, 100, 300, font, cs1146);
        ds.createShading(10, 10, 300, 300, Color.white);
        ds.createIncludePageOverlay("testings", 10, 10);
        ds.startDocument();
        ds.startPageGroup();
        ds.createInvokeMediumMap("test");
        ds.createIncludePageSegment("test", 10, 10, 300, 300);
        ds.createTagLogicalElement("test", "test", 0);
        PageGroup pg = ds.getCurrentPageGroup();
        InterchangeSet is = ds.getInterchangeSet();
        ds.getResourceGroup(AFPResourceLevel.valueOf(AFPResourceLevel.ResourceType.DOCUMENT.name()));
    }

    @Test
    public void testMediumMapOnPage() throws Exception {
        ds.createInvokeMediumMap("test");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ds.getCurrentPage().writeToStream(bos);
        ByteArrayInputStream data = new ByteArrayInputStream(bos.toByteArray());
        data.skip(21);
        Assert.assertEquals((byte)data.read(), InvokeMediumMap.Type.MAP);
        Assert.assertEquals((byte)data.read(), InvokeMediumMap.Category.MEDIUM_MAP);
    }

    @Test
    public void testMediumMapOnDocument() throws Exception {
        ds = new DataStream(new Factory(), paintState, outStream);
        ds.startDocument();
        ds.createInvokeMediumMap("test");
        ds.endDocument();
        ByteArrayInputStream data = new ByteArrayInputStream(outStream.toByteArray());
        data.skip(21);
        Assert.assertEquals((byte)data.read(), InvokeMediumMap.Type.MAP);
        Assert.assertEquals((byte)data.read(), InvokeMediumMap.Category.MEDIUM_MAP);
    }

    @Test
    public void testMediumMapBeforePageGroupOnDocument() throws Exception {
        ds = new DataStream(new Factory(), paintState, outStream);
        ds.startDocument();
        ds.createInvokeMediumMap("test");
        ds.startPageGroup();
        ds.startPage(1, 1, 0, 1, 1);
        ds.endPage();
        ds.endPageGroup();
        ds.endDocument();
        ByteArrayInputStream data = new ByteArrayInputStream(outStream.toByteArray());
        data.skip(21);
        Assert.assertEquals((byte)data.read(), InvokeMediumMap.Type.MAP);
        Assert.assertEquals((byte)data.read(), InvokeMediumMap.Category.MEDIUM_MAP);
    }
}
