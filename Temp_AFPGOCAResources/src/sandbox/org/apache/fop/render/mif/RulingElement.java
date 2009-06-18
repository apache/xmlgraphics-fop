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

public class RulingElement extends RefElement {

    public RulingElement() {
        super("RulingCatalog");
    }

    public MIFElement lookupElement(Object key) {
        if (key == null) {
            MIFElement rul = new MIFElement("Ruling");
            MIFElement prop = new MIFElement("RulingTag");
            prop.setValue("`Default'");
            rul.addElement(prop);
            prop = new MIFElement("RulingPenWidth");
            prop.setValue("1");
            rul.addElement(prop);
            prop = new MIFElement("RulingPen");
            prop.setValue("0");
            rul.addElement(prop);
            prop = new MIFElement("RulingLines");
            prop.setValue("1");
            rul.addElement(prop);

            addElement(rul);
            rul.finish(true);
            return rul;
        }
        return null;
    }
}

