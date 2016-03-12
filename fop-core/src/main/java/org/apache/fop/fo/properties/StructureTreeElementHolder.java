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

package org.apache.fop.fo.properties;

import org.apache.fop.accessibility.StructureTreeElement;

/**
 * Implementations of this interface can return the element in the document's
 * structure tree that they resulted into. Used for tagged PDF and other formats
 * that support a structure tree in addition to paged content.
 */
public interface StructureTreeElementHolder {

    /**
     * Returns the element in the document's structure tree that corresponds to this instance.
     *
     * @return a structure tree element
     */
    StructureTreeElement getStructureTreeElement();

}
