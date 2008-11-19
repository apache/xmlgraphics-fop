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

package org.apache.fop.render.ps;

/**
 * Constants used for configuring PostScript output.
 */
public interface PSConfigurationConstants {

    /** Controls the behaviour for landscape pages */
    String AUTO_ROTATE_LANDSCAPE = "auto-rotate-landscape";
    /** Controls whether resources are optimized (rather than inlined) */
    String OPTIMIZE_RESOURCES = "optimize-resources";
    /** Determines the PostScript language level to be generated */
    String LANGUAGE_LEVEL = "language-level";
}
