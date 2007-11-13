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
 * Represents a train route with both distance and duration.
 */
public class TrainRoute implements Edge {

    private Mode mode;
    private Vertex start;
    private Vertex end;
    private int distance;
    private int minutes;

    /**
     * Main constructor.
     * @param origin the start city
     * @param dest the destination city
     * @param distance the distance between the two cities
     * @param minutes the duration for the route
     */
    public TrainRoute(Mode mode, City origin, City dest, int distance, int minutes) {
        this.mode = mode;
        this.start = origin;
        this.end = dest;
        this.distance = distance;
        this.minutes = minutes;
    }
    
    /** {@inheritDoc} */
    public int getPenalty() {
        if (mode.isDuration()) {
            return this.minutes;
        } else {
            return this.distance;
        }
    }

    /** {@inheritDoc} */
    public Vertex getEnd() {
        return this.end;
    }

    /** {@inheritDoc} */
    public Vertex getStart() {
        return this.start;
    }

}
