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
 * Font Catalog element.
 * This is the reference lookup element for fonts in
 * the MIF document.
 */
public class PGFElement extends RefElement {

    /**
     * Creates a new font catalog element.
     */
    public PGFElement() {
        super("PgfCatalog");
    }

    public MIFElement lookupElement(Object key) {
        if (key == null) {
            MIFElement pgf = new MIFElement("Pgf");
            MIFElement prop = new MIFElement("PgfTag");
            prop.setValue("`Body'");
            pgf.addElement(prop);
            addElement(pgf);
            pgf.finish(true);
            return pgf;
        }
        return null;
    }
}

