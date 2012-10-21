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

package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Null Filter class. The content is just passed through. The class is used to
 * override the default Flate filter for debugging purposes.
 */
public class NullFilter extends PDFFilter {

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public PDFObject getDecodeParms() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public OutputStream applyFilter(OutputStream out) throws IOException {
        return out;
        //No active filtering, NullFilter does nothing
    }

}

