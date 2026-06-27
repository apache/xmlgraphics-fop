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

using System.Text;

namespace Fop.Util.Text;

/// <summary>
/// Defines an "if" field part that checks if a field's value is true or false. It returns either of
/// two possible values attached as additional part parameters. Example: <c>{field,if,Yes,No}</c>.
/// <para>Port of <c>org.apache.fop.util.text.IfFieldPart</c>.</para>
/// </summary>
public class IfFieldPart : IPart
{
    /// <summary>The field name for the part.</summary>
    protected string FieldName { get; }

    /// <summary>The value returned if the field is true.</summary>
    protected string? IfValue { get; set; }

    /// <summary>The value returned if the field is false.</summary>
    protected string? ElseValue { get; set; }

    /// <summary>Creates a new "if" field part.</summary>
    /// <param name="fieldName">The field name.</param>
    /// <param name="values">The unparsed parameter values.</param>
    public IfFieldPart(string fieldName, string? values)
    {
        FieldName = fieldName;
        ParseValues(values);
    }

    /// <summary>Parses the parameter values.</summary>
    /// <param name="values">The unparsed parameter values.</param>
    protected virtual void ParseValues(string? values)
    {
        string[] parts = AdvancedMessageFormat.SplitOnComma(values ?? string.Empty, 2);
        if (parts.Length == 2)
        {
            IfValue = AdvancedMessageFormat.UnescapeComma(parts[0]);
            ElseValue = AdvancedMessageFormat.UnescapeComma(parts[1]);
        }
        else
        {
            IfValue = AdvancedMessageFormat.UnescapeComma(values ?? string.Empty);
        }
    }

    /// <inheritdoc/>
    public void Write(StringBuilder sb, IReadOnlyDictionary<string, object?> parameters)
    {
        if (IsTrue(parameters))
        {
            sb.Append(IfValue);
        }
        else if (ElseValue != null)
        {
            sb.Append(ElseValue);
        }
    }

    /// <summary>
    /// Indicates whether the field's value is true. If the field is not a boolean, it is true if
    /// the field is not null.
    /// </summary>
    /// <param name="parameters">The message parameters.</param>
    /// <returns>The field's value as a boolean.</returns>
    protected virtual bool IsTrue(IReadOnlyDictionary<string, object?> parameters)
    {
        parameters.TryGetValue(FieldName, out var obj);
        if (obj is bool b)
        {
            return b;
        }
        return obj != null;
    }

    /// <inheritdoc/>
    public bool IsGenerated(IReadOnlyDictionary<string, object?> parameters)
        => IsTrue(parameters) || ElseValue != null;

    /// <inheritdoc/>
    public override string ToString() => "{" + FieldName + ", if...}";

    /// <summary>Part factory for "if".</summary>
    public sealed class Factory : IPartFactory
    {
        /// <inheritdoc/>
        public IPart NewPart(string fieldName, string? values) => new IfFieldPart(fieldName, values);

        /// <inheritdoc/>
        public string Format => "if";
    }
}
