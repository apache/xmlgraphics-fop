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

package org.apache.fop.apps;

import java.net.URI;

import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.apache.xmlgraphics.io.ResourceResolver;

public class EnvironmentalProfileFactoryTestCase {

    private final URI testURI = URI.create("this.is.purely.for.test.purposes");

    @Test
    public void testCreateDefault() {
        ResourceResolver mockedResolver = mock(ResourceResolver.class);
        EnvironmentProfile sut = EnvironmentalProfileFactory.createDefault(testURI, mockedResolver);
        assertEquals(mockedResolver, sut.getResourceResolver());
    }
}
