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

package org.apache.fop.datatypes;

import org.apache.fop.fo.FObj;

/**
 * Class to implement a simple lookup context for a single percent base value.
 */
public class SimplePercentBaseContext implements PercentBaseContext {

    private PercentBaseContext parentContext;
    private int lengthBase;
    private int lengthBaseValue;

    /**
     * @param parentContext the context to be used for all percentages other than lengthBase
     * @param lengthBase the particular percentage length base for which this context provides
     *                   a value
     * @param lengthBaseValue the value to be returned for requests to the given lengthBase
     */
    public SimplePercentBaseContext(PercentBaseContext parentContext,
                             int lengthBase,
                             int lengthBaseValue) {
        this.parentContext = parentContext;
        this.lengthBase = lengthBase;
        this.lengthBaseValue = lengthBaseValue;
    }

    /**
     * Returns the value for the given lengthBase.
     * {@inheritDoc}
     */
    public int getBaseLength(int lengthBase, FObj fobj) {
        // if its for us return our value otherwise delegate to parent context
        if (lengthBase == this.lengthBase) {
            return lengthBaseValue;
        } else if (parentContext != null) {
            return parentContext.getBaseLength(lengthBase, fobj);
        }
        return -1;
    }

}
