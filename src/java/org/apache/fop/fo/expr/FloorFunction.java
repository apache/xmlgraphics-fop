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


class FloorFunction extends FunctionBase {

    public int nbArgs() {
        return 1;
    }

    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        Number dbl = args[0].getNumber();
        if (dbl == null) {
            throw new PropertyException("Non number operand to floor function");
        }
        return NumberProperty.getInstance(Math.floor(dbl.doubleValue()));
    }

}

