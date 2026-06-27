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

using Fop.Colors;
using Fop.Fo.Expr;

using Xunit;

namespace Fop.Fo.Tests;

/// <summary>Parser/evaluator tests for the XSL-FO property expression engine.</summary>
public sealed class ExprEvaluatorTests
{
    /// <summary>A minimal context: a 10pt font size, optional percent base, no property references.</summary>
    private sealed class TestContext(double fontSizeMpt = 10_000, double? percentBaseMpt = null)
        : IExpressionContext
    {
        public double FontSizeMpt => fontSizeMpt;

        public double? PercentBaseMpt => percentBaseMpt;

        public PropertyReference? ResolvePropertyReference(string propertyName, PropertyReferenceKind kind) => null;
    }

    private static ExprValue Eval(string expr, IExpressionContext? context = null) =>
        ExprEvaluator.Evaluate(expr, context ?? new TestContext());

    [Fact]
    public void AddsTwoLengths()
    {
        ExprValue value = Eval("2pt + 3pt");
        Assert.Equal(ExprValueKind.Length, value.Kind);
        Assert.Equal(5_000, value.Value, 3);
    }

    [Fact]
    public void MultipliesNumberByLength()
    {
        ExprValue value = Eval("2 * 3pt");
        Assert.Equal(ExprValueKind.Length, value.Kind);
        Assert.Equal(6_000, value.Value, 3);
    }

    [Fact]
    public void DividesLengthByNumber()
    {
        ExprValue value = Eval("10pt div 2");
        Assert.Equal(ExprValueKind.Length, value.Kind);
        Assert.Equal(5_000, value.Value, 3);
    }

    [Fact]
    public void ModuloOfIntegers()
    {
        ExprValue value = Eval("7 mod 3");
        Assert.Equal(ExprValueKind.Numeric, value.Kind);
        Assert.Equal(1, value.Value, 3);
    }

    [Fact]
    public void RespectsPrecedence()
    {
        // Multiplicative binds tighter than additive: 2 + 3 * 4 = 14.
        Assert.Equal(14, Eval("2 + 3 * 4").Value, 3);
    }

    [Fact]
    public void RespectsParentheses()
    {
        Assert.Equal(20, Eval("(2 + 3) * 4").Value, 3);
    }

    [Fact]
    public void HandlesUnaryMinus()
    {
        Assert.Equal(-5, Eval("-5").Value, 3);
        Assert.Equal(-2_000, Eval("3pt - 5pt").Value, 3);
        Assert.Equal(1_000, Eval("3pt + -2pt").Value, 3);
    }

    [Fact]
    public void MixesUnits()
    {
        // 1in == 72pt == 72000mpt; + 1pc (12pt) == 84000mpt.
        ExprValue value = Eval("1in + 1pc");
        Assert.Equal(ExprValueKind.Length, value.Kind);
        Assert.Equal(84_000, value.Value, 1);
    }

    [Fact]
    public void ResolvesEmAgainstFontSize()
    {
        // 1.2em with a 10pt font == 12pt.
        ExprValue value = Eval("1.2em", new TestContext(fontSizeMpt: 10_000));
        Assert.Equal(12_000, value.Value, 1);
    }

    [Fact]
    public void ResolvesPercentWhenBaseKnown()
    {
        ExprValue value = Eval("50%", new TestContext(percentBaseMpt: 100_000));
        Assert.Equal(ExprValueKind.Length, value.Kind);
        Assert.Equal(50_000, value.Value, 1);
    }

    [Fact]
    public void KeepsPercentWhenBaseUnknown()
    {
        ExprValue value = Eval("50%");
        Assert.Equal(ExprValueKind.Percentage, value.Kind);
        Assert.Equal(50, value.Value, 3);
    }

    [Theory]
    [InlineData("max(2pt, 5pt)", 5_000)]
    [InlineData("min(2pt, 5pt)", 2_000)]
    [InlineData("abs(-3pt)", 3_000)]
    [InlineData("round(2.4)", 2)]
    [InlineData("round(2.6)", 3)]
    [InlineData("ceiling(2.1)", 3)]
    [InlineData("floor(2.9)", 2)]
    public void EvaluatesNumericFunctions(string expr, double expected)
    {
        Assert.Equal(expected, Eval(expr).Value, 3);
    }

    [Fact]
    public void RgbProducesRed()
    {
        ExprValue value = Eval("rgb(255, 0, 0)");
        Assert.Equal(ExprValueKind.Color, value.Kind);
        Assert.Equal(FopColor.FromRgb(255, 0, 0), value.Color);
    }

    [Fact]
    public void RgbIccUsesSrgbFallback()
    {
        ExprValue value = Eval("rgb-icc(0, 128, 255, \"#CMYK\", 0, 0, 0, 1)");
        Assert.Equal(ExprValueKind.Color, value.Kind);
        Assert.Equal(FopColor.FromRgb(0, 128, 255), value.Color);
    }

    [Fact]
    public void HexColorSpecParses()
    {
        ExprValue value = Eval("#f00");
        Assert.Equal(ExprValueKind.Color, value.Kind);
        Assert.Equal(FopColor.FromRgb(255, 0, 0), value.Color);
    }

    [Fact]
    public void DeferredFunctionsReturnStringSentinel()
    {
        Assert.Equal("proportional-column-width(2)", Eval("proportional-column-width(2)").Text);
        Assert.Equal("label-end()", Eval("label-end()").Text);
        Assert.Equal("body-start()", Eval("body-start()").Text);
    }

    [Fact]
    public void DivisionByZeroThrows()
    {
        Assert.Throws<PropertyException>(() => Eval("1pt div 0"));
    }

    [Fact]
    public void UnknownFunctionThrows()
    {
        Assert.Throws<PropertyException>(() => Eval("bogus(1)"));
    }

    [Fact]
    public void MalformedExpressionThrows()
    {
        Assert.Throws<PropertyException>(() => Eval("2pt +"));
        Assert.Throws<PropertyException>(() => Eval("(2pt"));
    }

    [Fact]
    public void EmptyExpressionIsEmptyString()
    {
        ExprValue value = Eval("");
        Assert.Equal(ExprValueKind.String, value.Kind);
        Assert.Equal(string.Empty, value.Text);
    }
}
