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


/**
 * A collection of constants for XML handling.
 */
public interface XMLConstants {

    /** "CDATA" constant */
    String CDATA = "CDATA";

    /** XML namespace prefix */
    String XML_PREFIX = "xml";
    /** XML namespace URI */
    String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    /** XMLNS namespace prefix */
    String XMLNS_PREFIX = "xmlns";
    /** XMLNS namespace URI */
    String XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

    /** Namespace prefix for XLink */
    String XLINK_PREFIX = "xlink";
    /** XML namespace for XLink */
    String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";
    /** xlink:href attribute */
    org.apache.xmlgraphics.util.QName XLINK_HREF = new org.apache.xmlgraphics.util.QName(
            XLINK_NAMESPACE, XLINK_PREFIX, "href");

}
