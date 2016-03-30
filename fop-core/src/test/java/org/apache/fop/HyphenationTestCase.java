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
package org.apache.fop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.hyphenation.Hyphenation;
import org.apache.fop.hyphenation.HyphenationException;
import org.apache.fop.hyphenation.HyphenationTree;
import org.apache.fop.hyphenation.Hyphenator;

public class HyphenationTestCase {

    @Test
    public void testHyphenator() {
        File f = new File("test/resources/fop");
        InternalResourceResolver resourceResolver = ResourceResolverFactory.createDefaultInternalResourceResolver(
                f.toURI());
        Hyphenation hyph = Hyphenator.hyphenate("fr.xml" + Hyphenator.XMLTYPE, null, resourceResolver, null,
                "hello", 0, 0);
        assertEquals(hyph.toString(), "-hel-lo");
    }

    @Test
    public void testHyphenatorBinary() throws HyphenationException, IOException {
        File f = File.createTempFile("hyp", "fop");
        f.delete();
        f.mkdir();
        InternalResourceResolver resourceResolver = ResourceResolverFactory.createDefaultInternalResourceResolver(
                f.toURI());

        HyphenationTree hTree = new HyphenationTree();
        hTree.loadPatterns(new File("test/resources/fop/fr.xml").getAbsolutePath());
        File hyp = new File(f, "fr.hyp");
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(hyp));
        out.writeObject(hTree);
        out.close();

        Hyphenation hyph = Hyphenator.hyphenate("fr.hyp" + Hyphenator.HYPTYPE, null, resourceResolver, null,
                "oello", 0, 0);
        assertEquals(hyph.toString(), "oel-lo");

        hyp.delete();
        f.delete();
    }
}
