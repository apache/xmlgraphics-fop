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
/// The XSL-FO expression function set, ported from the function classes in
/// <c>org.apache.fop.fo.expr</c>. Numeric/colour functions resolve fully; the context-and-
/// layout-dependent functions (<c>proportional-column-width</c>, <c>label-end</c>, <c>body-start</c>,
/// <c>from-table-column</c>) are recognised and re-emitted as a canonical <see cref="ExprValueKind.String"/>
/// sentinel so the existing layout-side string parsing keeps handling them.
/// </summary>
internal static class ExprFunctions
{
    /// <summary>
    /// The names of the functions that this evaluator deliberately does not resolve, but instead
    /// re-emits verbatim as a string sentinel for the existing layout-side handling.
    /// </summary>
    internal static readonly IReadOnlySet<string> DeferredFunctions = new HashSet<string>(StringComparer.Ordinal)
    {
        "proportional-column-width",
        "label-end",
        "body-start",
        "from-table-column",
    };

    internal static ExprValue Eval(string name, IReadOnlyList<ExprValue> args, IExpressionContext context)
    {
        switch (name)
        {
            case "max":
                Expect(name, args, 2);
                return args[0].Value >= args[1].Value ? args[0] : args[1];
            case "min":
                Expect(name, args, 2);
                return args[0].Value <= args[1].Value ? args[0] : args[1];
            case "abs":
                Expect(name, args, 1);
                return WithMagnitude(args[0], Math.Abs(args[0].Value));
            case "round":
                Expect(name, args, 1);
                return WithMagnitude(args[0], Math.Round(args[0].Value, MidpointRounding.AwayFromZero));
            case "ceiling":
                Expect(name, args, 1);
                return WithMagnitude(args[0], Math.Ceiling(args[0].Value));
            case "floor":
                Expect(name, args, 1);
                return WithMagnitude(args[0], Math.Floor(args[0].Value));
            case "rgb":
                Expect(name, args, 3);
                return Rgb(args);
            case "rgb-icc":
            case "fop-rgb-icc":
                return RgbIcc(name, args);
            case "system-color":
                Expect(name, args, 1);
                return SystemColor(args[0]);

            case "from-parent":
                return PropertyReferenceValue(name, args, context, PropertyReferenceKind.FromParent);
            case "from-nearest-specified-value":
                return PropertyReferenceValue(name, args, context, PropertyReferenceKind.FromNearestSpecified);
            case "inherited-property-value":
                return PropertyReferenceValue(name, args, context, PropertyReferenceKind.Inherited);

            case "proportional-column-width":
            case "label-end":
            case "body-start":
            case "from-table-column":
                return Deferred(name, args);

            default:
                throw new PropertyException($"no such function: {name}");
        }
    }

    /// <summary>Returns a value with the magnitude replaced, keeping the length/number kind.</summary>
    private static ExprValue WithMagnitude(ExprValue source, double magnitude) => source.Kind switch
    {
        ExprValueKind.Length => ExprValue.FromLength(magnitude),
        ExprValueKind.Percentage => ExprValue.FromPercentage(magnitude),
        ExprValueKind.Numeric => ExprValue.FromNumber(magnitude),
        _ => throw new PropertyException("expected a numeric argument"),
    };

    private static ExprValue Rgb(IReadOnlyList<ExprValue> args)
    {
        int r = ColorComponent(args[0]);
        int g = ColorComponent(args[1]);
        int b = ColorComponent(args[2]);
        return ExprValue.FromColor(FopColor.FromRgb(r, g, b));
    }

    private static ExprValue RgbIcc(string name, IReadOnlyList<ExprValue> args)
    {
        // The first three args are the sRGB fallback; the remainder name an ICC profile we don't
        // resolve here, so we use the sRGB fallback (documented behaviour).
        if (args.Count < 3)
        {
            throw new PropertyException($"{name} expects at least 3 arguments");
        }

        int r = ColorComponent(args[0]);
        int g = ColorComponent(args[1]);
        int b = ColorComponent(args[2]);
        return ExprValue.FromColor(FopColor.FromRgb(r, g, b));
    }

    private static ExprValue SystemColor(ExprValue arg)
    {
        string name = arg.Text ?? arg.ToString();
        FopColor? color = ColorUtil.ParseColorString(null, $"system-color({name})");
        if (color is null)
        {
            throw new PropertyException($"unknown system colour '{name}'");
        }

        return ExprValue.FromColor(color);
    }

    private static ExprValue PropertyReferenceValue(
        string name, IReadOnlyList<ExprValue> args, IExpressionContext context, PropertyReferenceKind kind)
    {
        Expect(name, args, 1);
        string property = args[0].Text ?? args[0].ToString();
        PropertyReference? reference = context.ResolvePropertyReference(property, kind);
        if (reference is not { } resolved)
        {
            throw new PropertyException($"cannot resolve {name}('{property}')");
        }

        // Recursively evaluate the referenced raw value in its own context.
        return ExprEvaluator.Evaluate(resolved.RawValue, resolved.Context);
    }

    /// <summary>
    /// Re-emits a deferred function call as a canonical string sentinel, e.g.
    /// <c>proportional-column-width(2)</c>, so the existing layout-side string parsing handles it.
    /// </summary>
    private static ExprValue Deferred(string name, IReadOnlyList<ExprValue> args)
    {
        if (args.Count == 0)
        {
            return ExprValue.FromString($"{name}()");
        }

        string inner = string.Join(",", args.Select(a => a.ToString()));
        return ExprValue.FromString($"{name}({inner})");
    }

    private static int ColorComponent(ExprValue value)
    {
        // 0..255, or a percentage 0%..100% mapped to 0..255.
        double v = value.Kind == ExprValueKind.Percentage ? value.Value / 100.0 * 255.0 : value.Value;
        return Math.Clamp((int)Math.Round(v, MidpointRounding.AwayFromZero), 0, 255);
    }

    private static void Expect(string name, IReadOnlyList<ExprValue> args, int count)
    {
        if (args.Count != count)
        {
            throw new PropertyException(
                $"{name} expects {count} argument(s), got {args.Count.ToString(CultureInfo.InvariantCulture)}");
        }
    }
}
