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

package org.apache.fop.fonts.truetype;

import java.io.IOException;

/**
 * An interface for streaming full True Type tables from a TTF file.
 */
public interface TTFTableOutputStream {

    /**
     * Streams a table defined in byteArray at offset of length bytes.
     * @param byteArray The source of the table to stream from.
     * @param offset The position in byteArray to begin streaming from.
     * @param length The number of bytes to stream.
     * @throws IOException write error.
     */
    void streamTable(byte[] byteArray, int offset, int length) throws IOException;
}
