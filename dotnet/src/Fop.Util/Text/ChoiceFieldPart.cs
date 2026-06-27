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
using System.Text.RegularExpressions;

namespace Fop.Util.Text;

/// <summary>
/// Defines a "choice" field part that works like <see cref="ChoiceFormat"/>.
/// <para>Port of <c>org.apache.fop.util.text.ChoiceFieldPart</c>.</para>
/// </summary>
public partial class ChoiceFieldPart : IPart
{
#if NET7_0_OR_GREATER
    [GeneratedRegex("\\{([^\\}]+)\\}")]
    private static partial Regex VariableRegexGenerator();

    private static Regex VariableRegex => VariableRegexGenerator();
#else
    private static readonly Regex VariableRegex = new("\\{([^\\}]+)\\}");
#endif

    private readonly string fieldName;
    private readonly ChoiceFormat choiceFormat;

    /// <summary>Creates a new choice part.</summary>
    /// <param name="fieldName">The field name to work on.</param>
    /// <param name="choicesPattern">The choices pattern (as used by <see cref="ChoiceFormat"/>).</param>
    public ChoiceFieldPart(string fieldName, string? choicesPattern)
    {
        this.fieldName = fieldName;
        choiceFormat = new ChoiceFormat(choicesPattern ?? string.Empty);
    }

    /// <inheritdoc/>
    public bool IsGenerated(IReadOnlyDictionary<string, object?> parameters)
    {
        parameters.TryGetValue(fieldName, out var obj);
        return obj != null;
    }

    /// <inheritdoc/>
    public void Write(StringBuilder sb, IReadOnlyDictionary<string, object?> parameters)
    {
        parameters.TryGetValue(fieldName, out var obj);
        double num = Convert.ToDouble(obj, CultureInfo.InvariantCulture);
        string result = choiceFormat.Format(num);
        if (VariableRegex.IsMatch(result))
        {
            // Resolve inner variables.
            var f = new AdvancedMessageFormat(result);
            f.Format(parameters, sb);
        }
        else
        {
            sb.Append(result);
        }
    }

    /// <inheritdoc/>
    public override string ToString() => "{" + fieldName + ",choice, ....}";

    /// <summary>Factory for <see cref="ChoiceFieldPart"/>.</summary>
    public sealed class Factory : IPartFactory
    {
        /// <inheritdoc/>
        public IPart NewPart(string fieldName, string? values) => new ChoiceFieldPart(fieldName, values);

        /// <inheritdoc/>
        public string Format => "choice";
    }
}
