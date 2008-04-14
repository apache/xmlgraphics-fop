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

package org.apache.fop.util.dijkstra;

import java.util.Iterator;

/**
 * Represents a directory of edges for use by the {@link DijkstraAlgorithm}.
 */
public interface EdgeDirectory {

    /**
     * Returns the penalty between two vertices.
     * @param start the start vertex
     * @param end the end vertex
     * @return the penalty between two vertices, or 0 if no single edge between the two vertices
     *                  exists.
     */
    int getPenalty(Vertex start, Vertex end);

    /**
     * Returns an iterator over all valid destinations for a given vertex.
     * @param origin the origin from which to search for destinations
     * @return the iterator over all valid destinations for a given vertex
     */
    Iterator getDestinations(Vertex origin);

}