package org.apache.xml.fop.layout;

import org.apache.xml.fop.render.Renderer;

public class DisplaySpace extends Space {
		private int size;
		
		public DisplaySpace(int size) {
			this.size = size;
	}
	public int getSize() {
		return size;
	}

    public void render(Renderer renderer) {
	renderer.renderDisplaySpace(this);
    }
}
