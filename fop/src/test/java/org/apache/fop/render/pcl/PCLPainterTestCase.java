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
package org.apache.fop.render.pcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.transform.stream.StreamResult;

import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;

import junit.framework.Assert;

public class PCLPainterTestCase {
    private FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();

    @Test
    public void testFillRect() throws IFException {
        Rectangle size = new Rectangle(1, 1);
        PCLPageDefinition pclPageDef = new PCLPageDefinition("", 0, new Dimension(), size, true);
        PCLDocumentHandler documentHandler = new PCLDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(output));
        documentHandler.startDocument();
        PCLPainter pclPainter = new PCLPainter(documentHandler, pclPageDef);
        pclPainter.fillRect(size, Color.RED);
        Assert.assertTrue(output.toString().contains("*c4Q\u001B*c0.01h0.01V\u001B*c32G\u001B*c4P"));
        output.reset();

        pclPainter.getPCLUtil().setColorEnabled(true);
        pclPainter.fillRect(size, Color.RED);
        Assert.assertFalse(output.toString().contains("*c4P"));
        Assert.assertTrue(output.toString().contains("*v255a0b0c0I\u001B*v0S\u001B*c0.01h0.01V\u001B*c0P"));
    }

}
