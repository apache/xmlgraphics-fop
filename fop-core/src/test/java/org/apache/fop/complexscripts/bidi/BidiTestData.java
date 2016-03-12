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

/* $Id: License.java 1039179 2010-11-25 21:04:09Z vhennebert $ */

package org.apache.fop.complexscripts.bidi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;


/*
 * !!! THIS IS A GENERATED FILE !!!
 * If updates to the source are needed, then:
 * - apply the necessary modifications to
 *   'src/codegen/unicode/java/org/apache/fop/text/bidi/GenerateBidiTestData.java'
 * - run 'ant codegen-unicode', which will generate a new BidiTestData.java
 *   in 'test/java/org/apache/fop/complexscripts/bidi'
 * - commit BOTH changed files
 */

/** Bidirectional test data. */
public final class BidiTestData {

    private BidiTestData() {
    }

    public static final String TD_PFX = "TD";
    public static final int TD_CNT = 19;

    public static final String LD_PFX = "LD";
    public static final int LD_CNT = 622;

    public static final int NUM_TEST_SEQUENCES = 216357;

    public static int[] readTestData(String prefix, int index) {
        int[] data = null;
        InputStream is = null;
        Class btc = BidiTestData.class;
        String name = btc.getSimpleName() + "$" + prefix + index + ".ser";
        try {
            if ((is = btc.getResourceAsStream(name)) != null) {
                ObjectInputStream ois = new ObjectInputStream(is);
                data = (int[]) ois.readObject();
                ois.close();
            }
        } catch (IOException e) {
            data = null;
        } catch (ClassNotFoundException e) {
            data = null;
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception e) { /* NOP */ }
            }
        }
        return data;
    }
}
