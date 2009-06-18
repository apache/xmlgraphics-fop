/*
 * Copyright 1999-2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.properties.ColorTypeProperty;
import org.apache.fop.fo.properties.FixedLength;
import org.apache.fop.fo.properties.ListProperty;
import org.apache.fop.fo.properties.NumberProperty;
import org.apache.fop.fo.properties.PercentLength;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.StringProperty;

import java.util.HashMap;

/**
 * Class to parse XSL-FO property expressions.
 * This class is heavily based on the epxression parser in James Clark's
 * XT, an XSLT processor.
 */
public final class PropertyParser extends PropertyTokenizer {
    private PropertyInfo propInfo;    // Maker and propertyList related info

    private static final String RELUNIT = "em";
    private static final HashMap FUNCTION_TABLE = new HashMap();

    static {
        // Initialize the HashMap of XSL-defined functions
        FUNCTION_TABLE.put("ceiling", new CeilingFunction());
        FUNCTION_TABLE.put("floor", new FloorFunction());
        FUNCTION_TABLE.put("round", new RoundFunction());
        FUNCTION_TABLE.put("min", new MinFunction());
        FUNCTION_TABLE.put("max", new MaxFunction());
        FUNCTION_TABLE.put("abs", new AbsFunction());
        FUNCTION_TABLE.put("rgb", new RGBColorFunction());
        FUNCTION_TABLE.put("system-color", new SystemColorFunction());
        FUNCTION_TABLE.put("from-table-column", new FromTableColumnFunction());
        FUNCTION_TABLE.put("inherited-property-value",
                          new InheritedPropFunction());
        FUNCTION_TABLE.put("from-parent", new FromParentFunction());
        FUNCTION_TABLE.put("from-nearest-specified-value",
                          new NearestSpecPropFunction());
        FUNCTION_TABLE.put("proportional-column-width",
                          new PPColWidthFunction());
        FUNCTION_TABLE.put("label-end", new LabelEndFunction());
        FUNCTION_TABLE.put("body-start", new BodyStartFunction());
        // NOTE: used from code generated for corresponding properties
        FUNCTION_TABLE.put("_fop-property-value", new FopPropValFunction());

        /**
         * * NOT YET IMPLEMENTED!!!
         * FUNCTION_TABLE.put("icc-color", new ICCcolorFunction());
         * FUNCTION_TABLE.put("system-font", new SystemFontFunction());
         *
         * FUNCTION_TABLE.put("merge-property-values", new MergePropsFunction());
         */
    }


    /**
     * Public entrypoint to the Property expression parser.
     * @param expr The specified value (attribute on the xml element).
     * @param propInfo A PropertyInfo object representing the context in
     * which the property expression is to be evaluated.
     * @return A Property object holding the parsed result.
     * @throws PropertyException If the "expr" cannot be parsed as a Property.
     */
    public static Property parse(String expr, PropertyInfo propInfo)
            throws PropertyException {
        try {
            return new PropertyParser(expr, propInfo).parseProperty();
        } catch (PropertyException exc) {
            exc.setPropertyInfo(propInfo);
            throw exc;
        }
    }


    /**
     * Private constructor. Called by the static parse() method.
     * @param propExpr The specified value (attribute on the xml element).
     * @param propInfo A PropertyInfo object representing the context in
     * which the property expression is to be evaluated.
     */
    private PropertyParser(String propExpr, PropertyInfo pInfo) {
        super(propExpr);
        this.propInfo = pInfo;
    }

    /**
     * Parse the property expression described in the instance variables.
     * Note: If the property expression String is empty, a StringProperty
     * object holding an empty String is returned.
     * @return A Property object holding the parsed result.
     * @throws PropertyException If the "expr" cannot be parsed as a Property.
     */
    private Property parseProperty() throws PropertyException {
        next();
        if (currentToken == TOK_EOF) {
            // if prop value is empty string, force to StringProperty
            return new StringProperty("");
        }
        ListProperty propList = null;
        while (true) {
            Property prop = parseAdditiveExpr();
            if (currentToken == TOK_EOF) {
                if (propList != null) {
                    propList.addProperty(prop);
                    return propList;
                } else {
                    return prop;
                }
            } else {
                if (propList == null) {
                    propList = new ListProperty(prop);
                } else {
                    propList.addProperty(prop);
                }
            }
            // throw new PropertyException("unexpected token");
        }
        // return prop;
    }

    /**
     * Try to parse an addition or subtraction expression and return the
     * resulting Property.
     */
    private Property parseAdditiveExpr() throws PropertyException {
        // Evaluate and put result on the operand stack
        Property prop = parseMultiplicativeExpr();
        loop:
        while (true) {
            switch (currentToken) {
            case TOK_PLUS:
                next();
                prop = evalAddition(prop.getNumeric(),
                                    parseMultiplicativeExpr().getNumeric());
                break;
            case TOK_MINUS:
                next();
                prop = evalSubtraction(prop.getNumeric(),
                                    parseMultiplicativeExpr().getNumeric());
                break;
            default:
                break loop;
            }
        }
        return prop;
    }

