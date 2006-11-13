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

import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.TableColLength;

/**
 * Class modelling the proportional-column-width function. See Sec. 5.10.4 of
 * the XSL-FO standard.
 */
public class PPColWidthFunction extends FunctionBase {

    /**
     * @return 1 (the number of arguments for the proportional-column-width
     * function)
     */
    public int nbArgs() {
        return 1;
    }

    /**
     *
     * @param args array of arguments for this function
     * @param pInfo PropertyInfo for this function
     * @return numeric Property containing the units of proportional measure
     * for this column
     * @throws PropertyException for non-numeric operand, or if the parent
     * element is not a table-column
     */
    public Property eval(Property[] args,
                         PropertyInfo pInfo) throws PropertyException {
        Number d = args[0].getNumber();
        if (d == null) {
            throw new PropertyException("Non numeric operand to "
                    + "proportional-column-width() function.");
        }
        PropertyList pList = pInfo.getPropertyList();
        if (!"fo:table-column".equals(pList.getFObj().getName())) {
            throw new PropertyException("proportional-column-width() function "
                    + "may only be used on fo:table-column.");
        }
        
        Table t = (Table) pList.getParentFObj();
        if (t.isAutoLayout()) {
            throw new PropertyException("proportional-column-width() function "
                    + "may only be used when fo:table has "
                    + "table-layout=\"fixed\".");
        }
        return new TableColLength(d.doubleValue(), pInfo.getFO());
    }

}
