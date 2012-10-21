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

package org.apache.fop.fo.expr;


import org.apache.fop.fo.properties.NumberProperty;
import org.apache.fop.fo.properties.Property;

class RoundFunction extends FunctionBase {

    /** {@inheritDoc} */
    public int getRequiredArgsCount() {
        return 1;
    }

    /** {@inheritDoc} */
    public Property eval(Property[] args, PropertyInfo pInfo) throws PropertyException {
        Number dbl = args[0].getNumber();
        if (dbl == null) {
            throw new PropertyException("Non number operand to round function");
        }
        double n = dbl.doubleValue();
        double r = Math.floor(n + 0.5);
        if (r == 0.0 && n < 0.0) {
            r = -r;    // round(-0.2) returns -0 not 0
        }
        return NumberProperty.getInstance(r);
    }

}

