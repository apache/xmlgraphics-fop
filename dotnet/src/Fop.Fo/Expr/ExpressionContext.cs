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
/// The evaluation context an <see cref="ExprEvaluator"/> consults while resolving an expression:
/// the current font size (for <c>em</c>), an optional percentage base, and a resolver for the
/// property-reference functions (<c>from-parent</c>, <c>from-nearest-specified-value</c>,
/// <c>inherited-property-value</c>). A modern stand-in for the relevant slice of FOP's
/// <c>PropertyInfo</c>/<c>PropertyList</c> context.
/// </summary>
public interface IExpressionContext
{
    /// <summary>The current font size in millipoints, used to resolve the <c>em</c> unit.</summary>
    double FontSizeMpt { get; }

    /// <summary>
    /// The percentage base in millipoints, or <c>null</c> when no base is known (a percentage then
    /// stays a <see cref="ExprValueKind.Percentage"/> for the caller to resolve later).
    /// </summary>
    double? PercentBaseMpt { get; }

    /// <summary>
    /// Resolves a property reference for the <c>from-parent</c>/<c>from-nearest-specified-value</c>/
    /// <c>inherited-property-value</c> functions: returns the raw declared value of
    /// <paramref name="propertyName"/> from the appropriate place in the inheritance chain and a
    /// context against which to (recursively) evaluate it, or <c>null</c> when unavailable.
    /// </summary>
    /// <param name="propertyName">The referenced property name.</param>
    /// <param name="kind">Which flavour of reference is requested.</param>
    PropertyReference? ResolvePropertyReference(string propertyName, PropertyReferenceKind kind);
}

/// <summary>The flavour of a property-reference function.</summary>
public enum PropertyReferenceKind
{
    /// <summary><c>from-parent(prop)</c>: the computed value on the parent FO.</summary>
    FromParent,

    /// <summary><c>from-nearest-specified-value(prop)</c>: the nearest ancestor that specified the property.</summary>
    FromNearestSpecified,

    /// <summary><c>inherited-property-value(prop)</c>: the inherited (parent-computed) value.</summary>
    Inherited,
}

/// <summary>
/// A resolved property reference: the raw value text to evaluate and the context against which it
/// should be (recursively) evaluated.
/// </summary>
/// <param name="RawValue">The raw declared value text.</param>
/// <param name="Context">The context to evaluate <paramref name="RawValue"/> in.</param>
public readonly record struct PropertyReference(string RawValue, IExpressionContext Context);
