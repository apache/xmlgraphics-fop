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

package org.apache.fop.accessibility.fo;

import java.io.File;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Utility class to stream an instance of {@link DOMResult} into a file. May be
 * useful for debugging.
 */
final class DOMResultUtil {

    private DOMResultUtil() {
    }

    /**
     * Streams the given result into a file of the given name.
     *
     * @param result the result of a transformation
     * @param filename name of the file into which to stream the result
     * @throws TransformerException if a problem occurs when streaming
     */
    public static void streamToFile(DOMResult result, String filename) throws TransformerException {
        DOMSource source = new DOMSource(result.getNode());
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.transform(source, new StreamResult(new File(filename)));
    }

}
