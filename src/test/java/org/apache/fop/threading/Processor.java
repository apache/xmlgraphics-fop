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

package org.apache.fop.threading;

import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;

/**
 * Represents a processor.
 */
public interface Processor {

    /**
     * Process a file.
     * @param src the Source for the FO or XML file
     * @param templates a JAXP Templates object for an XSLT transformation or null
     * @param out the OutputStream for the target file
     * @throws Exception if an error occurs
     */
    void process(Source src, Templates templates, OutputStream out)
            throws Exception;

    /**
     * Returns the target file extension for the configured output format.
     * @return the target file extension (for example ".pdf")
     */
    String getTargetFileExtension();
}
