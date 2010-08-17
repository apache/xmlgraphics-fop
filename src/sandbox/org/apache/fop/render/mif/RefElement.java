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

package org.apache.fop.render.mif;

/**
 * Reference MIF Element.
 * This element is a lookup reference set that contains
 * a list of resources used in the MIF Document.
 * When a lookup is performed it will either create a new
 * element or return an existing element that is valid.
 * THe key depends on the type of reference, it should be able
 * to uniquely identify the element.
 */
public class RefElement extends MIFElement {

    /**
     * @param name a name
     * @see org.apache.fop.render.mif.MIFElement#MIFElement(String)
     */
    public RefElement(String name) {
        super(name);
    }

    /** 
     * @param key a key
     * @return an mif element
     */
    public MIFElement lookupElement(Object key) {
        return null;
    }
}

