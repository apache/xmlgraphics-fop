/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.expr;

import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropNames;

import org.apache.fop.fo.expr.PropertyValue;
import org.apache.fop.fo.expr.PropertyValueList;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Literal;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.Percentage;
import org.apache.fop.datatypes.Ems;
import org.apache.fop.datatypes.IntegerType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Time;
import org.apache.fop.datatypes.Frequency;
import org.apache.fop.datatypes.Angle;
import org.apache.fop.datatypes.Bool;
import org.apache.fop.datatypes.Inherit;
import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.None;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.StringType;
import org.apache.fop.datatypes.MimeType;
import org.apache.fop.datatypes.UriType;
import org.apache.fop.datatypes.FromParent;
import org.apache.fop.datatypes.FromNearestSpecified;
//import org.apache.fop.datatypes.*;

import java.util.HashMap;

/**
 * Class to parse XSL FO property expression.
 * This class is heavily based on the expression parser in James Clark's
 * XT, an XSLT processor.
 *
 * PropertyParser objects are re-usable.  The constructor simply creates the
 * object.  To parse an expression, the public method <i>Parse</i> is
 * called.
 */
public class PropertyParser extends PropertyTokenizer {

    /**
     * This is an attempt to ensure that the restriction on the application of
     * from-parent() and from-nearest-specified-value() functions to shorthand
     * properties (Section 5.10.4 Property Value Functions) is maintained.
     * I.e. if a shorthand property is the subject of one of these functions,
     * ist is valid only if "...the expression only consists of the
     * [from-parent or from-nearest-specified-value] function with an argument
     * matching the property being computed..."
     */
    //private int elementsSeen = 0;
    /**
     * Used in conjunction with <i>elementsSeen</i>.
     */
    //private String restrictedValueFunctSeen = null;

    public PropertyParser() {
        super();
    }

    /**
     * Parse the property expression described in the instance variables.
     * <p>
     * Note: If the property expression String is empty, a StringProperty
     * object holding an empty String is returned.
     * @param property an <tt>int</tt> containing the property index.
     * which the property expression is to be evaluated.
     * @param expr The specified value (attribute on the xml element).
     * @return A PropertyValue holding the parsed result.
     * @throws PropertyException If the "expr" cannot be parsed as a
     * PropertyValue.
     */
    public PropertyValue parse(int property, String expr)
        throws PropertyException
    {
        synchronized (this) {
            // make sure this parser is available
            if (expr != null) // the parser is currently active
                throw new PropertyException
                        ("PropertyParser is currently active.");
            initialize(property, expr);
        }

        next();
        if (currentToken == EOF)
            // prop value is empty
            throw new PropertyException
                    ("No token recognized in :" + expr + ":");

        PropertyValueList propList = new PropertyValueList(property);
        while (true) {
            PropertyValue prop = parseAdditiveExpr();
            if (currentToken == EOF) {
                // end of the expression - add to list and go
                if (propList.size() != 0) {
                    propList.add(prop);
                    return propList;
                } else { // list is empty
                    return prop;
                }
            }
            // throw away commas separating arguments.  These can occur
            // in font-family and voice-family.  Commas are regarded here
            // as separators of list and sublist elements.
            // See 7.16.5 "text-shadow" in the 1.0 Recommendation for an
            // example of sublists.
            if (currentToken == COMMA) {
                next();
                propList.add(prop);
            } else { // whitespace separates list elements; make a sublist
                propList.add(parseSublist(prop));
                if (currentToken == EOF)
                    return propList;
            }
        }
    }

    /**
     * <p>Parse a property values sublist - a list of whitespace separated
     * <tt>PropertyValue</tt>s.
     * </p><p>
     * Property value expressions for various properties may contaiin lists
     * of values, which may be separated by whitespace or by commas.  See,
     * e.g., 7.6.17 "voice-family" and 7.8.2 "font-family".  The shorthands
     * may also contain lists of elements, generally (or exclusively)
     * whitespace separated.  7.16.5 "text-shadow" allows whitespace
     * separated length doubles or triples to be specified for individual
     * shadow effects, with multiple shadow effects, each separated by
     * commmas.
     * @param initialValue a <tt>PropertyValue</tt> to assign as the initial
     * value of the sublist.  The detection of this value, which is
     * whitespace separated from a subsequent value,  has been the
     * trigger for the creation of the sublist.
     * @return a <tt>PropertyValueList</tt> containing the sublist.  The
     * indicatior for the end of the sublist is the end of the expression,
     * or a comma.
     */
    PropertyValueList parseSublist(PropertyValue initialValue)
        throws PropertyException
    {
        PropertyValueList sublist = new PropertyValueList(property);
        sublist.add(initialValue);
        while (true) {
            PropertyValue prop = parseAdditiveExpr();
            if (currentToken == EOF) {
                // end of the expression - add to sublist and go
                sublist.add(prop);
                return sublist;
            }
            // Comma separates next element - end of sublist
            if (currentToken == COMMA) {
                next();
                sublist.add(prop);
                return sublist;
            } else { // whitespace separates next elements; add to sublist
                sublist.add(prop);
            }
        }
    }

