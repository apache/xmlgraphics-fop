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
 * This base context is used during validation when the actual base values are still unknown
 * but should still already be checked. The actual value returned is not so important in this
 * case. But it's important that zero and non-zero values can be distinguished.
 * <p>
 * Example: A table with collapsing border model has no padding. The Table FO should be able 
 * to check if non-zero values (even percentages) have been specified.
 */
public final class ValidationPercentBaseContext implements PercentBaseContext {
    
    /**
     * Main constructor.
     */
    private ValidationPercentBaseContext() {
    }

    /**
     * Returns the value for the given lengthBase.
     * @see org.apache.fop.datatypes.PercentBaseContext#getBaseLength(int, FObj)
     */
    public int getBaseLength(int lengthBase, FObj fobj) {
        //Simply return a dummy value which produces a non-zero value when a non-zero percentage
        //was specified.
        return 100000;
    }

    private static PercentBaseContext pseudoContextForValidation = null;
    
    /** @return a base context for validation purposes. See class description. */
    public static PercentBaseContext getPseudoContext() {
        if (pseudoContextForValidation == null) {
            pseudoContextForValidation = new ValidationPercentBaseContext();
        }
        return pseudoContextForValidation;
    }
    
}
