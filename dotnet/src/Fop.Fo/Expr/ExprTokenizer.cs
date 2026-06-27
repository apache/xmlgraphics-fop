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

namespace Fop.Fo.Expr;

/// <summary>
/// Tokenizes an XSL-FO property expression into a stream of <see cref="ExprToken"/>s. A modern,
/// idiomatic port of FOP's <c>org.apache.fop.fo.expr.PropertyTokenizer</c> (originally derived from
/// James Clark's XT). Whitespace-insensitive; commas separate function arguments.
/// </summary>
public sealed class ExprTokenizer
{
    private const string NameStartChars = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private const string NameChars = ".-0123456789";

    private readonly string expr;
    private int index;
    private int tokenStart;

    /// <summary>Creates a tokenizer over the given expression string.</summary>
    public ExprTokenizer(string expression)
    {
        ArgumentNullException.ThrowIfNull(expression);
        expr = expression;
    }

    /// <summary>Reads the next token from the input.</summary>
    /// <exception cref="PropertyException">If an unrecognized character is encountered.</exception>
    public ExprToken Next()
    {
        while (true)
        {
            if (index >= expr.Length)
            {
                return new ExprToken(ExprTokenKind.Eof, string.Empty);
            }

            tokenStart = index;
            char c = expr[index++];
            switch (c)
            {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                case ',':
                    return new ExprToken(ExprTokenKind.Comma, ",");
                case '+':
                    return new ExprToken(ExprTokenKind.Plus, "+");
                case '-':
                    return new ExprToken(ExprTokenKind.Minus, "-");
                case '(':
                    return new ExprToken(ExprTokenKind.LPar, "(");
                case ')':
                    return new ExprToken(ExprTokenKind.RPar, ")");
                case '*':
                    return new ExprToken(ExprTokenKind.Multiply, "*");
                case '"':
                case '\'':
                    return ScanLiteral(c);
                case '.':
                    return ScanDecimalPoint();
                case '#':
                    return ScanColor();
                case >= '0' and <= '9':
                    return ScanNumber();
                default:
                    index--;
                    return ScanName();
            }
        }
    }

    private ExprToken ScanLiteral(char quote)
    {
        int close = expr.IndexOf(quote, index);
        if (close < 0)
        {
            throw new PropertyException("missing quote in expression");
        }

        string value = expr[index..close];
        index = close + 1;
        return new ExprToken(ExprTokenKind.Literal, value);
    }

    private ExprToken ScanNumber()
    {
        ScanDigits();
        bool sawDecimal = false;
        if (index < expr.Length && expr[index] == '.')
        {
            index++;
            sawDecimal = true;
            ScanDigits();
        }

        return FinishNumber(sawDecimal);
    }

    private ExprToken ScanDecimalPoint()
    {
        if (index < expr.Length && IsDigit(expr[index]))
        {
            ScanDigits();
            return FinishNumber(sawDecimal: true);
        }

        throw new PropertyException("illegal character '.' in expression");
    }

    private ExprToken FinishNumber(bool sawDecimal)
    {
        if (index < expr.Length && expr[index] == '%')
        {
            index++;
            // The value excludes the trailing '%'.
            return new ExprToken(ExprTokenKind.Percent, expr[tokenStart..(index - 1)]);
        }

        // A unit name may follow the number.
        int unitStart = index;
        ScanName();
        int unitLength = index - unitStart;
        string text = expr[tokenStart..index];
        ExprTokenKind kind = unitLength > 0
            ? ExprTokenKind.Numeric
            : sawDecimal ? ExprTokenKind.Float : ExprTokenKind.Integer;
        return new ExprToken(kind, text, unitLength);
    }

    private ExprToken ScanColor()
    {
        if (index >= expr.Length)
        {
            throw new PropertyException("illegal character '#' in expression");
        }

        int hexStart = index;
        while (index < expr.Length && IsHexDigit(expr[index]))
        {
            index++;
        }

        int len = index - hexStart;
        if (len > 0 && (len % 3 == 0 || len == 8))
        {
            return new ExprToken(ExprTokenKind.ColorSpec, expr[tokenStart..index]);
        }

        // Not a colour after all: an NCName starting with '#'.
        ScanRestOfName();
        return new ExprToken(ExprTokenKind.NCName, expr[tokenStart..index]);
    }

    private ExprToken ScanName()
    {
        if (index < expr.Length && IsNameStartChar(expr[index]))
        {
            ScanRestOfName();
        }

        if (index == tokenStart)
        {
            throw new PropertyException($"illegal character '{expr[index]}' in expression");
        }

        string value = expr[tokenStart..index];
        if (value == "mod")
        {
            return new ExprToken(ExprTokenKind.Mod, value);
        }

        if (value == "div")
        {
            return new ExprToken(ExprTokenKind.Div, value);
        }

        return FollowingParen()
            ? new ExprToken(ExprTokenKind.FunctionLPar, value)
            : new ExprToken(ExprTokenKind.NCName, value);
    }

    private void ScanDigits()
    {
        while (index < expr.Length && IsDigit(expr[index]))
        {
            index++;
        }
    }

    private void ScanRestOfName()
    {
        index++;
        while (index < expr.Length && IsNameChar(expr[index]))
        {
            index++;
        }
    }

    /// <summary>Whether the next non-whitespace character is an opening parenthesis (consuming it).</summary>
    private bool FollowingParen()
    {
        for (int i = index; i < expr.Length; i++)
        {
            switch (expr[i])
            {
                case '(':
                    index = i + 1;
                    return true;
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    break;
                default:
                    return false;
            }
        }

        return false;
    }

    private static bool IsDigit(char c) => c is >= '0' and <= '9';

    private static bool IsHexDigit(char c) =>
        IsDigit(c) || c is (>= 'a' and <= 'f') or (>= 'A' and <= 'F');

    private static bool IsNameStartChar(char c) => NameStartChars.IndexOf(c) >= 0 || c >= 0x80;

    private static bool IsNameChar(char c) =>
        NameStartChars.IndexOf(c) >= 0 || NameChars.IndexOf(c) >= 0 || c >= 0x80;
}
