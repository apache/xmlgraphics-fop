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

/**
 * Class representing a PDF Names object
 */
public class PDFNames extends PDFDictionary {

    /**
     * Create the Names object
     */
    public PDFNames() {
        /* generic creation of PDF object */
        super();
    }

    /**
     * Returns the Dests object
     * @return the Dests object, or null if it's not used
     */
    public PDFDests getDests() {
        return (PDFDests)get("Dests");
    }

    /**
     * Set the Dests object
     * @param dests the Dests object
     */
    public void setDests(PDFDests dests) {
        put("Dests", dests);
    }

}
