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
import java.util.LinkedList;

import junit.framework.TestCase;

/**
 * Tests the Dijkstra algorithm implementation. We're comparing best solutions with focus on
 * time or distance getting from St. Gallen to Lucerne on Switzerland's railroads.
 */
public class DijkstraTestCase extends TestCase {

    private static final boolean DEBUG = false;
    
    private static final City ROMANSHORN = new City("Romanshorn");
    private static final City ST_GALLEN = new City("St. Gallen");
    private static final City WINTERTHUR = new City("Winterthur");
    private static final City ZURICH = new City("Zurich");
    private static final City ZUG = new City("Zug");
    private static final City RAPPERSWIL = new City("Rapperswil");
    private static final City ARTH_GOLDAU = new City("Arth-Goldau");
    private static final City LUCERNE = new City("Lucerne");

    private static final City NOWHERE = new City("nowhere");
    
    private DijkstraAlgorithm algo;
    private DefaultEdgeDirectory edges;
    private Mode mode;
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        edges = new DefaultEdgeDirectory();
        algo = new DijkstraAlgorithm(edges);
        mode = new Mode();
        
        //St.Gallen - Winterthur - Zurich - Zug - Lucerne: 161 km, 2h 01min
        edges.addEdge(new TrainRoute(mode, ST_GALLEN, WINTERTHUR, 61, 39));
        edges.addEdge(new TrainRoute(mode, WINTERTHUR, ZURICH, 31, 31));
        edges.addEdge(new TrainRoute(mode, ZURICH, ZUG, 39, 31));
        edges.addEdge(new TrainRoute(mode, ZUG, LUCERNE, 30, 20));
        
        //St.Gallen - Rapperswil - Arth-Goldau - Lucerne: 158km, 2h 18min
        edges.addEdge(new TrainRoute(mode, ST_GALLEN, RAPPERSWIL, 72, 57));
        edges.addEdge(new TrainRoute(mode, RAPPERSWIL, ARTH_GOLDAU, 55, 48));
        edges.addEdge(new TrainRoute(mode, ARTH_GOLDAU, LUCERNE, 31, 33));
        
        //A detour to make it interesting (St.Gallen - Romanshorn - Winterthur): 89km, 1h 23min
        edges.addEdge(new TrainRoute(mode, ST_GALLEN, ROMANSHORN, 30, 32));
        edges.addEdge(new TrainRoute(mode, ROMANSHORN, WINTERTHUR, 59, 51));
    }

    public void testAlgorithmWithDistance() throws Exception {
        mode.useDistance();
        City origin = ST_GALLEN;
        City destination = LUCERNE;
        String route = executeAlgorithm(origin, destination);
        
        int distance = algo.getLowestPenalty(destination);
        
        if (DEBUG) {
            System.out.println(route + " " + distance + " km");
        }
        
        assertEquals(158, distance);
        assertEquals("St. Gallen - Rapperswil - Arth-Goldau - Lucerne", route);
    }

    public void testAlgorithmWithDuration() throws Exception {
        mode.useDuration();
        City origin = ST_GALLEN;
        City destination = LUCERNE;
        String route = executeAlgorithm(origin, destination);
        
        int duration = algo.getLowestPenalty(destination);
        
        if (DEBUG) {
            System.out.println(route + " " + duration + " minutes");
        }
        
        assertEquals(121, duration);
        assertEquals("St. Gallen - Winterthur - Zurich - Zug - Lucerne", route);
    }
    
    public void testAlgorithmWithNonExistentRoute() throws Exception {
        City origin = ST_GALLEN;
        City destination = NOWHERE;
        algo.execute(origin, destination);
        Vertex pred = algo.getPredecessor(destination);
        assertNull(pred);
    }
    
    private String executeAlgorithm(City origin, City destination) {
        algo.execute(origin, destination);
        Vertex prev = destination;
        Vertex pred = algo.getPredecessor(destination);
        if (pred == null) {
            fail("No route found!");
        }
        LinkedList stops = new LinkedList();
        stops.addLast(destination);
        while ((pred = algo.getPredecessor(prev)) != null) {
            stops.addFirst(pred);
            prev = pred;
        }
        StringBuffer sb = new StringBuffer();
        Iterator iter = stops.iterator();
        while (iter.hasNext()) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(iter.next());
        }
        String route = sb.toString();
        return route;
    }
    
}
