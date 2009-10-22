package org.apache.fop.afp.modca.triplets;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.util.BinaryUtils;

/**
 * The attribute qualifier triplet is used to specify a qualifier for a document
 * attribute.
 */
public class AttributeQualifierTriplet extends AbstractTriplet {

    private int seqNumber;
    private int levNumber;

    /**
     * Main constructor
     * 
     * @param seqNumber the attribute qualifier sequence number
     * @param levNumber the attribute qualifier level number
     */
    public AttributeQualifierTriplet(int seqNumber, int levNumber) {
        super(ATTRIBUTE_QUALIFIER);
        this.seqNumber = seqNumber;
        this.levNumber = levNumber;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();
        byte[] id = BinaryUtils.convert(seqNumber, 4);
        System.arraycopy(id, 0, data, 2, id.length);
        byte[] level = BinaryUtils.convert(levNumber, 4);
        System.arraycopy(level, 0, data, 6, level.length);
        os.write(data);
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 10;
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "seqNumber=" + seqNumber + ", levNumber=" + levNumber;
    }
}
