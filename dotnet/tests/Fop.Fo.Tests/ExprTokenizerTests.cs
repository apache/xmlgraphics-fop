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

using Fop.Fo.Expr;

using Xunit;

namespace Fop.Fo.Tests;

/// <summary>Tokenizer cases for the XSL-FO expression lexer.</summary>
public sealed class ExprTokenizerTests
{
    private static List<ExprToken> Tokenize(string expr)
    {
        var tokenizer = new ExprTokenizer(expr);
        var tokens = new List<ExprToken>();
        while (true)
        {
            ExprToken token = tokenizer.Next();
            if (token.Kind == ExprTokenKind.Eof)
            {
                break;
            }

            tokens.Add(token);
        }

        return tokens;
    }

    [Fact]
    public void TokenizesIntegerAndFloat()
    {
        Assert.Equal(ExprTokenKind.Integer, Tokenize("42")[0].Kind);
        Assert.Equal(ExprTokenKind.Float, Tokenize("2.5")[0].Kind);
        Assert.Equal(ExprTokenKind.Float, Tokenize(".5")[0].Kind);
    }

    [Fact]
    public void TokenizesNumericWithUnit()
    {
        ExprToken token = Tokenize("12pt")[0];
        Assert.Equal(ExprTokenKind.Numeric, token.Kind);
        Assert.Equal("12", token.NumberPart);
        Assert.Equal("pt", token.UnitPart);

        ExprToken em = Tokenize("1.2em")[0];
        Assert.Equal(ExprTokenKind.Numeric, em.Kind);
        Assert.Equal("em", em.UnitPart);
    }

    [Theory]
    [InlineData("72px")]
    [InlineData("1in")]
    [InlineData("1cm")]
    [InlineData("1pc")]
    [InlineData("2.5mm")]
    public void TokenizesAllUnits(string expr)
    {
        Assert.Equal(ExprTokenKind.Numeric, Tokenize(expr)[0].Kind);
    }

    [Fact]
    public void TokenizesPercent()
    {
        ExprToken token = Tokenize("50%")[0];
        Assert.Equal(ExprTokenKind.Percent, token.Kind);
        Assert.Equal("50", token.Value);
    }

    [Theory]
    [InlineData("#abc")]
    [InlineData("#aabbcc")]
    public void TokenizesColorSpec(string expr)
    {
        Assert.Equal(ExprTokenKind.ColorSpec, Tokenize(expr)[0].Kind);
    }

    [Fact]
    public void TokenizesKeywordOperators()
    {
        List<ExprToken> tokens = Tokenize("10pt div 2 mod 3");
        Assert.Equal(ExprTokenKind.Numeric, tokens[0].Kind);
        Assert.Equal(ExprTokenKind.Div, tokens[1].Kind);
        Assert.Equal(ExprTokenKind.Integer, tokens[2].Kind);
        Assert.Equal(ExprTokenKind.Mod, tokens[3].Kind);
        Assert.Equal(ExprTokenKind.Integer, tokens[4].Kind);
    }

    [Fact]
    public void TokenizesOperatorsAndParens()
    {
        List<ExprToken> tokens = Tokenize("(2 + 3) * 4 - 1");
        Assert.Equal(ExprTokenKind.LPar, tokens[0].Kind);
        Assert.Equal(ExprTokenKind.Plus, tokens[2].Kind);
        Assert.Equal(ExprTokenKind.RPar, tokens[4].Kind);
        Assert.Equal(ExprTokenKind.Multiply, tokens[5].Kind);
        Assert.Equal(ExprTokenKind.Minus, tokens[7].Kind);
    }

    [Fact]
    public void TokenizesFunctionCallAndCommas()
    {
        List<ExprToken> tokens = Tokenize("rgb(255, 0, 0)");
        Assert.Equal(ExprTokenKind.FunctionLPar, tokens[0].Kind);
        Assert.Equal("rgb", tokens[0].Value);
        Assert.Equal(ExprTokenKind.Comma, tokens[2].Kind);
    }

    [Fact]
    public void TokenizesNCNameVersusFunction()
    {
        Assert.Equal(ExprTokenKind.NCName, Tokenize("bold")[0].Kind);
        Assert.Equal(ExprTokenKind.FunctionLPar, Tokenize("body-start()")[0].Kind);
    }

    [Fact]
    public void TokenizesQuotedLiteral()
    {
        ExprToken token = Tokenize("\"font-size\"")[0];
        Assert.Equal(ExprTokenKind.Literal, token.Kind);
        Assert.Equal("font-size", token.Value);
    }

    [Fact]
    public void IsWhitespaceInsensitive()
    {
        Assert.Equal(3, Tokenize("2pt+3pt".Replace("+", " + ")).Count);
        Assert.Equal(3, Tokenize("2pt + 3pt").Count);
    }
}
