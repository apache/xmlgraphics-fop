package org.apache.fop.fo.flow;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;


public abstract class TableFObj extends FObj {

    private Numeric borderAfterPrecedence;
    private Numeric borderBeforePrecedence;
    private Numeric borderEndPrecedence;
    private Numeric borderStartPrecedence;
        
    protected static class PendingSpan {
        protected int rowsLeft;
        
        public PendingSpan( int rows ) {
            rowsLeft = rows;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("pending span: rowsLeft=").append(rowsLeft);
            return sb.toString();
        }
    }
    
    public TableFObj(FONode parent) {
        super(parent);
    }
    
    public void bind(PropertyList pList) throws FOPException {
        borderAfterPrecedence = pList.get(PR_BORDER_AFTER_PRECEDENCE).getNumeric();
        borderBeforePrecedence = pList.get(PR_BORDER_BEFORE_PRECEDENCE).getNumeric();
        borderEndPrecedence = pList.get(PR_BORDER_END_PRECEDENCE).getNumeric();
        borderStartPrecedence = pList.get(PR_BORDER_START_PRECEDENCE).getNumeric();
    }
    
    /**
     * 
     * @param side  the side for which to return the border precedence
     * @return the "border-precedence" value for the given side
     */
    public Numeric getBorderPrecedence(int side) {
        switch( side ) {
        case CommonBorderPaddingBackground.BEFORE:
            return borderBeforePrecedence;
        case CommonBorderPaddingBackground.AFTER:
            return borderAfterPrecedence;
        case CommonBorderPaddingBackground.START:
            return borderStartPrecedence;
        case CommonBorderPaddingBackground.END:
            return borderEndPrecedence;
        default:
            return null;
        }
    }
    
    protected void setBorderPrecedence(int side, Numeric newPrecedence) {
        switch( side ) {
        case CommonBorderPaddingBackground.BEFORE:
            borderBeforePrecedence = newPrecedence;
        case CommonBorderPaddingBackground.AFTER:
            borderAfterPrecedence = newPrecedence;
        case CommonBorderPaddingBackground.START:
            borderStartPrecedence = newPrecedence;
        case CommonBorderPaddingBackground.END:
            borderEndPrecedence = newPrecedence;
        }
    }
    
    /**
     * Returns the current column index of the given TableFObj
     * (overridden for Table, TableBody, TableRow)
     * 
     * @return the next column number to use
     */
    public int getCurrentColumnIndex() {
        return 0;
    }
    
    /**
     * Sets the current column index of the given TableFObj
     * (overridden for Table, TableBody, TableRow)
     */
    protected void setCurrentColumnIndex(int newIndex) {
        //do nothing by default
    }
    
    /**
     * Checks if a certain column-number is already occupied
     * (overridden for Table, TableBody, TableRow)
     * 
     * @param colNr the column-number to check
     * @return true if column-number is already in use
     */
    protected boolean isColumnNumberUsed(int colNr) {
        return false;
    }
    
    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public abstract CommonBorderPaddingBackground getCommonBorderPaddingBackground();

}
