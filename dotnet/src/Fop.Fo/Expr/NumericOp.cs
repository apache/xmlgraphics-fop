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
/// The arithmetic over <see cref="ExprValue"/>s, mirroring FOP's <c>NumericOp</c>. Length and number
/// mix per the XSL-FO rules: length +/- length -> length; length * number (or number * length) ->
/// length; length div number -> length; number op number -> number. Modulo operates on plain
/// numbers.
/// </summary>
internal static class NumericOp
{
    internal static ExprValue Add(ExprValue a, ExprValue b)
    {
        EnsureNumeric(a, "addition");
        EnsureNumeric(b, "addition");
        bool length = a.IsLength || b.IsLength;
        double sum = a.Value + b.Value;
        return length ? ExprValue.FromLength(sum) : ExprValue.FromNumber(sum);
    }

    internal static ExprValue Subtract(ExprValue a, ExprValue b)
    {
        EnsureNumeric(a, "subtraction");
        EnsureNumeric(b, "subtraction");
        bool length = a.IsLength || b.IsLength;
        double diff = a.Value - b.Value;
        return length ? ExprValue.FromLength(diff) : ExprValue.FromNumber(diff);
    }

    internal static ExprValue Multiply(ExprValue a, ExprValue b)
    {
        EnsureNumeric(a, "multiplication");
        EnsureNumeric(b, "multiplication");
        if (a.IsLength && b.IsLength)
        {
            throw new PropertyException("cannot multiply two lengths");
        }

        bool length = a.IsLength || b.IsLength;
        double product = a.Value * b.Value;
        return length ? ExprValue.FromLength(product) : ExprValue.FromNumber(product);
    }

    internal static ExprValue Divide(ExprValue a, ExprValue b)
    {
        EnsureNumeric(a, "division");
        EnsureNumeric(b, "division");
        if (b.Value == 0)
        {
            throw new PropertyException("division by zero");
        }

        // length div length -> number (a ratio); length div number -> length; number div number -> number.
        if (a.IsLength && b.IsLength)
        {
            return ExprValue.FromNumber(a.Value / b.Value);
        }

        bool length = a.IsLength;
        double quotient = a.Value / b.Value;
        return length ? ExprValue.FromLength(quotient) : ExprValue.FromNumber(quotient);
    }

    internal static ExprValue Modulo(ExprValue a, ExprValue b)
    {
        // FOP's modulo operates on plain numbers.
        double left = a.AsNumber();
        double right = b.AsNumber();
        if (right == 0)
        {
            throw new PropertyException("modulo by zero");
        }

        return ExprValue.FromNumber(left % right);
    }

    internal static ExprValue Negate(ExprValue a)
    {
        EnsureNumeric(a, "unary minus");
        return a.IsLength ? ExprValue.FromLength(-a.Value) : ExprValue.FromNumber(-a.Value);
    }

    private static void EnsureNumeric(ExprValue value, string operation)
    {
        if (value.Kind is not (ExprValueKind.Numeric or ExprValueKind.Length or ExprValueKind.Percentage))
        {
            throw new PropertyException($"non-numeric operand in {operation}");
        }
    }
}
