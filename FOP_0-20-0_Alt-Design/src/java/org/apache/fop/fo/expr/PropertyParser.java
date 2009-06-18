/*
 * $Id: PropertyParser.java,v 1.5.2.22 2003/06/12 18:19:36 pbwest Exp $
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */

package org.apache.fop.fo.expr;

import org.apache.fop.datatypes.Angle;
import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.Bool;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Ems;
import org.apache.fop.datatypes.Frequency;
import org.apache.fop.datatypes.IntegerType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Literal;
import org.apache.fop.datatypes.MimeType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.None;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Percentage;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.Slash;
import org.apache.fop.datatypes.StringType;
import org.apache.fop.datatypes.Time;
import org.apache.fop.datatypes.UriType;
import org.apache.fop.datatypes.indirect.FromNearestSpecified;
import org.apache.fop.datatypes.indirect.FromParent;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.datatypes.indirect.InheritedValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.properties.Property;

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

    private static final String tag = "$Name:  $";
    private static final String revision = "$Revision: 1.5.2.22 $";

    /** The FO tree which has initiated this parser */
    private FOTree foTree;
    /** The FONode which has initiated this parser */
    private FONode node;

    public PropertyParser(FOTree foTree) {
        super();
        this.foTree = foTree;
    }

    /**
     * Parse the property expression described in the instance variables.
     * 
     * <p>The <tt>PropertyValue</tt> returned by this function has the
     * following characteristics:
     * If the expression resolves to a single element that object is returned
     * directly in an object which implements <PropertyValue</tt>.
     *
     * <p>If the expression cannot be resolved into a single object, the set
     * to which it resolves is returned in a <tt>PropertyValueList</tt> object
     * (which itself implements <tt>PropertyValue</tt>).
     *
     * <p>The <tt>PropertyValueList</tt> contains objects whose corresponding
     * elements in the original expression were separated by <em>commas</em>.
     *
     * <p>Objects whose corresponding elements in the original expression
     * were separated by spaces are composed into a sublist contained in
     * another <tt>PropertyValueList</tt>.  If all of the elements in the
     * expression were separated by spaces, the returned
     * <tt>PropertyValueList</tt> will contain one element, a
     * <tt>PropertyValueList</tt> containing objects representing each of
     * the space-separated elements in the original expression.
     *
     * <p>E.g., if a <b>font-family</b> property is assigned the string
     * <em>Palatino, New Century Schoolbook, serif</em>, the returned value
     * will look like this:
     * <pre>
     * PropertyValueList(NCName('Palatino')
     *                   PropertyValueList(NCName('New')
     *                                     NCName('Century')
     *                                     NCName('Schoolbook') )
     *                   NCName('serif') )
     * </pre>
     * <p>If the property had been assigned the string
     * <em>Palatino, "New Century Schoolbook", serif</em>, the returned value
     * would look like this:
     * <pre>
     * PropertyValueList(NCName('Palatino')
     *                   NCName('New Century Schoolbook')
     *                   NCName('serif') )
     * </pre>
     * <p>If a <b>background-position</b> property is assigned the string
     * <em>top center</em>, the returned value will look like this:
     * <pre>
     * PropertyValueList(PropertyValueList(NCName('top')
     *                                     NCName('center') ) )
     * </pre>
     *
     * <p>Note: If the property expression String is empty, a StringProperty
     * object holding an empty String is returned.
     * @param node - the <tt>FONode</tt> for which the property expression
     * is being resolved.
     * @param property - an <tt>int</tt> containing the property index.
     * which the property expression is to be evaluated.
     * @param expr - the specified value (attribute on the xml element).
     * @return a PropertyValue holding the parsed result.
     * @throws PropertyException if the "expr" cannot be parsed as a
     * PropertyValue.
     */
    public PropertyValue parse(FONode node, int property, String expr)
        throws PropertyException
    {
        //System.out.println("-----Entering parse:"
        // + PropNames.getPropertyName(property) + " " + expr);
        synchronized (this) {
            // make sure this parser is available
            if (getExpr() != null) // the parser is currently active
                throw new PropertyException
                        ("PropertyParser is currently active: " + getExpr());
            initialize(property, expr);
            this.node = node;
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
                    reset();
                    return propList;
                } else { // list is empty
                    reset();
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
                if (currentToken == EOF) {
                    reset();
                    return propList;
                }
            }
        }
    }

    /**
     * <p>Parse a property values sublist - a list of whitespace separated
     * <tt>PropertyValue</tt>s.
     * <p>
     * Property value expressions for various properties may contain lists
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
     * Generate an arithmetic error string.
     * @return arithmetic error message.
     */
    private String arithErrorStr() {
        return "Arithmetic operator not followed by Numeric or integer: "
                + getExpr();
    }


    /**
     * Generate an function numeric argument error string.
     * @return function numeric argument error message.
     */
    private String funcNumericErrorStr() {
        return "Function requires Numeric or integer argument: "
                + getExpr();
    }

    /**
     * Try to parse an addition or subtraction expression and return the
     * resulting PropertyValue.
     */
    private PropertyValue parseAdditiveExpr() throws PropertyException {
        // Evaluate and put result on the operand stack
        //System.out.println("parseAdd");
        PropertyValue prop = parseMultiplicativeExpr();
        PropertyValue pv;
        outer:
        for (; ; ) {
            inner:
            switch (prop.getType()) {
            case PropertyValue.NUMERIC: {
                switch (currentToken) {
                case PLUS:
                    next();
                    pv = parseMultiplicativeExpr();
                    switch (pv.getType()) {
                    case PropertyValue.NUMERIC:
                        ((Numeric)prop).add((Numeric)pv);
                        break inner;
                    case PropertyValue.INTEGER:
                        ((Numeric)prop).add(((IntegerType)pv).getInt());
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                case MINUS:
                    next();
                    pv = parseMultiplicativeExpr();
                    switch (pv.getType()) {
                    case PropertyValue.NUMERIC:
                        ((Numeric)prop).subtract((Numeric)pv);
                        break inner;
                    case PropertyValue.INTEGER:
                        ((Numeric)prop).subtract(((IntegerType)pv).getInt());
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                default:
                    break outer;
                }
            }
            case PropertyValue.INTEGER: {
                int intVal = ((IntegerType)prop).getInt();
                switch (currentToken) {
                case PLUS:
                    next();
                    pv = parseMultiplicativeExpr();
                    switch (pv.getType()) {
                    case PropertyValue.NUMERIC:
                        prop = ((Numeric)pv).add(intVal);
                        break inner;
                    case PropertyValue.INTEGER:
                        ((IntegerType)prop).setInt(intVal +
                                            ((IntegerType)pv).getInt());
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                case MINUS:
                    next();
                    pv = parseMultiplicativeExpr();
                    switch (pv.getType()) {
                    case PropertyValue.NUMERIC:
                        ((Numeric)pv).add(-intVal);
                        prop = ((Numeric)pv).negate();
                        break inner;
                    case PropertyValue.INTEGER:
                        ((IntegerType)prop).setInt(intVal +
                                            ((IntegerType)pv).getInt());
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                default:
                    break outer;
                }
            }
            default:
                break outer;
            }
        }
        return prop;
    }

    /**
     * Try to parse a multiply, divide or modulo expression and return
     * the resulting PropertyValue.
     */
    private PropertyValue parseMultiplicativeExpr() throws PropertyException {
        //System.out.println("parseMult");
        PropertyValue prop = parseUnaryExpr();
        PropertyValue pv;
        outer:
        // Outer loop exists to handle a sequence of multiplicative operations
        // e.g. 5 * 4 / 2
        // break outer; will terminate the multiplicative expression parsing
        // break inner; will look for another trailing multiplicative
        // operator.
        for (; ; ) {
            inner:
            switch (prop.getType()) {
            case PropertyValue.NUMERIC:
                switch (currentToken) {
                case DIV:
                    next();
                    pv = parseUnaryExpr();
                    switch (pv.getType()) {
                    case PropertyValue.INTEGER:
                        ((Numeric)prop).divide(((IntegerType)pv).getInt());
                        break inner;
                    case PropertyValue.NUMERIC:
                        ((Numeric)prop).divide((Numeric)pv);
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                case MOD:
                    next();
                    pv = parseUnaryExpr();
                    switch (pv.getType()) {
                    case PropertyValue.INTEGER:
                        ((Numeric)prop).mod(((IntegerType)pv).getInt());
                        break inner;
                    case PropertyValue.NUMERIC:
                        ((Numeric)prop).mod((Numeric)pv);
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                case MULTIPLY:
                    next();
                    pv = parseUnaryExpr();
                    switch (pv.getType()) {
                    case PropertyValue.INTEGER:
                        ((Numeric)prop).multiply(((IntegerType)pv).getInt());
                        break inner;
                    case PropertyValue.NUMERIC:
                        ((Numeric)prop).multiply((Numeric)pv);
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                default:
                    break outer;
                }
                // N.B. The above case cannot fall through to here
            case PropertyValue.INTEGER:
                // This code treats all multiplicative operations as implicit
                // operations on doubles.  It might be reasonable to allow
                // an integer multiply.
                int intVal = ((IntegerType)prop).getInt();
                switch (currentToken) {
                case DIV:
                    next();
                    pv = parseUnaryExpr();
                    switch (pv.getType()) {
                    case PropertyValue.INTEGER:
                        prop = new Numeric(property,
                            (double)intVal / ((IntegerType)pv).getInt());
                        break inner;
                    case PropertyValue.NUMERIC:
                        prop = (new Numeric(property, (double)intVal))
                                                        .divide((Numeric)pv);
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                case MOD:
                    next();
                    pv = parseUnaryExpr();
                    switch (pv.getType()) {
                    case PropertyValue.INTEGER:
                        prop = new Numeric(property,
                                ((double)intVal) % ((IntegerType)pv).getInt());
                        break inner;
                    case PropertyValue.NUMERIC:
                        prop = (new Numeric(property, (double)intVal))
                                                        .mod((Numeric)pv);
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                case MULTIPLY:
                    next();
                    pv = parseUnaryExpr();
                    switch (pv.getType()) {
                    case PropertyValue.INTEGER:
                        prop = new Numeric(property,
                            ((double)intVal) * ((IntegerType)pv).getInt());
                        break inner;
                    case PropertyValue.NUMERIC:
                        prop = (new Numeric(property, (double)intVal))
                                                   .multiply((Numeric)pv);
                        break inner;
                    default:
                        throw new PropertyException(arithErrorStr());
                    }
                default:
                    break outer;
                }
            default:
                break outer;
            }
        }
        return prop;
    }

    /**
     * Try to parse a unary minus expression and return the
     * resulting PropertyValue.
     */
    private PropertyValue parseUnaryExpr() throws PropertyException {
        //System.out.println("Unary entry");
        if (currentToken == MINUS) {
            next();
            PropertyValue pv = parseUnaryExpr();
            switch (pv.getType()) {
            case PropertyValue.NUMERIC:
                return ((Numeric)pv).negate();
            case PropertyValue.INTEGER:
                ((IntegerType)pv).setInt( -((IntegerType)pv).getInt());
                return pv;
            default:
                throw new PropertyException(arithErrorStr());
            }
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
        //System.out.println("Primary currentToken:" + currentToken + " "
        //                   + currentTokenValue);
        switch (currentToken) {
        case LPAR:
            next();
            prop = parseAdditiveExpr();
            expectRpar();
            // Do this here, rather than breaking, because expectRpar()
            // consumes the right parenthesis and calls next().
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
                    (property, Double.parseDouble(currentTokenValue));
            break;

        case INTEGER:
            prop = new IntegerType
                    (property, Integer.parseInt(currentTokenValue));
            break;

        case PERCENT:
            /*
             * Generate a Percentage object with the percentage number.
             * The constructor converts this to a straight multiplicative
             * factor by dividing by 100.
             */
            prop = Percentage.makePercentage
                    (property, Double.parseDouble(currentTokenValue));
            break;

        case ABSOLUTE_LENGTH:
            prop = Length.makeLength(property,
                              Double.parseDouble(currentTokenValue),
                              currentUnit);
            break;
        case TIME:
            prop = new Time(property, currentUnit,
                            Double.parseDouble(currentTokenValue));
            break;
        case FREQ:
            prop = new Frequency(property, currentUnit,
                                 Double.parseDouble(currentTokenValue));
            break;
        case ANGLE:
            prop = new Angle(property, currentUnit,
                             Double.parseDouble(currentTokenValue));
            break;
        case RELATIVE_LENGTH:
            prop = Ems.makeEms(node, property,
                               Double.parseDouble(currentTokenValue));
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
            prop = new Inherit(property);
            //throw new PropertyException("INHERIT not supported");
            break;

        case URI:
            prop = new UriType(property, currentTokenValue);
            break;

        case MIMETYPE:
            prop = new MimeType(property, currentTokenValue);
            break;

        case SLASH:
            prop = new Slash(property);
            break;

        case FUNCTION_LPAR: {
            // N.B. parseArgs() invokes expectRpar at the end of argument
            // processing, so, like LPAR processing, next() is not called
            // and the return from this method must be premature
            prop = null;
            int funcType = PropertyValue.NO_TYPE;
            String function = currentTokenValue;
            next();
            do {
                // Numeric functions
                if (function.equals("floor")) {
                    PropertyValue[] args = parseArgs(1);
                    switch (args[0].getType()) {
                    case PropertyValue.INTEGER:
                        args[0] =
                            new Numeric
                                (property, ((IntegerType)args[0]).getInt());
                    case PropertyValue.NUMERIC:
                        prop = new Numeric
                                (property, ((Numeric)args[0]).floor());
                        break;
                    default:
                        throw new PropertyException(funcNumericErrorStr());
                    }
                    break;
                }
                if (function.equals("ceiling")) {
                    PropertyValue[] args = parseArgs(1);
                    switch (args[0].getType()) {
                    case PropertyValue.INTEGER:
                        args[0] =
                            new Numeric
                                (property, ((IntegerType)args[0]).getInt());
                    case PropertyValue.NUMERIC:
                        prop = new Numeric
                                (property, ((Numeric)args[0]).ceiling());
                        break;
                    default:
                        throw new PropertyException(funcNumericErrorStr());
                    }
                    break;
                }
                if (function.equals("round")) {
                    PropertyValue[] args = parseArgs(1);
                    switch (args[0].getType()) {
                    case PropertyValue.INTEGER:
                        args[0] =
                            new Numeric
                                (property, ((IntegerType)args[0]).getInt());
                    case PropertyValue.NUMERIC:
                        prop = new Numeric
                                (property, ((Numeric)args[0]).round());
                        break;
                    default:
                        throw new PropertyException(funcNumericErrorStr());
                    }
                    break;
                }
                if (function.equals("min")) {
                    PropertyValue[] args = parseArgs(2);
                    switch (args[0].getType()) {
                    case PropertyValue.INTEGER:
                        args[0] =
                            new Numeric
                                (property, ((IntegerType)args[0]).getInt());
                    case PropertyValue.NUMERIC:
                        prop = ((Numeric)args[0]).min((Numeric)args[1]);
                        break;
                    default:
                        throw new PropertyException(funcNumericErrorStr());
                    }
                    break;
                }
                if (function.equals("max")) {
                    PropertyValue[] args = parseArgs(2);
                    switch (args[0].getType()) {
                    case PropertyValue.INTEGER:
                        args[0] =
                            new Numeric
                                (property, ((IntegerType)args[0]).getInt());
                    case PropertyValue.NUMERIC:
                        prop = ((Numeric)args[0]).max((Numeric)args[1]);
                        break;
                    default:
                        throw new PropertyException(funcNumericErrorStr());
                    }
                    break;
                }
                if (function.equals("abs")) {
                    PropertyValue[] args = parseArgs(1);
                    switch (args[0].getType()) {
                    case PropertyValue.INTEGER:
                        args[0] =
                            new Numeric
                                (property, ((IntegerType)args[0]).getInt());
                    case PropertyValue.NUMERIC:
                        prop = ((Numeric)args[0]).abs();
                        break;
                    default:
                        throw new PropertyException(funcNumericErrorStr());
                    }
                    break;
                }

                // Color functions
                if (function.equals("rgb")) {
                    // Currently arguments must all be integers.
                    PropertyValue[] args = parseArgs(3);
                    switch (args[0].getType()) {
                    case PropertyValue.INTEGER:
                        prop = new ColorType
                                (property, ((IntegerType)args[0]).getInt(),
                                 ((IntegerType)args[1]).getInt(),
                                 ((IntegerType)args[2]).getInt());
                        break;
                    case PropertyValue.NUMERIC:
                        prop = new ColorType
                                (property, ((Numeric)args[0]).asInt(),
                                 ((Numeric)args[1]).asInt(),
                                 ((Numeric)args[2]).asInt());
                        break;
                    default:
                        throw new PropertyException(funcNumericErrorStr());
                    }
                    break;
                }
                if (function.equals("rgb-icc")) {
                    PropertyValue[] args = parseArgs(6);
                    throw new FunctionNotImplementedException("rgb-icc");
                    //break;
                }
                if (function.equals("system-color")) {
                    PropertyValue[] args = parseArgs(1);
                    prop = new ColorType
                            (property, ((StringType)args[0]).getString());
                    break;
                }

                // Font function
                if (function.equals("system-font")) {
                    PropertyValue[] args = parseArgs(1, 2);
                    if (args.length == 1) {
                        prop = SystemFontFunction.systemFontCharacteristic
                                (property,
                                 ((StringType)args[0]).getString());
                    } else {
                        // 2 args
                        prop = SystemFontFunction.systemFontCharacteristic
                                (property,
                                 ((StringType)args[0]).getString(),
                                 ((StringType)args[1]).getString());
                    }
                    break;
                }

                // Property value functions
                if (function.equals("label-end")) {
                    PropertyValue[] args = parseArgs(0);
                    throw new FunctionNotImplementedException("label-end");
                    //break;
                }
                if (function.equals("body-start")) {
                    PropertyValue[] args = parseArgs(0);
                    throw new FunctionNotImplementedException("body-start");
                    //break;
                }
                if (function.equals("inherited-property-value")) {
                    int propindex = property;
                    PropertyValue[] args = parseArgs(0, 1);
                    if (args.length != 0)
                        propindex = PropNames.getPropertyIndex(
                                ((StringType)args[0]).getString());

                    // If it's a compound, return an InheritedValue object
                    if (PropertyConsts.pconsts.isCompound(propindex)) {
                        prop = new InheritedValue(property, propindex);
                        break;
                    }
                    // Is it an inherited property?
                    if (PropertyConsts.pconsts.inheritance(propindex)
                                                            == Property.NO)
                        throw new PropertyException
                                ("inherited-property-value: "
                                 + PropNames.getPropertyName(propindex)
                                 + " is not inherited.");
                    // Not a compound, and inherited - try to resolve it
                    prop = node.fromParent(property, propindex);
                    break;
                }
                // N.B. see comments on classes FromNearestSpecified and
                // FromParent for explanation of this section
                if (function.equals("from-parent"))
                    funcType = PropertyValue.FROM_PARENT;
                if (function.equals("from-nearest-specified-value"))
                    funcType = PropertyValue.FROM_NEAREST_SPECIFIED;
                if (funcType == PropertyValue.FROM_PARENT
                    || funcType == PropertyValue.FROM_NEAREST_SPECIFIED)
                {
                    // Preset the return value in case of a shorthand property
                    switch (funcType) {
                    case PropertyValue.FROM_PARENT:
                        prop = new FromParent(property);
                    case PropertyValue.FROM_NEAREST_SPECIFIED:
                        prop = new FromNearestSpecified(property);
                    }

                    PropertyValue[] args = parseArgs(0, 1);
                    if (args.length == 0) {
                        if (! (PropertyConsts.pconsts.isShorthand(property)
                           || PropertyConsts.pconsts.isCompound(property))) {
                            // develop the function value and return it as
                            // a property.
                            switch (funcType) {
                            case PropertyValue.FROM_PARENT:
                                prop = node.fromParent(property);
                            case PropertyValue.FROM_NEAREST_SPECIFIED:
                                prop = node.fromNearestSpecified(property);
                            }
                        }
                        // else a shorthand/compound - do nothing;
                        // prop has been
                        // set to the appropriate pseudo-propertyValue
                    } else { // one argument - it must be a property name
                        if ( ! (args[0] instanceof NCName))
                            throw new PropertyException
                                    (function + " function requires"
                                     + " property name arg.");
                        // else arg[0] is an NCName
                        NCName ncname = (NCName)args[0];
                        String propname = ncname.getNCName();
                        int nameindex =
                                PropNames.getPropertyIndex(propname);
                        if (PropertyConsts.pconsts.isShorthand(nameindex)
                            || PropertyConsts.pconsts.isCompound(nameindex)) {
                            // the argument is a shorthand/compound property -
                            // it must be the same as the property being
                            // assigned to.
                            // see 5.10.4 Property Value Functions
                            if ( ! (nameindex == property))
                                throw new PropertyException
                                        (function +
                                         " argument " + propname +
                                         " does not match property " +
                                         PropNames.getPropertyName(property));
                            // else perform shorthand/compound processing
                            // i.e. do nothing;
                            // prop has been set to the correct
                            // pseudo-propertyValue
                        }
                        else {   // An NCName but not a shorthand/compound
                            // Perform normal from-? processing
                            switch (funcType) {
                            case PropertyValue.FROM_PARENT:
                                prop = node.fromParent(property, nameindex);
                            case PropertyValue.FROM_NEAREST_SPECIFIED:
                                prop = node.fromNearestSpecified
                                                        (property, nameindex);
                            }
                        }
                    }
                    break;
                }
                if (function.equals("from-table-column")) {
                    PropertyValue[] args = parseArgs(0, 1);
                    throw new FunctionNotImplementedException
                            ("from-table-column");
                    //break;
                }
                if (function.equals("proportional-column-width")) {
                    PropertyValue[] args = parseArgs(1);
                    throw new FunctionNotImplementedException
                            ("proportional-column-width");
                    //break;
                }
                if (function.equals("merge-property-values")) {
                    PropertyValue[] args = parseArgs(0, 1);
                    throw new FunctionNotImplementedException
                            ("merge-property-values");
                    //break;
                }
                throw new PropertyException("no such function: "
                                                        + function);
            } while (false);
            return prop;
        }
        default:
            throw new PropertyException("syntax error");
        }
        next();
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