    /**
     * Reset the parser by resetting the tokenizer to null (or equivalent)
     * values.
     */
    public void resetParser() {
        synchronized (this) {
            //elementsSeen = 0;
            //restrictedValueFunctSeen = null;
            reset();
        }
    }

    /**
     * Try to parse an addition or subtraction expression and return the
     * resulting PropertyValue.
     */
    private PropertyValue parseAdditiveExpr() throws PropertyException {
        // Evaluate and put result on the operand stack
        PropertyValue prop = parseMultiplicativeExpr();
        loop:
        for (; ; ) {
            switch (currentToken) {
            case PLUS:
                next();
                ((Numeric)prop).add((Numeric)parseMultiplicativeExpr());
                break;
            case MINUS:
                next();
                ((Numeric)prop).subtract((Numeric)parseMultiplicativeExpr());
                break;
            default:
                break loop;
            }
        }
        return prop;
    }

    /**
     * Try to parse a multiply, divide or modulo expression and return
     * the resulting PropertyValue.
     */
    private PropertyValue parseMultiplicativeExpr() throws PropertyException {
        PropertyValue prop = parseUnaryExpr();
        loop:
        for (; ; ) {
            switch (currentToken) {
            case DIV:
                next();
                ((Numeric)prop).divide((Numeric)parseUnaryExpr());
                break;
            case MOD:
                next();
                ((Numeric)prop).mod((Numeric)parseUnaryExpr());
                break;
            case MULTIPLY:
                next();
                ((Numeric)prop).multiply((Numeric)parseUnaryExpr());
                break;
            default:
                break loop;
            }
        }
        return prop;
    }

    /**
     * Try to parse a unary minus expression and return the
     * resulting PropertyValue.
     */
    private PropertyValue parseUnaryExpr() throws PropertyException {
        if (currentToken == MINUS) {
            next();
            return ((Numeric)parseUnaryExpr()).negate();
        }
        return parsePrimaryExpr();
    }


    /**
     * Checks that the current token is a right parenthesis
     * and throws an exception if this isn't the case.
     */
    private final void expectRpar() throws PropertyException {
        if (currentToken != RPAR)
            throw new PropertyException("expected )");
        next();
    }

