/*-- $Id$-- */
/*
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.apps.FOPException;

public class Marker extends FObjMixed {

	private String markerClassName;
	private Area registryArea;
	
	public static class Maker extends FObj.Maker {
		public FObj make(FObj parent, PropertyList propertyList) throws FOPException {
				return new Marker(parent, propertyList);
		}
	}

	public static FObj.Maker maker() {
		return new Marker.Maker();
	}

	public Marker(FObj parent, PropertyList propertyList) {
		super(parent, propertyList);
		this.name = "fo:marker";
		
		// do check to see that 'this' is under fo:flow
		
		this.markerClassName = this.properties.get("marker-class-name").getString();
		
		// check to ensure that no other marker with same parent
		// has this 'marker-class-name' is in addMarker() method
		try {
			parent.addMarker(this);
		} catch (FOPException fopex) {
			MessageHandler.error("marker cannot be added to '" + parent + "'");
		}
	}

	  public Status layout(Area area) throws FOPException {
		// no layout action desired
		this.registryArea = area;
		area.getPage().registerMarker(this);
		// System.out.println("Marker being registered in area '" + area + "'");
		return new Status(Status.OK);
	  }

	public Status layoutMarker(Area area) throws FOPException {
		if (this.marker == START)
			this.marker = 0;
			
		int numChildren = this.children.size();
		for (int i = this.marker; i < numChildren; i++) {
			FONode fo = (FONode) children.elementAt(i);
	
			Status status;
			if ((status = fo.layout(area)).isIncomplete()) {
			this.marker = i;
			return status;
			}
		}
	
		return new Status(Status.OK);
	}

	public String getMarkerClassName() {
		return markerClassName;
	}
	
	public Area getRegistryArea() {
		return registryArea;
	}
}
