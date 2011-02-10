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

package org.apache.fop.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

/**
 * @deprecated use org.apache.xmlgraphics.util.uri.DataURLUtil directly
 * @see org.apache.xmlgraphics.util.uri.DataURLUtil
 */
public final class DataURLUtil {

    private DataURLUtil() {
    }

    /**
     * @param in an input stream
     * @param mediatype a MIME media type
     * @return a data url as a string
     * @throws IOException if not caught
     * @deprecated
     * @see org.apache.xmlgraphics.util.uri.DataURLUtil#createDataURL(InputStream,
     *      String)
     */
    public static String createDataURL(InputStream in, String mediatype)
        throws IOException {
        return org.apache.xmlgraphics.util.uri.DataURLUtil.createDataURL(in,
                mediatype);
    }

    /**
     * @param in an input stream
     * @param mediatype a MIME media type
     * @param writer a writer
     * @throws IOException if not caught
     * @deprecated
     * @see org.apache.xmlgraphics.util.uri.DataURLUtil#writeDataURL(InputStream,
     *      String, Writer)
     */
    public static void writeDataURL(InputStream in, String mediatype,
            Writer writer) throws IOException {
        org.apache.xmlgraphics.util.uri.DataURLUtil.writeDataURL(in, mediatype,
                writer);
    }
}
