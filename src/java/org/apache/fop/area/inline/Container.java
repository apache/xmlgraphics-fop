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

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;

import java.util.List;
import java.util.ArrayList;

/**
 * Container area for inline container.
 * This area should be placed in a viewport as a result of the
 * inline container formatting object.
 * This allows an inline area to have blocks as children.
 */
public class Container extends Area {
    /**
     * The list of block areas stacked inside this container
     */
    protected List blocks = new ArrayList();

    /**
     * The width of this container
     */
    protected int width;

    /**
     * Create a new container area
     */
    public Container() {
    }

    /**
     * Add the block to this area.
     *
     * @param block the block area to add
     */
    public void addBlock(Block block) {
        blocks.add(block);
    }

    /**
     * Get the block areas stacked inside this container area.
     *
     * @return the list of block areas
     */
    public List getBlocks() {
        return blocks;
    }

    /**
     * Get the width of this container area.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }
}

