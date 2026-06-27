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

namespace Fop.Fonts;

/// <summary>
/// Holds information on the name, style and weight of one font.
/// <para>
/// Port of <c>org.apache.fop.fonts.FontTriplet</c>. The Java class is an immutable, comparable,
/// serializable value object; here it is a <c>sealed class</c> with value equality and
/// <see cref="IComparable{T}"/>.
/// </para>
/// <para>
/// Observable behaviour preserved from Java:
/// <list type="bullet">
/// <item><description>
/// <see cref="ToString"/> and the comparison/hash use the combined key
/// <c>name + "," + style + "," + weight</c> (the key is computed lazily and cached, exactly like the
/// Java <c>getKey()</c>).</description></item>
/// <item><description>
/// equality and ordering ignore <see cref="Priority"/> entirely (Java <c>equals</c> compares only
/// name/style/weight, and <c>compareTo</c> compares only the key).</description></item>
/// <item><description>
/// ordering uses ordinal string comparison, matching Java <c>String.compareTo</c>.</description></item>
/// </list>
/// </para>
/// </summary>
public sealed class FontTriplet : IComparable<FontTriplet>, IEquatable<FontTriplet>
{
    /// <summary>The default font triplet ("any", normal, normal weight, default priority).</summary>
    public static readonly FontTriplet DefaultFontTriplet =
        new("any", FontConstants.StyleNormal, FontConstants.WeightNormal);

    // Lazily-computed cache of the combined key, mirroring the Java transient "key" field.
    private string? key;

    /// <summary>
    /// Creates the empty triplet (Java no-arg constructor: <c>this(null, null, 0)</c>).
    /// </summary>
    public FontTriplet()
        : this(null, null, 0)
    {
    }

    /// <summary>
    /// Creates a new font triplet with the default priority.
    /// </summary>
    /// <param name="name">font name.</param>
    /// <param name="style">font style (normal, italic etc.).</param>
    /// <param name="weight">font weight (100, 200, 300...800, 900).</param>
    public FontTriplet(string? name, string? style, int weight)
        : this(name, style, weight, FontConstants.PriorityDefault)
    {
    }

    /// <summary>
    /// Creates a new font triplet.
    /// </summary>
    /// <param name="name">font name.</param>
    /// <param name="style">font style (normal, italic etc.).</param>
    /// <param name="weight">font weight (100, 200, 300...800, 900).</param>
    /// <param name="priority">priority of this triplet/font mapping.</param>
    public FontTriplet(string? name, string? style, int weight, int priority)
    {
        Name = name;
        Style = style;
        Weight = weight;
        Priority = priority;
    }

    /// <summary>Gets the font name.</summary>
    public string? Name { get; }

    /// <summary>Gets the font style.</summary>
    public string? Style { get; }

    /// <summary>Gets the font weight.</summary>
    public int Weight { get; }

    /// <summary>Gets the priority of this triplet/font mapping.</summary>
    public int Priority { get; }

    // Java getKey(): "name,style,weight", cached. Note that null name/style render as the empty
    // string here (C# string concatenation), whereas Java would render the literal "null"; the empty
    // triplet is only used as a sentinel, so this difference is not observable in practice.
    private string Key => key ??= $"{Name},{Style},{Weight}";

    /// <inheritdoc/>
    public int CompareTo(FontTriplet? other)
    {
        if (other is null)
        {
            // Java would throw NullPointerException; .NET convention sorts null first.
            return 1;
        }

        return string.CompareOrdinal(Key, other.Key);
    }

    /// <inheritdoc/>
    public bool Equals(FontTriplet? other)
    {
        if (other is null)
        {
            return false;
        }

        if (ReferenceEquals(this, other))
        {
            return true;
        }

        // Matches Java equals(): name, style and weight only (priority is ignored).
        return Name == other.Name
            && Style == other.Style
            && Weight == other.Weight;
    }

    /// <inheritdoc/>
    public override bool Equals(object? obj) => Equals(obj as FontTriplet);

    /// <inheritdoc/>
    public override int GetHashCode() => ToString().GetHashCode(StringComparison.Ordinal);

    /// <inheritdoc/>
    public override string ToString() => Key;

    public static bool operator ==(FontTriplet? left, FontTriplet? right) =>
        left is null ? right is null : left.Equals(right);

    public static bool operator !=(FontTriplet? left, FontTriplet? right) => !(left == right);

    public static bool operator <(FontTriplet? left, FontTriplet? right) =>
        Compare(left, right) < 0;

    public static bool operator <=(FontTriplet? left, FontTriplet? right) =>
        Compare(left, right) <= 0;

    public static bool operator >(FontTriplet? left, FontTriplet? right) =>
        Compare(left, right) > 0;

    public static bool operator >=(FontTriplet? left, FontTriplet? right) =>
        Compare(left, right) >= 0;

    private static int Compare(FontTriplet? left, FontTriplet? right)
    {
        if (left is null)
        {
            return right is null ? 0 : -1;
        }

        return left.CompareTo(right);
    }

    /// <summary>
    /// Matcher interface for <see cref="FontTriplet"/>.
    /// <para>Port of the nested Java interface <c>FontTriplet.Matcher</c>.</para>
    /// </summary>
    public interface IMatcher
    {
        /// <summary>
        /// Indicates whether the given <see cref="FontTriplet"/> matches a particular criterion.
        /// </summary>
        /// <param name="triplet">the font triplet.</param>
        /// <returns><c>true</c> if the font triplet is a match.</returns>
        bool Matches(FontTriplet triplet);
    }
}
