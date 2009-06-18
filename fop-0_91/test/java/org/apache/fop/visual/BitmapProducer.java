/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.visual;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Interface for a converter.
 */
public interface BitmapProducer {

    /**
     * Produces a BufferedImage from the source file by invoking the FO processor and
     * converting the generated output file to a bitmap image if necessary.
     * @param src the source FO or XML file
     * @param context context information for the conversion
     * @return the generated BufferedImage
     */
    BufferedImage produce(File src, ProducerContext context);
    
}
