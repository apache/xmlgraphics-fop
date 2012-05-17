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

/* $Id: CommandLineOptions.java 1293736 2012-02-26 02:29:01Z gadams $ */

package org.apache.fop.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.fop.apps.FOPException;
import org.junit.Before;
import org.junit.Test;

public class CommandLineOptionsTestCase {

    private final CommandLineOptions clo = new CommandLineOptions();
    private final String commandLine = "-fo examples/fo/basic/simple.fo -print";
    private String[] cmd;
    private boolean parsed;

    @Before
    public void setUp() throws Exception {
        cmd = commandLine.split(" ");
        parsed = clo.parse(cmd);
    }

    @Test
    public void testParse() {
        assertTrue(parsed);
    }

    @Test
    public void testGetOutputFormat() throws FOPException {
        assertEquals(clo.getOutputFormat(), "application/X-fop-print");
    }

}
