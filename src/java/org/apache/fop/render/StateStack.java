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

/* $Id: $ */

package org.apache.fop.render;

import java.util.Collection;

/**
 * No copy constructor for java.util.Stack so extended and implemented one.
 */
public class StateStack extends java.util.Stack {

    private static final long serialVersionUID = 4897178211223823041L;

    /**
     * Default constructor
     */
    public StateStack() {
        super();
    }

    /**
     * Copy constructor
     *
     * @param c initial contents of stack
     */
    public StateStack(Collection c) {
        elementCount = c.size();
        // 10% for growth
        elementData = new Object[
                      (int)Math.min((elementCount * 110L) / 100, Integer.MAX_VALUE)];
        c.toArray(elementData);
    }
}