    /**
     * Try to parse a multiply, divide or modulo expression and return
     * the resulting Property.
     */
    private Property parseMultiplicativeExpr() throws PropertyException {
        Property prop = parseUnaryExpr();
        loop:
        while (true) {
            switch (currentToken) {
            case TOK_DIV:
                next();
                prop = evalDivide(prop.getNumeric(),
                                  parseUnaryExpr().getNumeric());
                break;
            case TOK_MOD:
                next();
                prop = evalModulo(prop.getNumber(),
                                  parseUnaryExpr().getNumber());
                break;
            case TOK_MULTIPLY:
                next();
                prop = evalMultiply(prop.getNumeric(),
                                    parseUnaryExpr().getNumeric());
                break;
            default:
                break loop;
            }
        }
        return prop;
    }

    /**
     * Try to parse a unary minus expression and return the
     * resulting Property.
     */
    private Property parseUnaryExpr() throws PropertyException {
        if (currentToken == TOK_MINUS) {
            next();
            return evalNegate(parseUnaryExpr().getNumeric());
        }
        return parsePrimaryExpr();
    }


    /**
     * Checks that the current token is a right parenthesis
     * and throws an exception if this isn't the case.
     */
    private void expectRpar() throws PropertyException {
        if (currentToken != TOK_RPAR) {
            throw new PropertyException("expected )");
        }
        next();
    }

    /**
     * Try to parse a primary expression and return the
     * resulting Property.
     * A primary expression is either a parenthesized expression or an
     * expression representing a primitive Property datatype, such as a
     * string literal, an NCname, a number or a unit expression, or a
     * function call expression.
     */
    private Property parsePrimaryExpr() throws PropertyException {
        Property prop;
        if (currentToken == TOK_COMMA) {
            //Simply skip commas, for example for font-family
            next();
        }
        switch (currentToken) {
        case TOK_LPAR:
            next();
            prop = parseAdditiveExpr();
            expectRpar();
            return prop;

        case TOK_LITERAL:
            prop = new StringProperty(currentTokenValue);
            break;

        case TOK_NCNAME:
            // Interpret this in context of the property or do it later?
            prop = new NCnameProperty(currentTokenValue);
            break;

        case TOK_FLOAT:
            prop = new NumberProperty(new Double(currentTokenValue));
            break;

        case TOK_INTEGER:
            prop = new NumberProperty(new Integer(currentTokenValue));
            break;

        case TOK_PERCENT:
            /*
             * Get the length base value object from the Maker. If null, then
             * this property can't have % values. Treat it as a real number.
             */
            double pcval = new Double(currentTokenValue.substring(0,
                        currentTokenValue.length() - 1)).doubleValue() / 100.0;
            PercentBase pcBase = this.propInfo.getPercentBase();
            if (pcBase != null) {
                if (pcBase.getDimension() == 0) {
                    prop = new NumberProperty(pcval * pcBase.getBaseValue());
                } else if (pcBase.getDimension() == 1) {
                    prop = new PercentLength(pcval, pcBase);
                } else {
                    throw new PropertyException("Illegal percent dimension value");
                }
            } else {
                // WARNING? Interpret as a decimal fraction, eg. 50% = .5
                prop = new NumberProperty(pcval);
            }
            break;

        case TOK_NUMERIC:
            // A number plus a valid unit name.
            int numLen = currentTokenValue.length() - currentUnitLength;
            String unitPart = currentTokenValue.substring(numLen);
            Double numPart = new Double(currentTokenValue.substring(0,
                    numLen));
            if (unitPart.equals(RELUNIT)) {
                prop = (Property) NumericOp.multiply(new NumberProperty(numPart.doubleValue()),
                                    propInfo.currentFontSize());
            } else {
                prop = new FixedLength(numPart.doubleValue(), unitPart);
            }
            break;

        case TOK_COLORSPEC:
            prop = new ColorTypeProperty(currentTokenValue);
            break;

        case TOK_FUNCTION_LPAR:
            Function function = (Function)FUNCTION_TABLE.get(currentTokenValue);
            if (function == null) {
                throw new PropertyException("no such function: "
                                            + currentTokenValue);
            }
            next();
            // Push new function (for function context: getPercentBase())
            propInfo.pushFunction(function);
            prop = function.eval(parseArgs(function), propInfo);
            propInfo.popFunction();
            return prop;
        
        default:
            // TODO: add the token or the expr to the error message.
            throw new PropertyException("syntax error");
        }
        next();
        return prop;
    }

