package org.apache.xml.fop.layout;

import org.apache.xml.fop.render.Renderer;

abstract public class Box {
    protected Area parent;
    protected AreaTree areaTree;
    abstract public void render(Renderer renderer);
}
