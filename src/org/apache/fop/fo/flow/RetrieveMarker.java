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

// Java
import java.util.Vector;

public class RetrieveMarker extends FObjMixed {

	private String retrieveClassName;
	private int retrievePosition;
	private int retrieveBoundary;
	
	public static class Maker extends FObj.Maker {
		public FObj make(FObj parent, PropertyList propertyList) throws FOPException {
				return new RetrieveMarker(parent, propertyList);
		}
	}

	public static FObj.Maker maker() {
		return new RetrieveMarker.Maker();
	}

	public RetrieveMarker(FObj parent, PropertyList propertyList) {
		super(parent, propertyList);
		this.name = "fo:retrieve-marker";
	
		this.retrieveClassName = this.properties.get("retrieve-class-name").getString();
		this.retrievePosition = this.properties.get("retrieve-position").getEnum();
		this.retrieveBoundary = this.properties.get("retrieve-boundary").getEnum();
	}

	public Status layout(Area area) throws FOPException {
		// locate qualifying areas by 'marker-class-name' and
		// 'retrieve-boundary'. Initially we will always check
		// the containing page
		Page containingPage = area.getPage();
		Marker bestMarker = searchPage(containingPage);
		
		// if marker not yet found, and 'retrieve-boundary' permits,
		// search forward by Page
		/* insert code for searching forward by Page, if allowed */
		
		Status status = new Status(Status.AREA_FULL_NONE);
		if (null != bestMarker) {
			// System.out.println("Laying out marker in area '" + area + "'");
			status = bestMarker.layoutMarker(area);
		}
		
		return status;
	}
	
	private Marker searchPage(Page page) throws FOPException {
		Vector pageMarkers = page.getMarkers();
		if (pageMarkers.isEmpty())
			return null;
			
		// search forward if 'first-starting-within-page' or
		// 'first-including-carryover'
		Marker fallback = null;
		if (retrievePosition == RetrievePosition.FIC) {
			for (int c = 0; c < pageMarkers.size(); c++) {
				Marker currentMarker = (Marker)pageMarkers.elementAt(c);
				if (currentMarker.getMarkerClassName().equals(retrieveClassName))
					return currentMarker;
			}
			
		} else if (retrievePosition == RetrievePosition.FSWP) {
			for (int c = 0; c < pageMarkers.size(); c++) {
				Marker currentMarker = (Marker)pageMarkers.elementAt(c);
				if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
					if (currentMarker.getRegistryArea().isFirst)
						return currentMarker;
					else if (null == fallback)
						fallback = currentMarker;
				}
			}
			
		} else if (retrievePosition == RetrievePosition.LSWP) {
			for (int c = pageMarkers.size(); c > 0; c--) {
				Marker currentMarker = (Marker)pageMarkers.elementAt(c-1);
				if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
					if (currentMarker.getRegistryArea().isFirst)
						return currentMarker;
					else if (null == fallback)
						fallback = currentMarker;
				}
			}	
			
		} else if (retrievePosition == RetrievePosition.LEWP) {
			for (int c = pageMarkers.size(); c > 0; c--) {
				Marker currentMarker = (Marker)pageMarkers.elementAt(c-1);
				if (currentMarker.getMarkerClassName().equals(retrieveClassName)) {
					if (currentMarker.getRegistryArea().isLast)
						return currentMarker;
					else if (null == fallback)
						fallback = currentMarker;
				}
			}	
			
		} else {
			throw new FOPException("Illegal 'retrieve-position' value");
		}
		return fallback;
	}
}
