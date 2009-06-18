/*
 *
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 25/05/2004
 * $Id$
 */
package org.apache.fop.render;

import java.awt.Font;
import java.util.Map;

import org.apache.fop.fonts.FontException;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public interface FontData {
    public Font getFont(Map attributes, int strategy);
    public Font getFont(String family, int style, int variant, int weight,
            int stretch, float size, int strategy) throws FontException;
    public Font getGenericFont(Map attributes);
    public Font getGenericFont(String type, int style, int variant, int weight,
            int stretch, float size) throws FontException;
    public Font getSystemFont(int type) throws FontException;
    public Map makeFontAttributes(String family, int style, int variant,int
            weight, int stretch, float size) throws FontException;
}