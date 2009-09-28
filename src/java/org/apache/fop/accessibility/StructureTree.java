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

import org.w3c.dom.NodeList;

/**
 * A reduced version of the document's FO tree, containing only its logical
 * structure. Used by accessible output formats.
 */
public interface StructureTree {

    /**
     * Returns the list of nodes that are the children of the given page sequence.
     *
     * @param number number of the page sequence, 1-based
     * @return its children nodes
     */
    NodeList getPageSequence(int number);

}
