/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.fop.traits;

import org.apache.fop.area.Trait;
import org.apache.fop.fo.Constants;

import junit.framework.TestCase;

/**
 * Tests the BorderProps class.
 */
public class BorderPropsTestCase extends TestCase {

    /**
     * Test serialization and deserialization to/from String.
     * @throws Exception if an error occurs
     */
    public void testSerialization() throws Exception {
        Trait.Color col = new Trait.Color(1.0f, 1.0f, 0.5f, 1.0f);
        //Normalize: Avoid false alarms due to color conversion (rounding)
        col = Trait.Color.valueOf(col.toString());
        
        BorderProps b1 = new BorderProps(Constants.EN_DOUBLE, 1250, 
                col, BorderProps.COLLAPSE_OUTER);
        String ser = b1.toString();
        BorderProps b2 = BorderProps.valueOf(ser);
        assertEquals(b1, b2);

        b1 = new BorderProps(Constants.EN_INSET, 9999, 
                col, BorderProps.SEPARATE);
        ser = b1.toString();
        b2 = BorderProps.valueOf(ser);
        assertEquals(b1, b2);
    }
    
}
