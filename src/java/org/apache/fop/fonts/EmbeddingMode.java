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

package org.apache.fop.fonts;

/**
 * This enumerates the embedding mode of fonts; full; subset; auto (auto defaults to full for
 * type1 fonts and subset for truetype fonts.
 */
public enum EmbeddingMode {
    /** Default option: assumes FULL for type1 fonts and SUBSET for truetype fonts. */
    AUTO,
    /** Full font embedding: This means the whole of the font is written to file. */
    FULL,
    /** Subset font embedding: Only the mandatory tables and a subset of glyphs are written
     * to file.*/
    SUBSET;

    /**
     * Returns the name of this embedding mode.
     * @return String - lower case.
     */
    public String getName() {
        return this.toString().toLowerCase();
    }

    /**
     * Returns {@link EmbeddingMode} by name.
     * @param value String - the name of the embedding mode (not case sensitive).
     * @return embedding mode constant.
     */
    public static EmbeddingMode getValue(String value) {
        for (EmbeddingMode mode : EmbeddingMode.values()) {
            if (mode.toString().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid embedding-mode: " + value);
    }
}
