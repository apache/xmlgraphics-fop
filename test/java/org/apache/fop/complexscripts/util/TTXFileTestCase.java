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

package org.apache.fop.complexscripts.util;

import java.io.File;

import junit.framework.TestCase;

public class TTXFileTestCase extends TestCase {

    private static String ttxFilesRoot = "test/resources/complexscripts";

    private static String[] ttxFiles = {
        "arab/ttx/arab-001.ttx",
        "arab/ttx/arab-002.ttx",
        "arab/ttx/arab-003.ttx",
        "arab/ttx/arab-004.ttx",
    };

    public void testTTXFiles() throws Exception {
        for ( String tfn : ttxFiles ) {
            try {
                TTXFile tf = new TTXFile();
                tf.parse ( ttxFilesRoot + File.separator + tfn );
            } catch ( Exception e ) {
                fail ( e.getMessage() );
            }
        }
    }

}
