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
package org.apache.fop.afp.modca;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import org.apache.xmlgraphics.java2d.color.DefaultColorConverter;

import org.apache.fop.afp.Factory;
import org.apache.fop.afp.parser.MODCAParser;

public class GraphicsObjectTestCase {
    @Test
    public void testSetColor() throws IOException {
        GraphicsObject go = new GraphicsObject(new Factory(), null);
        go.setColorConverter(DefaultColorConverter.getInstance());
        go.newSegment();
        go.setColor(Color.white);
        go.newSegment();
        go.setColor(Color.white);
        Assert.assertEquals(go.objects.get(0).getDataLength(), 66);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        go.writeContent(bos);

        MODCAParser modcaParser = new MODCAParser(new ByteArrayInputStream(bos.toByteArray()));
        byte[] field = modcaParser.readNextStructuredField().getData();
        ByteArrayInputStream bis = new ByteArrayInputStream(field);
        bis.skip(55);
        //White data:
        Assert.assertEquals(bis.read(), 255);
        Assert.assertEquals(bis.read(), 255);
        Assert.assertEquals(bis.read(), 255);
    }
}
