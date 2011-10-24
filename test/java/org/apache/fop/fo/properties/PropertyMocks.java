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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;

/**
 * Helper class to create mocks of various kinds of properties.
 */
public final class PropertyMocks {

    private PropertyMocks() { }

    /**
     * Creates and returns a generic mock property returning decent defaults for the
     * {@link Property#getString()}, {@link Property#getEnum()} and
     * {@link Property#getLengthRange()} methods.
     *
     * @return a mock all-purpose property
     */
    public static Property mockGenericProperty() {
        final Property mockGenericProperty = mock(Property.class);
        when(mockGenericProperty.getString()).thenReturn("A non-empty string");
        when(mockGenericProperty.getEnum()).thenReturn(Constants.EN_SPACE);
        LengthRangeProperty lengthRangeProperty = mockLengthRangeProperty();
        when(mockGenericProperty.getLengthRange()).thenReturn(lengthRangeProperty);
        return mockGenericProperty;
    }

    private static LengthRangeProperty mockLengthRangeProperty() {
        final LengthRangeProperty mockLengthRangeProperty = mock(LengthRangeProperty.class);
        final Property optimum = mockOptimumProperty();
        when(mockLengthRangeProperty.getOptimum(any(PercentBaseContext.class)))
                .thenReturn(optimum);
        return mockLengthRangeProperty;
    }

    /**
     * Creates and returns a mock property returning a decent default for the
     * {@link Property#getNumeric()} method.
     *
     * @return a mock number property
     */
    public static Property mockNumberProperty() {
        final Property mockNumberProperty = mock(Property.class);
        final Numeric mockNumeric = mock(Numeric.class);
        when(mockNumberProperty.getNumeric()).thenReturn(mockNumeric);
        return mockNumberProperty;
    }

    private static Property mockOptimumProperty() {
        final Property optimum = mock(Property.class);
        when(optimum.isAuto()).thenReturn(true);
        return optimum;
    }

}
