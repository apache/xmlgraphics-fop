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

package org.apache.fop.render.pcl.fonts.truetype;

import java.io.IOException;

import org.apache.fop.fonts.truetype.FontFileReader;

public class PCLTTFTable {
    protected FontFileReader reader;

    public PCLTTFTable(FontFileReader reader) {
        this.reader = reader;
    }

    protected void skipShort(FontFileReader reader, int skips)
            throws IOException {
        reader.skip(skips * 2L);
    }

    protected void skipLong(FontFileReader reader, int skips)
            throws IOException {
        reader.skip(skips * 4L);
    }

    protected void skipByte(FontFileReader reader, int skips)
            throws IOException {
        reader.skip(skips);
    }
}
