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
using System.Text;

namespace Fop.Util.Text;

/// <summary>
/// Formats a number or character to a hex value.
/// <para>Port of <c>org.apache.fop.util.text.HexFieldPart</c>.</para>
/// </summary>
public sealed class HexFieldPart(string fieldName) : IPart
{
    private readonly string fieldName = fieldName;

    /// <inheritdoc/>
    public bool IsGenerated(IReadOnlyDictionary<string, object?> parameters)
    {
        parameters.TryGetValue(fieldName, out var obj);
        return obj != null;
    }

    /// <inheritdoc/>
    public void Write(StringBuilder sb, IReadOnlyDictionary<string, object?> parameters)
    {
        if (!parameters.ContainsKey(fieldName))
        {
            throw new ArgumentException(
                "Message pattern contains unsupported field name: " + fieldName);
        }
        parameters.TryGetValue(fieldName, out var obj);
        if (obj is char c)
        {
            // Mirrors Java Integer.toHexString((Character) obj): lowercase, no leading zeros.
            sb.Append(ToHexString(c));
        }
        else if (IsNumber(obj))
        {
            sb.Append(ToHexString(Convert.ToInt32(obj, CultureInfo.InvariantCulture)));
        }
        else
        {
            throw new ArgumentException(
                "Incompatible value for hex field part: " + (obj?.GetType().FullName ?? "null"));
        }
    }

    private static bool IsNumber(object? obj) =>
        obj is sbyte or byte or short or ushort or int or uint or long or ulong
            or float or double or decimal;

    // Java Integer.toHexString treats the value as an unsigned 32-bit integer.
    private static string ToHexString(int value) => Convert.ToString(value, 16);

    /// <inheritdoc/>
    public override string ToString() => "{" + fieldName + ",hex}";

    /// <summary>Factory for <see cref="HexFieldPart"/>.</summary>
    public sealed class Factory : IPartFactory
    {
        /// <inheritdoc/>
        public IPart NewPart(string fieldName, string? values) => new HexFieldPart(fieldName);

        /// <inheritdoc/>
        public string Format => "hex";
    }
}
