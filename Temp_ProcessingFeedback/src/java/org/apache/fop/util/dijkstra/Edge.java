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

/**
 * Represents an edge (or direct route between two points) for the {@link DijkstraAlgorithm}.
 * Implement this class to hold the start and end vertex for an edge and implement the
 * <code>getPenalty()</code> method.
 */
public interface Edge {

    /**
     * Returns the start vertex of the edge.
     * @return the start vertex
     */
    Vertex getStart();

    /**
     * Returns the end vertex of the edge.
     * @return the end vertex
     */
    Vertex getEnd();
    
    /**
     * Returns the penalty (or distance) for this edge.
     * @return the penalty value (must be non-negative)
     */
    int getPenalty();
    
}
