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
 * This interface is implemented by classes that can be serialized to a PDF file either by
 * serializing the object or by writing a indirect reference to the actual object.
 */
public interface PDFWritable {
    
    /**
     * Returns a representation of this object for in-object placement, i.e. if the object
     * has an object number its reference is returned. Otherwise, its PDF representation is
     * returned.
     * @return the String representation
     */
    String toInlinePDFString();
    
}
