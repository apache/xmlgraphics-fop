/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.traits.MinOptMax;

/**
 * Utilities for MinOptMax and LengthRangeProperty.
 */
public class MinOptMaxUtil {

    /**
     * Restricts a MinOptMax using the values from a LengthRangeProperty.
     * @param mom MinOptMax to restrict
     * @param lr restricting source
     */
    public static void restrict(MinOptMax mom, LengthRangeProperty lr) {
        if (lr.getEnum() != Constants.EN_AUTO) {
            if (lr.getMinimum().getEnum() != Constants.EN_AUTO) {
                int min = lr.getMinimum().getLength().getValue();
                if (min > mom.min) {
                    mom.min = min;
                    fixAfterMinChanged(mom);
                }
            }
            if (lr.getMaximum().getEnum() != Constants.EN_AUTO) {
                int max = lr.getMaximum().getLength().getValue();
                if (max < mom.max) {
                    mom.max = max;
                    if (mom.max < mom.opt) {
                        mom.opt = mom.max;
                        mom.min = mom.opt;
                    }
                }
            }
            if (lr.getOptimum().getEnum() != Constants.EN_AUTO) {
                int opt = lr.getOptimum().getLength().getValue();
                if (opt > mom.min) {
                    mom.opt = opt;
                    if (mom.opt > mom.max) {
                        mom.max = mom.opt;
                    }
                }
            }
        }
    }

    /**
     * After a calculation on a MinOptMax, this can be called to set opt to
     * a new effective value.
     * @param mom MinOptMax to adjust
     */
    public static void fixAfterMinChanged(MinOptMax mom) {
        if (mom.min > mom.opt) {
            mom.opt = mom.min;
            if (mom.opt > mom.max) {
                mom.max = mom.opt;
            }
        }
    }
    
}
