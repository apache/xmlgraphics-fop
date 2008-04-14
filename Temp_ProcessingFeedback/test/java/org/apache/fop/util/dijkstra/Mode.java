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
 * Class to allow easy switching between duration and distance mode.
 */
public class Mode {

    private boolean duration = true;
    
    /**
     * Switch to duration mode.
     */
    public void useDuration() {
        this.duration = true;
    }
    
    /**
     * Switch to distance mode.
     */
    public void useDistance() {
        this.duration = false;
    }
    
    /**
     * Indicates whether to use duration mode or distance mode.
     * @return true if duration mode is active, otherwise it's the distance mode.
     */
    public boolean isDuration() {
        return this.duration;
    }
    
}
