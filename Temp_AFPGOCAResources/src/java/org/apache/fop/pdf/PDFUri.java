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
 * class used to create a PDF Uri link
 */
public class PDFUri extends PDFAction {

    private String uri;

    /**
     * create a Uri instance.
     *
     * @param uri the uri to which the link should point
     */
    public PDFUri(String uri) {
        this.uri = uri;
    }

    /**
     * returns the action ncecessary for a uri
     *
     * @return the action to place next to /A within a Link
     */
    public String getAction() {
        return "<< /URI (" + uri + ")\n/S /URI >>";
    }

    /**
     * {@inheritDoc}
     */
    public String toPDFString() {
        throw new UnsupportedOperationException("This method should not be called");
    }

}
