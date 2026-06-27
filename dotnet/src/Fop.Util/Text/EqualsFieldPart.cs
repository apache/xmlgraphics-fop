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

namespace Fop.Util.Text;

/// <summary>
/// Defines an "equals" field part that can compare a field's string value against another string.
/// It returns either of two possible values attached as additional part parameters. Example:
/// <c>{field,equals,new,This is new!,This is old!}</c>.
/// <para>Port of <c>org.apache.fop.util.text.EqualsFieldPart</c>.</para>
/// </summary>
public class EqualsFieldPart : IfFieldPart
{
    private string? equalsValue;

    /// <summary>Creates a new "equals" field part.</summary>
    /// <param name="fieldName">The field name.</param>
    /// <param name="values">The unparsed parameter values.</param>
    public EqualsFieldPart(string fieldName, string? values)
        : base(fieldName, values)
    {
    }

    /// <inheritdoc/>
    protected override void ParseValues(string? values)
    {
        string[] parts = AdvancedMessageFormat.SplitOnComma(values ?? string.Empty, 3);
        equalsValue = parts[0];
        if (parts.Length == 1)
        {
            throw new ArgumentException("'equals' format must have at least 2 parameters");
        }
        if (parts.Length == 3)
        {
            IfValue = AdvancedMessageFormat.UnescapeComma(parts[1]);
            ElseValue = AdvancedMessageFormat.UnescapeComma(parts[2]);
        }
        else
        {
            IfValue = AdvancedMessageFormat.UnescapeComma(parts[1]);
        }
    }

    /// <inheritdoc/>
    protected override bool IsTrue(IReadOnlyDictionary<string, object?> parameters)
    {
        parameters.TryGetValue(FieldName, out var obj);
        if (obj != null)
        {
            // Mirrors Java String.valueOf(obj).equals(equalsValue).
            return (obj.ToString() ?? "null").Equals(equalsValue, StringComparison.Ordinal);
        }
        return false;
    }

    /// <inheritdoc/>
    public override string ToString() => "{" + FieldName + ", equals " + equalsValue + "}";

    /// <summary>Part factory for "equals".</summary>
    public new sealed class Factory : IPartFactory
    {
        /// <inheritdoc/>
        public IPart NewPart(string fieldName, string? values) => new EqualsFieldPart(fieldName, values);

        /// <inheritdoc/>
        public string Format => "equals";
    }
}
