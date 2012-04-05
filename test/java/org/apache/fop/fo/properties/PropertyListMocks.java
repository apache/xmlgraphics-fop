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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A helper class for mocking a property list.
 */
public final class PropertyListMocks {

    private PropertyListMocks() { }

    /**
     * Creates and returns a mock property list returning a generic default for the
     * {@link PropertyList#get(int)} method.
     *
     * @return a mock property list
     */
    public static PropertyList mockPropertyList() {
        try {
            final PropertyList mockPList = mock(PropertyList.class);
            final Property mockGenericProperty = PropertyMocks.mockGenericProperty();
            when(mockPList.get(anyInt())).thenReturn(mockGenericProperty);
            return mockPList;
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overrides with working mock properties the values returned by
     * {@link PropertyList#get(int)} for {@link Constants#PR_COLUMN_NUMBER},
     * {@link Constants#PR_NUMBER_COLUMNS_SPANNED},
     * {@link Constants#PR_NUMBER_ROWS_SPANNED} and {@link Constants#PR_BORDER_COLLAPSE}.
     *
     * @param mockPList a mock property list
     */
    public static void mockTableProperties(PropertyList mockPList) {
        try {
            final Property mockNumberProperty = PropertyMocks.mockNumberProperty();
            when(mockPList.get(Constants.PR_COLUMN_NUMBER)).thenReturn(mockNumberProperty);
            when(mockPList.get(Constants.PR_NUMBER_COLUMNS_SPANNED)).thenReturn(mockNumberProperty);
            when(mockPList.get(Constants.PR_NUMBER_ROWS_SPANNED)).thenReturn(mockNumberProperty);

            final Property borderCollapseProperty = mock(Property.class);
            when(borderCollapseProperty.getEnum()).thenReturn(Constants.EN_SEPARATE);
            when(mockPList.get(Constants.PR_BORDER_COLLAPSE)).thenReturn(borderCollapseProperty);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Overrides with a working mock property the value returned by
     * {@link PropertyList#getBorderPaddingBackgroundProps()}.
     *
     * @param mockPList a mock property list
     */
    public static void mockCommonBorderPaddingBackgroundProps(PropertyList mockPList) {
        try {
            final CommonBorderPaddingBackground mockCommonBorderPaddingBackground
                    = mock(CommonBorderPaddingBackground.class);
            when(mockPList.getBorderPaddingBackgroundProps())
                    .thenReturn(mockCommonBorderPaddingBackground);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        }
    }

}
