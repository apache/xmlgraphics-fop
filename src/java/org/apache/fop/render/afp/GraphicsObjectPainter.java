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
package org.apache.fop.render.afp;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.modca.GraphicsObject;

/**
 * A simple AFP Graphics 2D painter
 */
public class GraphicsObjectPainter {
    /** Static logging instance */
    protected static Log log = LogFactory.getLog(GraphicsObjectPainter.class);

    private AFPGraphics2D graphics2D;
    
    private GraphicsNode root;
    
    /**
     * Default constructor
     */
    public GraphicsObjectPainter() {
    }
    
    /**
     * Returns the graphics 2D
     * 
     * @return the graphics 2D
     */
    public AFPGraphics2D getGraphics2D() {
        return graphics2D;
    }

    /**
     * Sets the graphics 2D
     * 
     * @param graphics the AFP graphics 2D
     */
    public void setGraphics2D(AFPGraphics2D graphics) {
        this.graphics2D = graphics;
    }
    
    /**
     * Sets the graphics node
     * 
     * @param rootNode the graphics root node
     */
    public void setGraphicsNode(GraphicsNode rootNode) {
        this.root = rootNode;
    }

    /**
     * Paints the graphics object
     * 
     * @param graphicsObj the graphics object
     */
    public void paint(GraphicsObject graphicsObj) {
        log.debug("Generating SVG");
        graphics2D.setGraphicsObject(graphicsObj);
        root.paint(graphics2D);
        graphics2D.dispose();
    }
}