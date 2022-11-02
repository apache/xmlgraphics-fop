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

package org.apache.fop.accessibility;

import java.util.Locale;

import org.xml.sax.Attributes;

/**
 * This implementation ignores all structure tree events.
 */
public final class DummyStructureTreeEventHandler implements StructureTreeEventHandler {

    /** The singleton instance of this class. */
    public static final StructureTreeEventHandler INSTANCE = new DummyStructureTreeEventHandler();

    private DummyStructureTreeEventHandler() { }

    /** {@inheritDoc} */
    public void startPageSequence(Locale locale, String role) {
    }

    /** {@inheritDoc} */
    public void endPageSequence() {
    }

    /** {@inheritDoc} */
    public StructureTreeElement startNode(String name, Attributes attributes, StructureTreeElement parent) {
        return null;
    }

    /** {@inheritDoc} */
    public void endNode(String name) {
    }

    /** {@inheritDoc} */
    public StructureTreeElement startImageNode(String name, Attributes attributes, StructureTreeElement parent) {
        return null;
    }

    /** {@inheritDoc} */
    public StructureTreeElement startReferencedNode(String name, Attributes attributes, StructureTreeElement parent) {
        return null;
    }

}
