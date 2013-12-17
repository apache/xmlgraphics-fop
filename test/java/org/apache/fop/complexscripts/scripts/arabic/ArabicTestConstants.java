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

package org.apache.fop.complexscripts.scripts.arabic;

/**
 * Constants for test functionality related to the arabic script.
 */
public interface ArabicTestConstants {

    String WF_FILE_SCRIPT = "arab";
    String WF_FILE_LANGUAGE = "dflt";

    String SRC_FILES_DIR = "test/resources/complexscripts/arab/data";
    String DAT_FILES_DIR = "test/resources/complexscripts/arab/data";

    String[] SRC_FILES = {
        "arab-001",     // unpointed word forms
    };

    String WF_FILE_SRC_EXT = "txt";
    String WF_FILE_DAT_EXT = "ser";

    String TTX_FONTS_DIR = "test/resources/complexscripts/arab/ttx";

    String[] TTX_FONTS = {
        "arab-001.ttx", // simplified arabic
        "arab-002.ttx", // traditional arabic
        "arab-003.ttx", // lateef
        "arab-004.ttx", // scheherazade
    };

}
