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
package org.apache.fop.render.rtf;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.properties.FixedLength;
import org.apache.fop.fotreetest.DummyFOEventHandler;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfText;

public class TextAttributesConverterTestCase {
    @Test
    public void test() throws FOPException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        Root root = new Root(null);
        root.setFOEventHandler(new DummyFOEventHandler(ua));
        Block block = new Block(root);
        StaticPropertyList propertyList = new StaticPropertyList(block, null);
        propertyList.putExplicit(Constants.PR_TEXT_INDENT, FixedLength.getInstance(1000));
        block.bind(propertyList);
        RtfAttributes attributes = TextAttributesConverter.convertAttributes(block);
        Assert.assertEquals(attributes.getValue(RtfText.LEFT_INDENT_FIRST), 20);
    }
}
