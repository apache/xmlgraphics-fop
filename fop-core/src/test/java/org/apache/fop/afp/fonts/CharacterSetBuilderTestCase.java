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
package org.apache.fop.afp.fonts;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;

public class CharacterSetBuilderTestCase {
    @Test
    public void testCharacterSetBuilder() {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        Assert.assertEquals(fopFactory.newFOUserAgent().getSingleByteCharacterSetBuilder(),
                fopFactory.newFOUserAgent().getSingleByteCharacterSetBuilder());
        Assert.assertEquals(fopFactory.newFOUserAgent().getDoubleByteCharacterSetBuilder(),
                fopFactory.newFOUserAgent().getDoubleByteCharacterSetBuilder());
        FOUserAgent userAgent2 = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        Assert.assertNotEquals(fopFactory.newFOUserAgent().getSingleByteCharacterSetBuilder(),
                userAgent2.getSingleByteCharacterSetBuilder());
        Assert.assertNotEquals(fopFactory.newFOUserAgent().getDoubleByteCharacterSetBuilder(),
                userAgent2.getDoubleByteCharacterSetBuilder());
    }
}
