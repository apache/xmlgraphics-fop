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

package org.apache.fop.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A factory class for creating checks that belong to a same family.
 * @param <C> a family of checks
 */
public abstract class ChecksFactory<C extends Check> {

    /**
     * A factory to create a particular kind of check.
     */
    protected interface CheckFactory<C> {

        /**
         * Creates a {@link Check} instance from the given XML element.
         *
         * @param element an element representing a check
         * @return the corresponding check
         */
        C createCheck(Element element);
    }

    private final Map<String, CheckFactory<C>> checkFactories
            = new HashMap<String, CheckFactory<C>>();

    /** Default constructor. */
    protected ChecksFactory() { }

    /**
     * Registers a factory for a new kind of check.
     *
     * @param elementName the name of the element under which the check is identified in
     * the XML test case
     * @param factory the corresponding factory
     */
    protected void registerCheckFactory(String elementName, CheckFactory<C> factory) {
        checkFactories.put(elementName, factory);
    }

    /**
     * Creates a new {@link Check} instance corresponding to the given element.
     *
     * @param element an element in the XML test case that identifies a particular check
     * @return the corresponding check
     * @throws IllegalArgumentException if not check corresponding to the given element
     * has been found
     */
    public final C createCheck(Element element) {
        String name = element.getTagName();
        CheckFactory<C> factory = checkFactories.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("No check class found for " + name);
        } else {
            return factory.createCheck(element);
        }
    }

    public final List<C> createCheckList(Element container) {
        List<C> checks = new ArrayList<C>();
        NodeList nodes = container.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                checks.add(createCheck((Element) node));
            }
        }
        return checks;
    }
}
