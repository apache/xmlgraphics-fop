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
import org.apache.fop.fonts.truetype.OFTableName;

public final class PCLTTFTableFactory {
    private FontFileReader reader;

    private PCLTTFTableFactory(FontFileReader reader) {
        this.reader = reader;
    }

    public static PCLTTFTableFactory getInstance(FontFileReader reader) {
        return new PCLTTFTableFactory(reader);
    }

    public PCLTTFTable newInstance(OFTableName tableName)
            throws IOException {
        if (tableName == OFTableName.PCLT) {
            return new PCLTTFPCLTFontTable(reader);
        } else if (tableName == OFTableName.OS2) {
            return new PCLTTFOS2FontTable(reader);
        } else if (tableName == OFTableName.POST) {
            return new PCLTTFPOSTFontTable(reader);
        }
        return null;
    }
}
