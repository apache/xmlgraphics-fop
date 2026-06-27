// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System.Globalization;
using Fop.Colors;

namespace Fop.Fo.Expr;

/// <summary>
/// A recursive-descent parser and evaluator for XSL-FO property expressions. A clean, modern
/// reformulation of FOP's <c>org.apache.fop.fo.expr.PropertyParser</c> + the function classes.
/// <para>
/// Supports unary minus, parentheses, the operators <c>+ - * div mod</c> (additive precedence below
/// multiplicative), mixed numeric/length arithmetic, and the standard function set. The
/// context-and-layout-dependent functions (<c>proportional-column-width</c>, <c>label-end</c>,
/// <c>body-start</c>, <c>from-table-column</c>) are recognised but deliberately <em>not</em> fully
/// resolved here: they yield a documented sentinel <see cref="ExprValueKind.String"/> so the
/// existing layout-side string handling keeps working.
/// </para>
/// </summary>
public sealed class ExprEvaluator
{
    private const string EmUnit = "em";

    private readonly ExprTokenizer tokenizer;
    private readonly IExpressionContext context;
    private ExprToken current;

    private ExprEvaluator(string expression, IExpressionContext context)
    {
        tokenizer = new ExprTokenizer(expression);
        this.context = context;
    }

    /// <summary>
    /// Parses and evaluates <paramref name="expression"/> against <paramref name="context"/>,
    /// returning the typed result. An empty expression yields an empty string value.
    /// </summary>
    /// <exception cref="PropertyException">If the expression is malformed.</exception>
    public static ExprValue Evaluate(string expression, IExpressionContext context)
    {
        ArgumentNullException.ThrowIfNull(expression);
        ArgumentNullException.ThrowIfNull(context);
        var evaluator = new ExprEvaluator(expression, context);
        return evaluator.Parse();
    }

    private ExprValue Parse()
    {
        Advance();
        if (current.Kind == ExprTokenKind.Eof)
        {
            return ExprValue.FromString(string.Empty);
        }

        ExprValue value = ParseAdditive();

        // A space-separated list (e.g. font-family or a shorthand): keep the first component, which
        // is what the curated single-value accessors want. Skip remaining components defensively.
        while (current.Kind != ExprTokenKind.Eof)
        {
            ParseAdditive();
        }

        return value;
    }

    private ExprValue ParseAdditive()
    {
        ExprValue left = ParseMultiplicative();
        while (true)
        {
            switch (current.Kind)
            {
                case ExprTokenKind.Plus:
                    Advance();
                    left = NumericOp.Add(left, ParseMultiplicative());
                    break;
                case ExprTokenKind.Minus:
                    Advance();
                    left = NumericOp.Subtract(left, ParseMultiplicative());
                    break;
                default:
                    return left;
            }
        }
    }

    private ExprValue ParseMultiplicative()
    {
        ExprValue left = ParseUnary();
        while (true)
        {
            switch (current.Kind)
            {
                case ExprTokenKind.Multiply:
                    Advance();
                    left = NumericOp.Multiply(left, ParseUnary());
                    break;
                case ExprTokenKind.Div:
                    Advance();
                    left = NumericOp.Divide(left, ParseUnary());
                    break;
                case ExprTokenKind.Mod:
                    Advance();
                    left = NumericOp.Modulo(left, ParseUnary());
                    break;
                default:
                    return left;
            }
        }
    }

    private ExprValue ParseUnary()
    {
        if (current.Kind == ExprTokenKind.Minus)
        {
            Advance();
            return NumericOp.Negate(ParseUnary());
        }

        return ParsePrimary();
    }

    private ExprValue ParsePrimary()
    {
        // Skip a leading comma (FOP does this, e.g. for font-family lists).
        if (current.Kind == ExprTokenKind.Comma)
        {
            Advance();
        }

        ExprToken token = current;
        switch (token.Kind)
        {
            case ExprTokenKind.LPar:
            {
                Advance();
                ExprValue inner = ParseAdditive();
                ExpectRPar();
                return inner;
            }

            case ExprTokenKind.Literal:
                Advance();
                return ExprValue.FromString(token.Value);

            case ExprTokenKind.NCName:
                Advance();
                return ExprValue.FromString(token.Value);

            case ExprTokenKind.Integer:
            case ExprTokenKind.Float:
                Advance();
                return ExprValue.FromNumber(ParseDouble(token.Value));

            case ExprTokenKind.Percent:
            {
                Advance();
                double pct = ParseDouble(token.Value);
                if (context.PercentBaseMpt is { } baseMpt)
                {
                    return ExprValue.FromLength(baseMpt * pct / 100.0);
                }

                return ExprValue.FromPercentage(pct);
            }

            case ExprTokenKind.Numeric:
                Advance();
                return ParseNumeric(token);

            case ExprTokenKind.ColorSpec:
                Advance();
                return ParseColorSpec(token.Value);

            case ExprTokenKind.FunctionLPar:
                return ParseFunction(token.Value);

            default:
                throw new PropertyException($"syntax error in expression near '{token.Value}'");
        }
    }

    private ExprValue ParseNumeric(ExprToken token)
    {
        string unit = token.UnitPart;
        double number = ParseDouble(token.NumberPart);
        if (unit == EmUnit)
        {
            return ExprValue.FromLength(number * context.FontSizeMpt);
        }

        FoLength? length = FoLength.TryParse(token.Value, context.FontSizeMpt);
        if (length is null)
        {
            throw new PropertyException($"unrecognized unit in '{token.Value}'");
        }

        return ExprValue.FromLength(length.Value.Millipoints);
    }

    private static ExprValue ParseColorSpec(string hex)
    {
        FopColor? color = ColorUtil.ParseColorString(null, hex);
        if (color is null)
        {
            throw new PropertyException($"invalid colour '{hex}'");
        }

        return ExprValue.FromColor(color);
    }

    private ExprValue ParseFunction(string name)
    {
        Advance(); // consume the function-name '(' token; current is now the first arg (or ')')
        List<ExprValue> args = ParseArgs();
        return ExprFunctions.Eval(name, args, context);
    }

    private List<ExprValue> ParseArgs()
    {
        var args = new List<ExprValue>();
        if (current.Kind == ExprTokenKind.RPar)
        {
            Advance();
            return args;
        }

        while (true)
        {
            args.Add(ParseAdditive());
            if (current.Kind != ExprTokenKind.Comma)
            {
                break;
            }

            Advance();
        }

        ExpectRPar();
        return args;
    }

    private void ExpectRPar()
    {
        if (current.Kind != ExprTokenKind.RPar)
        {
            throw new PropertyException("expected ')' in expression");
        }

        Advance();
    }

    private void Advance() => current = tokenizer.Next();

    private static double ParseDouble(string text)
    {
        if (double.TryParse(text, NumberStyles.Float, CultureInfo.InvariantCulture, out double value))
        {
            return value;
        }

        throw new PropertyException($"invalid number '{text}'");
    }
}
