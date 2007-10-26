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

package org.apache.fop.svg;

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Iterator;
import java.util.Map;

/**
 * Utilities for java.text.AttributedCharacterIterator.
 */
public final class ACIUtils {

    private ACIUtils() {
        //This class shouldn't be instantiated.
    }

    /**
     * Dumps the contents of an ACI to System.out. Used for debugging only.
     * @param aci the ACI to dump
     */
    public static void dumpAttrs(AttributedCharacterIterator aci) {
        aci.first();
        Iterator i = aci.getAttributes().entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            if (entry.getValue() != null) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            } 
        }
        int start = aci.getBeginIndex();
        System.out.print("AttrRuns: ");
        while (aci.current() != CharacterIterator.DONE) {
            int end = aci.getRunLimit();
            System.out.print("" + (end - start) + ", ");
            aci.setIndex(end);
            if (start == end) {
                break;
            } 
            start = end;
        }
        System.out.println("");
    }

}
