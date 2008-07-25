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

package org.apache.fop.layoutengine;

import java.util.List;

import org.apache.fop.layoutmgr.ElementListObserver.Observer;

/**
 * This class collects element list generated during a FOP processing run. These lists are later
 * used to perform automated checks.
 */
public class ElementListCollector implements Observer {

    private List elementLists = new java.util.ArrayList();

    /**
     * Resets the collector.
     */
    public void reset() {
        elementLists.clear();
    }

    /**
     * @return the list of ElementList instances.
     */
    public List getElementLists() {
        return this.elementLists;
    }

    /** @see org.apache.fop.layoutmgr.ElementListObserver.Observer */
    public void observe(List elementList, String category, String id) {
        elementLists.add(new ElementList(elementList, category, id));
    }

    /**
     * Data object representing an element list along with additional information.
     */
    public static class ElementList {

        private List elementList;
        private String category;
        private String id;

        /**
         * Creates a new ElementList instance
         * @param elementList the element list
         * @param category the category for the element list
         * @param id an optional ID
         */
        public ElementList(List elementList, String category, String id) {
            this.elementList = elementList;
            this.category = category;
            this.id = id;
        }

        /** @return the element list */
        public List getElementList() {
            return elementList;
        }

        /** @return the category */
        public String getCategory() {
            return category;
        }

        /** @return the ID, may be null */
        public String getID() {
            return id;
        }
    }

}