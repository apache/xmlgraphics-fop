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

package org.apache.fop.cli;

import java.io.File;
import java.io.StringReader;
import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * InputHandler for the images (for example TIFF) as input.
 */
public class ImageInputHandler extends InputHandler {

    /**
     * Main constructor.
     * @param imagefile the image file
     * @param xsltfile XSLT file (may be null in which case the default stylesheet is used)
     * @param params Vector of command-line parameters (name, value,
     *      name, value, ...) for XSL stylesheet, null if none
     */
    public ImageInputHandler(File imagefile, File xsltfile, Vector params) {
        super(imagefile, xsltfile, params);
    }

    /** {@inheritDoc} */
    protected Source createMainSource() {
        return new StreamSource(new StringReader(
                "<image>" + this.sourcefile.toURI().toASCIIString() + "</image>"));
    }

    /** {@inheritDoc} */
    protected Source createXSLTSource() {
        Source src = super.createXSLTSource();
        if (src == null) {
            src = new StreamSource(getClass().getResource("image2fo.xsl").toExternalForm());
        }
        return src;
    }

}
