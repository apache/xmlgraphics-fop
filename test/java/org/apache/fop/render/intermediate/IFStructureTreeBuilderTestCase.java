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

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.fo.FOElementMapping;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.util.XMLUtil;

public class IFStructureTreeBuilderTestCase {

    private IFStructureTreeBuilder sut;

    @Before
    public void setUp() {
        sut = new IFStructureTreeBuilder();
    }

    @Test
    public void startAndEndPageSequence() throws SAXException {
        final ContentHandler handler = mock(ContentHandler.class);

        try {
            sut.replayEventsForPageSequence(handler, 0);
            fail("No page sequences created");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }

        sut.startPageSequence(null, null);
        sut.endPageSequence();

        sut.replayEventsForPageSequence(handler, 0);

        InOrder inOrder = inOrder(handler);

        inOrder.verify(handler).startPrefixMapping(
                InternalElementMapping.STANDARD_PREFIX, InternalElementMapping.URI);
        inOrder.verify(handler).startPrefixMapping(
                ExtensionElementMapping.STANDARD_PREFIX, ExtensionElementMapping.URI);
        inOrder.verify(handler).startElement(eq(IFConstants.NAMESPACE),
                eq(IFConstants.EL_STRUCTURE_TREE),
                eq(IFConstants.EL_STRUCTURE_TREE),
                any(Attributes.class));
        inOrder.verify(handler).endElement(eq(IFConstants.NAMESPACE),
                eq(IFConstants.EL_STRUCTURE_TREE),
                eq(IFConstants.EL_STRUCTURE_TREE));
        inOrder.verify(handler).endPrefixMapping(ExtensionElementMapping.STANDARD_PREFIX);
        inOrder.verify(handler).endPrefixMapping(InternalElementMapping.STANDARD_PREFIX);
    }

    @Test
    public void startNode() throws Exception {
        final String[] attributes = {"struct-id", "1"};
        final String nodeName = "block";
        final ContentHandler handler = mock(ContentHandler.class);

        sut.startPageSequence(null, null);
        sut.startNode(nodeName, createSimpleAttributes(attributes), null);
        sut.endPageSequence();

        sut.replayEventsForPageSequence(handler, 0);

        verify(handler).startElement(eq(FOElementMapping.URI), eq(nodeName),
                eq(FOElementMapping.STANDARD_PREFIX + ":" + nodeName),
                AttributesMatcher.match(createSimpleAttributes(attributes)));
    }

    @Test
    public void endNode() throws Exception {
        final String nodeName = "block";
        final ContentHandler handler = mock(ContentHandler.class);

        sut.startPageSequence(null, null);
        sut.endNode(nodeName);
        sut.endPageSequence();

        sut.replayEventsForPageSequence(handler, 0);

        verify(handler).endElement(eq(FOElementMapping.URI), eq(nodeName),
                eq(FOElementMapping.STANDARD_PREFIX + ":" + nodeName));
    }

    private static Attributes createSimpleAttributes(String... attributes) {
        assert (attributes.length % 2 == 0);
        final AttributesImpl atts = new AttributesImpl();
        for (int i = 0; i < attributes.length; i += 2) {
            String key = attributes[i];
            String value = attributes[i + 1];
            atts.addAttribute("", key, key, XMLUtil.CDATA, value);
        }
        return atts;
    }

    private static final class AttributesMatcher extends ArgumentMatcher<Attributes> {

        private final Attributes expected;

        private AttributesMatcher(Attributes expected) {
            this.expected = expected;
        }

        public static Attributes match(Attributes expected) {
            return argThat(new AttributesMatcher(expected));
        }

        public boolean matches(Object attributes) {
            return attributesEqual(expected, (Attributes) attributes);
        }

        private static boolean attributesEqual(Attributes attributes1, Attributes attributes2) {
            if (attributes1.getLength() != attributes2.getLength()) {
                return false;
            }
            for (int i = 0; i < attributes1.getLength(); i++) {
                if (attributes1.getLocalName(i) != attributes2.getLocalName(i)) {
                    return false;
                }
                if (attributes1.getQName(i) != attributes2.getQName(i)) {
                    return false;
                }
                if (attributes1.getType(i) != attributes2.getType(i)) {
                    return false;
                }
                if (attributes1.getURI(i) != attributes2.getURI(i)) {
                    return false;
                }
                if (attributes1.getValue(i) != attributes2.getValue(i)) {
                    return false;
                }
            }
            return true;
        }
    }
}
