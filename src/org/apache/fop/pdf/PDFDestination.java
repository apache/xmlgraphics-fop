package org.apache.fop.pdf;

import org.apache.fop.datatypes.IDReferences;

/**
 * This represents a single destination object in a PDF. Destinations allow
 * specific locations within a PDF document to be referenced by other PDF
 * documents (e.g. to link to a location within a document.)
 *
 * @author Stefan Wachter (based on work by Lloyd McKenzie)
 *
 */
public class PDFDestination {

    /**
     * References that are used to resolve the destination.
     */
    private IDReferences _idReferences;
    
    /**
     * the name that is used to reference the destination
     */
    private String _destinationName;

    /**
     * id of the internal destination that is referenced
     */
    private String _internalDestination;

    /**
     * @param idReferences the id nodes container that is used to resolve the
     * internal destination
     * @param destinationName the name under which this destination is referenced
     * @param internalDestination the internal destination that is referenced
     */
    public PDFDestination(IDReferences idReferences, String destinationName, String internalDestination) {
        _idReferences = idReferences;
        _destinationName = destinationName;
        _internalDestination = internalDestination;
    }

    /**
     * Represent the object in PDF. Outputs a key/value pair in a name tree structure.
     * The key is the destination name the value is an array addressing the corresponding
     * page/position.
     */
    protected String toPDF() {
        StringBuffer result = new StringBuffer();
        String destinationRef = _idReferences.getDestinationRef(_internalDestination);
        result.append(" (" + _destinationName + ") ").append(destinationRef);
        return result.toString();
    }

}