package org.apache.fop.extensions;

import org.apache.fop.fo.*;
import org.apache.fop.datatypes.IDReferences;

import java.util.*;

/** 
 * Provides support for PDF destinations, which allow external
 * files to link into a particular place within the generated
 * document.
 *
 * @author Stefan Wachter (based on work by Lloyd McKenzie)
 */
public class Destination extends ExtensionObj {
  
    private String _internalDestination;
    private String _destinationName;
    
    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList) {
            return new Destination(parent, propertyList);
        }

    }

    public static FObj.Maker maker() {
        return new Destination.Maker();
    }

    public Destination(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
        _internalDestination = properties.get("internal-destination").getString();
        if (_internalDestination.equals("")) {
            log.warn("fox:destination requires an internal-destination.");
        }
        _destinationName = properties.get("destination-name").getString();
    }

    /**
     * Gets the name under which the destination may be referenced.
     */
    public String getDestinationName() {
        // if no destination name is set then the internal destination is used as
        // destination name
        return !"".equals(_destinationName) ? _destinationName : _internalDestination;
    }

    /**
     * Gets the internal destination. The internal destination must be equal
     * to the value of an id-attribute of some xsl:fo-Element.
     */
    public String getInternalDestination() {
        return _internalDestination;
    }
    
    public String getName() {
        return "fox:destination";
    }
    
}