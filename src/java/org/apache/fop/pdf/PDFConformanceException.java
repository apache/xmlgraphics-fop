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
 * RuntimeException descendant indicating a conformance problem during PDF generation. This
 * exception will be throws, for example, when PDF/A-1 Level B conformance is activated but the
 * PDF version produced is not PDF 1.4 as mandated by ISO 19005-1:2005(E).
 */
public class PDFConformanceException extends RuntimeException {

    /**
     * Constructs an PDFConformanceException with no detail message.
     * A detail message is a String that describes this particular exception.
     */
    public PDFConformanceException() {
        super();
    }

    /**
     * Constructs an PDFConformanceException with the specified detail
     * message. A detail message is a String that describes this particular
     * exception.
     * @param message the String that contains a detailed message
     */
    public PDFConformanceException(String message) {
        super(message);
    }

}