    /**
     * Try to parse a primary expression and return the
     * resulting PropertyValue.
     * A primary expression is either a parenthesized expression or an
     * expression representing a primitive PropertyValue datatype, such as a
     * string literal, an NCname, a number or a unit expression, or a
     * function call expression.
     */
    private PropertyValue parsePrimaryExpr() throws PropertyException {
        PropertyValue prop;
        //if (restrictedValueFunctSeen != null)
        //throw new PropertyException
        //(restrictedValueFunctSeen
        //+ " already seen with shorthand argument");
        switch (currentToken) {
        case LPAR:
            next();
            prop = parseAdditiveExpr();
            expectRpar();
            // Do this here, rather than breaking, because expectRpar()
            // consumes the right parenthesis and calls next().
            //elementsSeen++;
            return prop;

        case LITERAL:
            prop = new Literal(property, currentTokenValue);
            break;

        case NCNAME:
            // Interpret this in context of the property or do it later?
            prop = new NCName(property, currentTokenValue);
            break;

        case FLOAT:
            // Do I need to differentiate here between floats and integers?
            prop = new Numeric
                    (property, (new Double(currentTokenValue)).doubleValue());
            break;

        case INTEGER:
            prop = IntegerType.makeInteger
                    (property, (new Long(currentTokenValue)).longValue());
            break;

        case PERCENT:
            /*
             * Generate a Percentage object with the percentage number.
             * The constructor converts this to a straight multiplicative
             * factor by dividing by 100.
             */
            prop = Percentage.makePercentage
                    (property, (new Double(currentTokenValue)).doubleValue());
            break;

        case ABSOLUTE_LENGTH:
            prop = Length.makeLength(property,
                              (new Double(currentTokenValue)).doubleValue(),
                              currentUnit);
            break;
        case TIME:
            prop = Time.makeTime(property,
                              (new Double(currentTokenValue)).doubleValue(),
                              currentUnit);
            break;
        case FREQ:
            prop = Frequency.makeFrequency(property,
                              (new Double(currentTokenValue)).doubleValue(),
                              currentUnit);
            break;
        case ANGLE:
            prop = Angle.makeAngle(property,
                              (new Double(currentTokenValue)).doubleValue(),
                              currentUnit);
            break;
        case RELATIVE_LENGTH:
            prop = Ems.makeEms(property,
                     (new Double(currentTokenValue)).doubleValue());
            break;

        case COLORSPEC:
            prop = new ColorType(property, currentTokenValue);
            break;

        case BOOL:
            prop = new Bool(property, currentTokenValue);
            break;

        case AUTO:
            prop = new Auto(property);
            break;

        case NONE:
            prop = new None(property);
            break;

        case INHERIT:
            throw new PropertyException("INHERIT not supported");
            //break;

        case URI:
            prop = new UriType(property, currentTokenValue);
            break;

        case MIMETYPE:
            prop = new MimeType(property, currentTokenValue);
            break;

        case FUNCTION_LPAR: {
            // N.B. parseArgs() invokes expectRpar at the end of argument
            // processing, so, like LPAR processing, next() is not called
            // and the return from this method must be premature
            prop = null;
            // Numeric functions
            if (currentTokenValue.equals("floor")) {
                PropertyValue[] args = parseArgs(1);
                prop = new Numeric
                        (property, ((Numeric)args[0]).floor());
            }
            else if (currentTokenValue.equals("ceiling")) {
                PropertyValue[] args = parseArgs(1);
                prop = new Numeric
                        (property, ((Numeric)args[0]).ceiling());
            }
            else if (currentTokenValue.equals("round")) {
                PropertyValue[] args = parseArgs(1);
                prop = new Numeric
                        (property, ((Numeric)args[0]).round());
            }
            else if (currentTokenValue.equals("min")) {
                PropertyValue[] args = parseArgs(2);
                prop = new Numeric
                        (property, ((Numeric)args[0]).min((Numeric)args[1]));
            }
            else if (currentTokenValue.equals("max")) {
                PropertyValue[] args = parseArgs(2);
                prop = new Numeric
                        (property, ((Numeric)args[0]).max((Numeric)args[1]));
            }
            else if (currentTokenValue.equals("abs")) {
                PropertyValue[] args = parseArgs(1);
                prop = new Numeric
                        (property, ((Numeric)args[0]).abs());
            }

            // Color functions
            else if (currentTokenValue.equals("rgb")) {
                PropertyValue[] args = parseArgs(3);
                prop = new ColorType
                        (property, ((Numeric)args[0]).asInt(),
                         ((Numeric)args[1]).asInt(),
                         ((Numeric)args[2]).asInt());
            }
            else if (currentTokenValue.equals("rgb-icc")) {
                PropertyValue[] args = parseArgs(6);
                throw new PropertyException
                        ("rgb-icc function is not supported.");
            }
            else if (currentTokenValue.equals("system-color")) {
                PropertyValue[] args = parseArgs(1);
                prop = new ColorType
                        (property, ((StringType)args[0]).getString());
            }

            // Font function
            else if (currentTokenValue.equals("system-font")) {
                PropertyValue[] args = parseArgs(1, 2);
                throw new PropertyException
                        ("system-font function is not supported.");
            }

            // Property value functions
            else if (currentTokenValue.equals("inherited-property-value")) {
                int propindex = property;
                PropertyValue[] args = parseArgs(0, 1);
                if (args.length != 0)
                    propindex = PropertyConsts.getPropertyIndex(
                            ((StringType)args[0]).getString());
                if (PropertyConsts.inheritance(propindex) == Properties.NO)
                    throw new PropertyException
                            ("inherited-property-value: "
                             + PropNames.getPropertyName(propindex)
                             + " is not inherited.");
                prop = new Inherit(property, propindex);
            }
            else if (currentTokenValue.equals("label-end")) {
                PropertyValue[] args = parseArgs(0);
                throw new PropertyException
                        ("label-end function is not supported.");
            }
            else if (currentTokenValue.equals("body-start")) {
                PropertyValue[] args = parseArgs(0);
                throw new PropertyException
                        ("body-start function is not supported.");
            }
            // N.B. see comments on classes FromNearestSpecified and
            // FromParent for explanation of this section
            else if (currentTokenValue.equals("from-parent") ||
                     currentTokenValue.equals("from-nearest-specified-value"))
            {
                // Preset the return value in case of a shorthand property
                if (currentTokenValue.equals("from-parent"))
                    prop = new FromParent(property);
                else
                    prop = new FromNearestSpecified(property);

                PropertyValue[] args = parseArgs(0, 1);
                if (args.length == 0) {
                    if (! PropertyConsts.isShorthand(property)) {
                        // develop the function value and return it as
                        // a property.
                        throw new PropertyException
                                (currentTokenValue +
                                     " function is not supported.");
                    }
                    // else a shorthand - do nothing; prop has been set
                    // to the appropriate pseudo-propertyValue
                } else { // one argument - it must be a property name
                    if ( ! (args[0] instanceof NCName))
                        throw new PropertyException
                                (currentTokenValue + " function requires"
                                     + " property name arg.");
                    // else arg[0] is an NCName
                    NCName ncname = (NCName)args[0];
                    String propname = ncname.getNCName();
                    int nameindex = PropertyConsts.getPropertyIndex(propname);
                    if (PropertyConsts.isShorthand(nameindex)) {
                        // the argument is a shorthand property -
                        // it must be the same as the property being
                        // assigned to.
                        // see 5.10.4 Property Value Functions
                        if ( ! (nameindex == property))
                            throw new PropertyException
                                    (currentTokenValue +
                                     " argument " + propname +
                                     " does not match property " +
                                     PropNames.getPropertyName(property));
                        // else perform shorthand processing
                        // i.e. do nothing; prop has been set to the correct
                        // pseudo-propertyValue
                    }
                    else {   // An NCName but not a shorthand
                        // Perform normal from-parent processing
                        throw new PropertyException
                                (currentTokenValue +
                                 " function is not supported.");
                    }
                }
            }
            else if (currentTokenValue.equals("from-table-column")) {
                PropertyValue[] args = parseArgs(0, 1);
                throw new PropertyException
                        ("from-table-column function is not supported.");
            }
            else if (currentTokenValue.equals("proportional-column-width")) {
                PropertyValue[] args = parseArgs(1);
                throw new PropertyException
                        ("proportional-column-width "
                         + "function is not supported.");
            }
            else if (currentTokenValue.equals("merge-property-values")) {
                PropertyValue[] args = parseArgs(0, 1);
                throw new PropertyException
                        ("merge-property-values function is not supported.");
            }
            else
                throw new PropertyException("no such function: "
                                            + currentTokenValue);
            //elementsSeen++;
            return prop;
        }
        default:
            throw new PropertyException("syntax error");
        }
        next();
        //elementsSeen++;
        return prop;
    }

