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
 
package org.apache.fop.traits;

import org.apache.fop.datatypes.Length;

/**
 * Store all block-level layout properties on an FO.
 * Public "structure" allows direct member access.
 */
public class BlockProps {
    
    public Length firstIndent; // text-indent
    public int lastIndent; // last-line-indent
    public int textAlign;
    public int textAlignLast;
    public int lineStackType; // line-stacking-strategy (enum)

}
