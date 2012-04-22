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

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.properties.Property;

/**
 * Class corresponding to the body-start Property Value function. See Sec.
 * 5.10.4 of the XSL-FO spec.
 */
public class BodyStartFunction extends FunctionBase {

    /** {@inheritDoc} */
    public int getRequiredArgsCount() {
        return 1;
    }

    /** {@inheritDoc} */
    public Property eval(Property[] args, PropertyInfo pInfo) throws PropertyException {
        Numeric distance
            = pInfo.getPropertyList()
              .get(Constants.PR_PROVISIONAL_DISTANCE_BETWEEN_STARTS).getNumeric();

        PropertyList pList = pInfo.getPropertyList();
        while (pList != null && !(pList.getFObj() instanceof ListItem)) {
            pList = pList.getParentPropertyList();
        }
        if (pList == null) {
            throw new PropertyException("body-start() called from outside an fo:list-item");
        }

        Numeric startIndent = pList.get(Constants.PR_START_INDENT).getNumeric();

        return (Property) NumericOp.addition(distance, startIndent);
    }

}