    /**
     * Parse a comma separated list of function arguments. Each argument
     * may itself be an expression. This method consumes the closing right
     * parenthesis of the argument list.
     * @param function The function object for which the arguments are 
     * collected.
     * @return An array of Property objects representing the arguments
     * found.
     * @throws PropertyException If the number of arguments found isn't equal
     * to the number expected.
     */
    Property[] parseArgs(Function function) throws PropertyException {
        int nbArgs = function.nbArgs();
        Property[] args = new Property[nbArgs];
        Property prop;
        int i = 0;
        if (currentToken == TOK_RPAR) {
            // No args: func()
            next();
        } else {
            while (true) {

                prop = parseAdditiveExpr();
                if (i < nbArgs) {
                    args[i++] = prop;
                }
                // ignore extra args
                if (currentToken != TOK_COMMA) {
                    break;
                }
                next();
            }
            expectRpar();
        }
        if (i == nbArgs - 1 && function.padArgsWithPropertyName()) {
            args[i++] = new StringProperty(propInfo.getPropertyMaker().getName());
        }
        if (nbArgs != i) {
            throw new PropertyException("Expected " + nbArgs
                                        + ", but got " + i + " args for function");
        }
        return args;
    }


    /**
     * Evaluate an addition operation. If either of the arguments is null,
     * this means that it wasn't convertible to a Numeric value.
     * @param op1 A Numeric object (Number or Length-type object)
     * @param op2 A Numeric object (Number or Length-type object)
     * @return A new NumericProperty object holding an object which represents
     * the sum of the two operands.
     * @throws PropertyException If either operand is null.
     */
    private Property evalAddition(Numeric op1,
                                  Numeric op2) throws PropertyException {
        if (op1 == null || op2 == null) {
            throw new PropertyException("Non numeric operand in addition");
        }
        return (Property) NumericOp.addition(op1, op2);
    }

    /**
     * Evaluate a subtraction operation. If either of the arguments is null,
     * this means that it wasn't convertible to a Numeric value.
     * @param op1 A Numeric object (Number or Length-type object)
     * @param op2 A Numeric object (Number or Length-type object)
     * @return A new NumericProperty object holding an object which represents
     * the difference of the two operands.
     * @throws PropertyException If either operand is null.
     */
    private Property evalSubtraction(Numeric op1,
                                     Numeric op2) throws PropertyException {
        if (op1 == null || op2 == null) {
            throw new PropertyException("Non numeric operand in subtraction");
        }
        return (Property) NumericOp.subtraction(op1, op2);
    }

    /**
     * Evaluate a unary minus operation. If the argument is null,
     * this means that it wasn't convertible to a Numeric value.
     * @param op A Numeric object (Number or Length-type object)
     * @return A new NumericProperty object holding an object which represents
     * the negative of the operand (multiplication by *1).
     * @throws PropertyException If the operand is null.
     */
    private Property evalNegate(Numeric op) throws PropertyException {
        if (op == null) {
            throw new PropertyException("Non numeric operand to unary minus");
        }
        return (Property) NumericOp.negate(op);
    }

    /**
     * Evaluate a multiplication operation. If either of the arguments is null,
     * this means that it wasn't convertible to a Numeric value.
     * @param op1 A Numeric object (Number or Length-type object)
     * @param op2 A Numeric object (Number or Length-type object)
     * @return A new NumericProperty object holding an object which represents
     * the product of the two operands.
     * @throws PropertyException If either operand is null.
     */
    private Property evalMultiply(Numeric op1,
                                  Numeric op2) throws PropertyException {
        if (op1 == null || op2 == null) {
            throw new PropertyException("Non numeric operand in multiplication");
        }
        return (Property) NumericOp.multiply(op1, op2);
    }


    /**
     * Evaluate a division operation. If either of the arguments is null,
     * this means that it wasn't convertible to a Numeric value.
     * @param op1 A Numeric object (Number or Length-type object)
     * @param op2 A Numeric object (Number or Length-type object)
     * @return A new NumericProperty object holding an object which represents
     * op1 divided by op2.
     * @throws PropertyException If either operand is null.
     */
    private Property evalDivide(Numeric op1,
                                Numeric op2) throws PropertyException {
        if (op1 == null || op2 == null) {
            throw new PropertyException("Non numeric operand in division");
        }
        return (Property) NumericOp.divide(op1, op2);
    }

    /**
     * Evaluate a modulo operation. If either of the arguments is null,
     * this means that it wasn't convertible to a Number value.
     * @param op1 A Number object
     * @param op2 A Number object
     * @return A new NumberProperty object holding an object which represents
     * op1 mod op2.
     * @throws PropertyException If either operand is null.
     */
    private Property evalModulo(Number op1,
                                Number op2) throws PropertyException {
        if (op1 == null || op2 == null) {
            throw new PropertyException("Non number operand to modulo");
        }
        return new NumberProperty(op1.doubleValue() % op2.doubleValue());
    }

}
