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

package org.apache.fop.apps;

import org.apache.fop.apps.FopConfBuilder.RendererConfBuilder;

import static org.apache.fop.render.java2d.Java2DRendererOptions.JAVA2D_TRANSPARENT_PAGE_BACKGROUND;

public class Java2DRendererConfBuilder extends RendererConfBuilder {

    protected Java2DRendererConfBuilder() {
        super("Java2D");
    }

    public Java2DRendererConfBuilder setPageBackgroundTransparency(boolean value) {
        createTextElement(JAVA2D_TRANSPARENT_PAGE_BACKGROUND, String.valueOf(value));
        return this;
    }
}