    /**
     * Parse a comma separated list of function arguments. Each argument
     * may itself be an expression. This method consumes the closing right
     * parenthesis of the argument list.
     * @param nbArgs The number of arguments expected by the function.
     * @return <tt>PropertyValueList</tt> of <tt>PropertyValue</tt> objects
     * representing the arguments found.
     * @exception PropertyException
     */
    PropertyValue[] parseArgs(int nbArgs) throws PropertyException {
        return parseArgs(nbArgs, nbArgs);
    }

    /**
     * Parse a comma separated list of function arguments. Each argument
     * may itself be an expression. This method consumes the closing right
     * parenthesis of the argument list.
     * @param minArgs The minimum number of arguments expected by the function.
     * @param maxArgs The maximum number of arguments expected by the function.
     * @return <tt>PropertyValueList</tt> of <tt>PropertyValue</tt> objects
     * representing the arguments found.  N.B.  The actual number of arguments
     * returned is guaranteed to be between minArgs and maxArgs, inclusive,
     * but the actual list of args found is terminated by the end of the
     * array, or the first null element.
     * @exception PropertyException
     */
    PropertyValue[] parseArgs(int minArgs, int maxArgs)
        throws PropertyException
    {
        PropertyValue[] args = new PropertyValue[maxArgs];
        PropertyValue prop;
        int i = 0;
        if (currentToken == RPAR) {
            // No args: func()
            next();
        } else {
            while (true) {
                prop = parseAdditiveExpr();
                if (i < maxArgs) {
                    args[i++] = prop;
                }
                // ignore extra args
                if (currentToken != COMMA)
                    break;
                next();
            }
            expectRpar();
        }
        if (minArgs > i || i > maxArgs) {
            throw new PropertyException("Wrong number of args for function");
        }
        return args;
    }

}
