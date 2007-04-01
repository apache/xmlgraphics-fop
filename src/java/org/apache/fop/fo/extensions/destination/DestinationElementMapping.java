package org.apache.fop.fo.extensions.destination;

import java.util.HashMap;
import java.util.Set;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.util.QName;

import org.apache.fop.fo.extensions.destination.Destination;

/**
 * Set up the destination element mapping.
 */
public class DestinationElementMapping extends ElementMapping {
    
    /**
    * The FOP extension namespace URI
    */
    public static final String URI = ExtensionElementMapping.URI;

    private static final Set propertyAttributes = new java.util.HashSet();
    
    static {
        //The extension property (fox:*) for named destinations
        propertyAttributes.add("internal-destination");
    }

    /**
    * Constructor.
    */
    public DestinationElementMapping() {
        namespaceURI = URI;
    }

    /**
    * @see org.apache.fop.fo.ElementMapping#initialize()
    */
    protected void initialize() {
        if (foObjs == null) {
            foObjs = new HashMap();
            foObjs.put("destination", new DestinationMaker());
        }
    }

    static class DestinationMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new Destination(parent);
        }
    }

    /**
    * @see org.apache.fop.fo.ElementMapping#getStandardPrefix()
    */
    public String getStandardPrefix() {
        return "fox";
    }
    
    /**
    * @see org.apache.fop.fo.ElementMapping#isAttributeProperty(org.apache.fop.util.QName)
    */
    public boolean isAttributeProperty(QName attributeName) {
        if (!URI.equals(attributeName.getNamespaceURI())) {
            throw new IllegalArgumentException("The namespace URIs don't match");
        }
        return propertyAttributes.contains(attributeName.getLocalName());
    }
    
}
