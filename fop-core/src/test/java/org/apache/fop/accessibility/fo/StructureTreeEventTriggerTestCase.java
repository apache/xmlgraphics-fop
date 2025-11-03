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

package org.apache.fop.accessibility.fo;

import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.StringProperty;

public class StructureTreeEventTriggerTestCase {

    @Test
    public void testStructureTreeEventTrigger() throws PropertyException {
        PropertyList propertyList = mock(PropertyList.class);
        when(propertyList.get(Constants.PR_SOURCE_DOCUMENT)).thenReturn(StringProperty.getInstance(""));
        when(propertyList.get(Constants.PR_ROLE)).thenReturn(StringProperty.getInstance(""));
        CommonAccessibility mockCommonAccessibility = CommonAccessibility.getInstance(propertyList);

        StructureTreeEventHandler mockHandler = mock(StructureTreeEventHandler.class);
        StructureTreeEventTrigger trigger = new StructureTreeEventTrigger(mockHandler);
        FONode mockParent = mock(FONode.class);
        ExternalDocument mockDoc = mock(ExternalDocument.class);
        when(mockDoc.getCommonAccessibility()).thenReturn(mockCommonAccessibility);
        when(mockDoc.getParent()).thenReturn(mockParent);

        trigger.startExternalDocument(mockDoc);

        verify(mockDoc, times(1)).setStructureTreeElement(any());
        verify(mockHandler, times(1)).startImageNode(any(), any(), any());

    }
}
