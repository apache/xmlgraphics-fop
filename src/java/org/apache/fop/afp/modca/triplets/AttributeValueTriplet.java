package org.apache.fop.afp.modca.triplets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.fop.afp.AFPConstants;

/**
 * The attribute value triplet is used to specify a value for a document
 * attribute.
 */
public class AttributeValueTriplet extends AbstractTriplet {
    private String attVal;

    /**
     * Main constructor
     * 
     * @param attVal an attribute value
     */
    public AttributeValueTriplet(String attVal) {
        super(ATTRIBUTE_VALUE);
        this.attVal = truncate(attVal, MAX_LENGTH - 4);
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = super.getData();
        data[2] = 0x00; // Reserved
        data[3] = 0x00; // Reserved

        // convert name and value to ebcdic
        byte[] tleByteValue = null;
        try {
            tleByteValue = attVal.getBytes(AFPConstants.EBCIDIC_ENCODING);
        } catch (UnsupportedEncodingException usee) {
            tleByteValue = attVal.getBytes();
            throw new IllegalArgumentException(attVal + " encoding failed");
        }
        System.arraycopy(tleByteValue, 0, data, 4, tleByteValue.length);
        os.write(data);
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 4 + attVal.length();
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return attVal;
    }
}
