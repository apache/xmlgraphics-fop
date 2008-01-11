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
 * Represents a vertex to be used by {@link DijkstraAlgorithm}. If you want to represent a city,
 * you can do "public class City implements Vertex". The purpose of this interface is to make
 * sure the Vertex implementation implements the Comparable interface so the sorting order is
 * well-defined even when two vertices have the same penalty/distance from an origin point.
 * Therefore, make sure you implement the <code>compareTo(Object)</code> and
 * <code>equals(Object)</code> methods. 
 */
public interface Vertex extends Comparable {

}
