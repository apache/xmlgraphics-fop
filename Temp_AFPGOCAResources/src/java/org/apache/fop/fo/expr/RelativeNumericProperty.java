/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.expr;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.TableColLength;


/**
 * This class represent a node in a property expression tree.
 * It is created when an operation involve relative expression and is used
 * to delay evaluation of the operation until the time where getNumericValue()
 * or getValue() is called.
 */
public class RelativeNumericProperty extends Property implements Length {
    public static final int ADDITION = 1;
    public static final int SUBTRACTION = 2;
    public static final int MULTIPLY = 3;
    public static final int DIVIDE = 4;
    public static final int MODULO = 5;
    public static final int NEGATE = 6;
    public static final int ABS = 7;
    public static final int MAX = 8;
    public static final int MIN = 9;

    // Used in the toString() method, indexed by operation id.
    private static String operations = " +-*/%";

    /**
     * The operation identifier.
     */
    private int operation;
    /**
     * The first (or only) operand.
     */
    private Numeric op1;
    /**
     * The second operand.
     */
    private Numeric op2 = null;
    /**
     * The dimension of the result.
     */
    private int dimension;

    /**
     * Constructor for a two argument operation.
     * @param operation the operation opcode: ADDITION, SUBTRACTION, ...
     * @param op1 the first operand.
     * @param op2 the second operand
     */
    public RelativeNumericProperty(int operation, Numeric op1, Numeric op2) {
        this.operation = operation;
        this.op1 = op1;
        this.op2 = op2;
        // Calculate the dimension. We can do now.
        switch (operation) {
        case MULTIPLY:
            dimension = op1.getDimension() + op2.getDimension();
            break;
        case DIVIDE:
            dimension = op1.getDimension() - op2.getDimension();
            break;
        default:
            dimension = op1.getDimension();
        }
    }

    /**
     * Constructor for a one argument operation.
     * @param operation the operation opcode: NEGATE, ABS
     * @param op the operand.
     */
    public RelativeNumericProperty(int operation, Numeric op) {
        this.operation = operation;
        this.op1 = op;
        this.dimension = op.getDimension();
    }

    /**
     * Return a resolved (calculated) Numeric with the value of the expression.
     * @param context Evaluation context
     * @return the resolved {@link Numeric} corresponding to the value of the expression
     * @throws PropertyException when an exception occur during evaluation.
     */
    private Numeric getResolved(PercentBaseContext context) throws PropertyException {
        switch (operation) {
        case ADDITION:
            return NumericOp.addition2(op1, op2, context);
        case SUBTRACTION:
            return NumericOp.subtraction2(op1, op2, context);
        case MULTIPLY:
            return NumericOp.multiply2(op1, op2, context);
        case DIVIDE:
            return NumericOp.divide2(op1, op2, context);
        case MODULO:
            return NumericOp.modulo2(op1, op2, context);
        case NEGATE:
            return NumericOp.negate2(op1, context);
        case ABS:
            return NumericOp.abs2(op1, context);
        case MAX:
            return NumericOp.max2(op1, op2, context);
        case MIN:
            return NumericOp.min2(op1, op2, context);
        default:
            throw new PropertyException("Unknown expr operation " + operation);
        }
    }

    /**
     * Return the resolved (calculated) value of the expression.
     * {@inheritDoc}
     */
    public double getNumericValue() throws PropertyException {
        return getResolved(null).getNumericValue(null);
    }

    /**
     * {@inheritDoc}
     */
    public double getNumericValue(PercentBaseContext context) throws PropertyException {
        return getResolved(context).getNumericValue(context);
    }

    /**
     * Return the dimension of the expression
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Return false since an expression is only created when there is relative
     * numerics involved.
     */
    public boolean isAbsolute() {
        return false;
    }

    /**
     * Cast this numeric as a Length.
     */
    public Length getLength() {
        if (dimension == 1) {
            return this;
        }
        log.error("Can't create length with dimension " + dimension);
        return null;
    }

    public Numeric getNumeric() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public int getValue() {
        try {
            return (int) getNumericValue();
        } catch (PropertyException exc) {
            log.error(exc);
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getValue(PercentBaseContext context) {
        try {
            return (int) getNumericValue(context);
        } catch (PropertyException exc) {
            log.error(exc);
        }
        return 0;
    }

    /**
     * Return the number of table units which are included in this length
     * specification. This will always be 0 unless the property specification
     * used the proportional-column-width() function (only on table column FOs).
     * <p>
     * If this value is not 0, the actual value of the Length cannot be known
     * without looking at all of the columns in the table to determine the value
     * of a "table-unit".
     *
     * @return The number of table units which are included in this length
     *         specification.
     */
    public double getTableUnits() {
        double tu1 = 0.0, tu2 = 0.0;
        if (op1 instanceof RelativeNumericProperty) {
            tu1 = ((RelativeNumericProperty) op1).getTableUnits();
        } else if (op1 instanceof TableColLength) {
            tu1 = ((TableColLength) op1).getTableUnits();
        }
        if (op2 instanceof RelativeNumericProperty) {
            tu2 = ((RelativeNumericProperty) op2).getTableUnits();
        } else if (op2 instanceof TableColLength) {
            tu2 = ((TableColLength) op2).getTableUnits();
        }
        if (tu1 != 0.0 && tu2 != 0.0) {
            switch (operation) {
            case ADDITION:
                return tu1 + tu2;
            case SUBTRACTION:
                return tu1 - tu2;
            case MULTIPLY:
                return tu1 * tu2;
            case DIVIDE:
                return tu1 / tu2;
            case MODULO:
                return tu1 % tu2;
            case MIN:
                return Math.min(tu1, tu2);
            case MAX:
                return Math.max(tu1, tu2);
            default:
                assert false;
            }
        } else if (tu1 != 0.0) {
            switch (operation) {
            case NEGATE:
                return -tu1;
            case ABS:
                return Math.abs(tu1);
            default:
                return tu1;
            }
        } else if (tu2 != 0.0) {
            return tu2;
        }
        return 0.0;
    }

    /**
     * Return a string represention of the expression. Only used for debugging.
     * @return the string representation.
     */
    public String toString() {
        switch (operation) {
        case ADDITION: case SUBTRACTION:
        case DIVIDE: case MULTIPLY: case MODULO:
            return "(" + op1 + " " + operations.charAt(operation) + op2 + ")";
        case NEGATE:
            return "-" + op1;
        case MAX:
            return "max(" + op1 + ", " + op2 + ")";
        case MIN:
           return "min(" + op1 + ", " + op2 + ")";
        case ABS:
           return "abs(" + op1 + ")";
        }
        return "unknown operation " + operation;
    }
}
