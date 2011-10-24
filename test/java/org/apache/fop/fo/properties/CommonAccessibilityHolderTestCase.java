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

package org.apache.fop.fo.properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FONodeMocks;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * This tests that all the FONodes that implement CommonAccessibilityHolder correctly configure
 * the CommonAccessibility property.
 */
public class CommonAccessibilityHolderTestCase {

    private static final List<Class<? extends CommonAccessibilityHolder>> IMPLEMENTATIONS
            = new ArrayList<Class<? extends CommonAccessibilityHolder>>();

    private final String role = "role";

    private final String sourceDocument = "source document";

    static {
        /* This triggers 'unimplemented feature' FO validation events so that the event system is
         * not triggered when testing, avoiding extra convoluted dependency stubbing. */
//        UnimplementedWarningNeutralizer.neutralizeUnimplementedWarning();

        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.BasicLink.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.Block.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.pagination.bookmarks.Bookmark.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.pagination.bookmarks.BookmarkTitle.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.ExternalGraphic.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.Footnote.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.FootnoteBody.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.InitialPropertySet.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.Inline.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.InstreamForeignObject.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.Leader.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.ListBlock.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.ListItem.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.ListItemBody.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.ListItemLabel.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.PageNumber.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.PageNumberCitation.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.PageNumberCitationLast.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.pagination.Root.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.table.Table.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.table.TableAndCaption.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.table.TableBody.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.table.TableCaption.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.table.TableCell.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.table.TableFooter.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.table.TableHeader.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.flow.table.TableRow.class);
        IMPLEMENTATIONS.add(org.apache.fop.fo.pagination.Title.class);
    }

    /**
     * Bind should be overridden to correctly configure the CommonAccessibility property
     * @throws Exception -
     */
    @Test
    public void bindMustSetRoleAndSourceDoc() throws Exception {
        final PropertyList mockPList = mockPropertyList();
        final FONode parent = FONodeMocks.mockFONode();
        for (Class<? extends CommonAccessibilityHolder> clazz : IMPLEMENTATIONS) {
            Constructor<? extends CommonAccessibilityHolder> constructor
                    = clazz.getConstructor(FONode.class);
            CommonAccessibilityHolder sut = constructor.newInstance(parent);
            ((FONode)sut).bind(mockPList);
            String errorMessage = "Test failed for " + clazz + ": ";
            assertEquals(errorMessage, role, sut.getCommonAccessibility().getRole());
            assertEquals(errorMessage, sourceDocument,
                    sut.getCommonAccessibility().getSourceDocument());
        }
    }

    private PropertyList mockPropertyList() throws PropertyException {
        final PropertyList mockPList = PropertyListMocks.mockPropertyList();
        PropertyListMocks.mockTableProperties(mockPList);
        PropertyListMocks.mockCommonBorderPaddingBackgroundProps(mockPList);
        mockRoleProperty(mockPList);
        mockSourceDocProperty(mockPList);
        return mockPList;
    }

    private void mockRoleProperty(PropertyList mockPList) throws PropertyException {
        final Property mockRoleProperty = mock(Property.class);
        when(mockRoleProperty.getString()).thenReturn(role);
        when(mockPList.get(Constants.PR_ROLE)).thenReturn(mockRoleProperty);
    }

    private void mockSourceDocProperty(PropertyList mockPList) throws PropertyException {
        final Property mockSourceDocProperty = mock(Property.class);
        when(mockSourceDocProperty.getString()).thenReturn(sourceDocument);
        when(mockPList.get(Constants.PR_SOURCE_DOCUMENT)).thenReturn(mockSourceDocProperty);
    }

}
