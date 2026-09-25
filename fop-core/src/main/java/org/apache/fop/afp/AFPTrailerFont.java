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
package org.apache.fop.afp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.fontbox.ttf.TTFTable;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;

import org.apache.fop.afp.modca.ActiveEnvironmentGroup;
import org.apache.fop.afp.modca.ObjectContainer;
import org.apache.fop.afp.modca.ResourceGroup;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.afp.modca.triplets.EncodingTriplet;
import org.apache.fop.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.truetype.CmapWriter;

/**
 * Builds an AFP object container resource holding an embedded TrueType font, written to the
 * document trailer's resource group. The full font is read from the given URI (extracted from a
 * TrueType Collection when a {@code ttc} name is supplied), optionally has a cmap appended, and is
 * wrapped in the MO:DCA structures required to reference it as a TrueType font resource.
 */
public class AFPTrailerFont {
    private AFPResourceAccessor accessor;
    private URI uri;
    private String ttc;
    private String resourceName;
    private ResourceGroup resourceGroup;
    private Factory factory;
    protected CMapSegment[] cmap;

    AFPTrailerFont(AFPResourceAccessor accessor, URI uri, String ttc, CMapSegment[] cmap, String resourceName,
                ResourceGroup resourceGroup, Factory factory) {
        this.accessor = accessor;
        this.uri = uri;
        this.ttc = ttc;
        this.cmap = cmap;
        this.resourceName = resourceName;
        this.resourceGroup = resourceGroup;
        this.factory = factory;
    }

    public void build() throws IOException {
        ResourceObject res = factory.createResource();
        res.setType(ResourceObject.TYPE_OBJECT_CONTAINER);

        ActiveEnvironmentGroup.setupTruetypeMDR(res, false);

        ObjectContainer objectContainer = factory.createObjectContainer();
        InputStream is = accessor.createInputStream(uri);

        byte[] fontData;
        if (ttc != null) {
            fontData = extractTTC(ttc, is);
        } else {
            fontData = IOUtils.toByteArray(is);
        }
        if (cmap != null) {
            fontData = CmapWriter.appendCmap(fontData, cmap);
        }
        objectContainer.setData(fontData);

        ActiveEnvironmentGroup.setupTruetypeMDR(objectContainer, true);

        res.addTriplet(new EncodingTriplet(1200));

        res.setFullyQualifiedName(FullyQualifiedNameTriplet.TYPE_REPLACE_FIRST_GID_NAME,
                FullyQualifiedNameTriplet.FORMAT_CHARSTR, resourceName, true);

        res.setDataObject(objectContainer);
        resourceGroup.addObject(res);
    }

    private byte[] extractTTC(String ttc, InputStream is) throws IOException {
        TrueTypeCollection trueTypeCollection = new TrueTypeCollection(is);
        TrueTypeFont ttf = trueTypeCollection.getFontByName(ttc);
        return extractFullFont(ttf);
    }

    protected static byte[] extractFullFont(TrueTypeFont ttf) throws IOException {
        byte[] source = IOUtils.toByteArray(ttf.getOriginalData());
        List<TTFTable> tables = new ArrayList<>(ttf.getTables());
        Collections.sort(tables, new Comparator<TTFTable>() {
            public int compare(TTFTable table1, TTFTable table2) {
                return table1.getTag().compareTo(table2.getTag());
            }
        });
        int numTables = tables.size();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        out.writeInt(0x00010000); // sfnt version (TrueType outlines)
        out.writeShort(numTables);
        int entrySelector = 0;
        while ((1 << (entrySelector + 1)) <= numTables) {
            entrySelector++;
        }
        int searchRange = (1 << entrySelector) * 16;
        out.writeShort(searchRange);
        out.writeShort(entrySelector);
        out.writeShort(numTables * 16 - searchRange);
        int offset = 12 + numTables * 16;
        for (TTFTable table : tables) {
            out.writeBytes(table.getTag());
            out.writeInt((int) table.getCheckSum());
            out.writeInt(offset);
            out.writeInt((int) table.getLength());
            offset += (int) ((table.getLength() + 3) & ~3L);
        }
        for (TTFTable table : tables) {
            int length = (int) table.getLength();
            out.write(source, (int) table.getOffset(), length);
            while (length % 4 != 0) {
                out.write(0);
                length++;
            }
        }
        out.flush();
        return bos.toByteArray();
    }
}
