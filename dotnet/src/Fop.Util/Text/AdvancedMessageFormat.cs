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
using System.Text.RegularExpressions;

namespace Fop.Util.Text;

/// <summary>
/// Formats messages based on a template and with a set of named parameters. This is similar to
/// Java's <c>java.text.MessageFormat</c> but uses named parameters and supports conditional
/// sub-groups.
/// <para>Example: <c>Missing field "{fieldName}"[ at location: {location}]!</c></para>
/// <list type="bullet">
///   <item>Curly brackets (<c>{}</c>) are used for fields.</item>
///   <item>Square brackets (<c>[]</c>) are used to delimit conditional sub-groups. A sub-group is
///     conditional when all fields inside the sub-group have a null value. In that case, everything
///     between the brackets is skipped.</item>
/// </list>
/// <para>
/// Port of <c>org.apache.fop.utils.text.AdvancedMessageFormat</c>. Java registered part factories,
/// object formatters and functions through the JAR service-provider mechanism. This port replaces
/// that with explicit registration of the built-in implementations (see <see cref="RegisterDefaults"/>),
/// keeping the registries open for additional registrations (used by <c>Fop.Events</c>).
/// </para>
/// </summary>
public partial class AdvancedMessageFormat
{
#if NET7_0_OR_GREATER
    [GeneratedRegex("(?<!\\\\),")]
    private static partial Regex CommaSeparatorRegexGenerator();

    /// <summary>Regex that matches "," but not "\," (escaped comma).</summary>
    public static Regex CommaSeparatorRegex { get; } = CommaSeparatorRegexGenerator();
#else
    /// <summary>Regex that matches "," but not "\," (escaped comma).</summary>
    public static Regex CommaSeparatorRegex { get; } = new("(?<!\\\\),");
#endif

    private static readonly Dictionary<string, IPartFactory> PartFactories = new();
    private static readonly List<IObjectFormatter> ObjectFormatters = new();
    private static readonly Dictionary<object, IFunction> Functions = new();

    private static readonly object RegistryLock = new();

    private CompositePart rootPart = null!;

    static AdvancedMessageFormat()
    {
        RegisterDefaults();
    }

    /// <summary>
    /// Registers the built-in part factories and object formatters. This replaces the Java
    /// service-provider discovery. Idempotent and safe to call repeatedly.
    /// </summary>
    private static void RegisterDefaults()
    {
        RegisterPartFactory(new IfFieldPart.Factory());
        RegisterPartFactory(new EqualsFieldPart.Factory());
        RegisterPartFactory(new ChoiceFieldPart.Factory());
        RegisterPartFactory(new HexFieldPart.Factory());
        RegisterPartFactory(new GlyphNameFieldPart.Factory());
        RegisterObjectFormatter(new LocatorFormatter());
    }

    /// <summary>Registers a part factory under its <see cref="IPartFactory.Format"/> name.</summary>
    public static void RegisterPartFactory(IPartFactory factory)
    {
        ArgumentNullException.ThrowIfNull(factory);
        lock (RegistryLock)
        {
            PartFactories[factory.Format] = factory;
        }
    }

    /// <summary>Registers an object formatter.</summary>
    public static void RegisterObjectFormatter(IObjectFormatter formatter)
    {
        ArgumentNullException.ThrowIfNull(formatter);
        lock (RegistryLock)
        {
            if (!ObjectFormatters.Contains(formatter))
            {
                ObjectFormatters.Add(formatter);
            }
        }
    }

    /// <summary>Registers a function under its <see cref="IFunction.Name"/>.</summary>
    public static void RegisterFunction(IFunction function)
    {
        ArgumentNullException.ThrowIfNull(function);
        lock (RegistryLock)
        {
            Functions[function.Name] = function;
        }
    }

    /// <summary>Constructs a new message format.</summary>
    /// <param name="pattern">The message format pattern.</param>
    public AdvancedMessageFormat(string pattern)
    {
        ArgumentNullException.ThrowIfNull(pattern);
        ParsePattern(pattern);
    }

    private void ParsePattern(string pattern)
    {
        rootPart = new CompositePart(false);
        var sb = new StringBuilder();
        ParseInnerPattern(pattern, rootPart, sb, 0);
    }

    private int ParseInnerPattern(string pattern, CompositePart parent, StringBuilder sb, int start)
    {
        // assert sb.length() == 0
        int i = start;
        int len = pattern.Length;
        bool loop = true;
        while (loop && i < len)
        {
            char ch = pattern[i];
            switch (ch)
            {
                case '{':
                    if (sb.Length > 0)
                    {
                        parent.AddChild(new TextPart(sb.ToString()));
                        sb.Length = 0;
                    }
                    i++;
                    int nesting = 1;
                    while (i < len)
                    {
                        ch = pattern[i];
                        if (ch == '{')
                        {
                            nesting++;
                        }
                        else if (ch == '}')
                        {
                            nesting--;
                            if (nesting == 0)
                            {
                                i++;
                                break;
                            }
                        }
                        sb.Append(ch);
                        i++;
                    }
                    parent.AddChild(ParseField(sb.ToString()));
                    sb.Length = 0;
                    break;
                case ']':
                    i++;
                    loop = false; // current composite is finished
                    break;
                case '[':
                    if (sb.Length > 0)
                    {
                        parent.AddChild(new TextPart(sb.ToString()));
                        sb.Length = 0;
                    }
                    i++;
                    var composite = new CompositePart(true);
                    parent.AddChild(composite);
                    i += ParseInnerPattern(pattern, composite, sb, i);
                    break;
                case '|':
                    if (sb.Length > 0)
                    {
                        parent.AddChild(new TextPart(sb.ToString()));
                        sb.Length = 0;
                    }
                    parent.NewSection();
                    i++;
                    break;
                case '\\':
                    if (i < len - 1)
                    {
                        i++;
                        ch = pattern[i];
                    }
                    sb.Append(ch);
                    i++;
                    break;
                default:
                    sb.Append(ch);
                    i++;
                    break;
            }
        }
        if (sb.Length > 0)
        {
            parent.AddChild(new TextPart(sb.ToString()));
            sb.Length = 0;
        }
        return i - start;
    }

    private static IPart ParseField(string field)
    {
        string[] parts = SplitOnComma(field, 3);
        string fieldName = parts[0];
        if (parts.Length == 1)
        {
            if (fieldName.StartsWith('#'))
            {
                return new FunctionPart(fieldName[1..]);
            }

            return new SimpleFieldPart(fieldName);
        }

        string format = parts[1];
        IPartFactory? factory;
        lock (RegistryLock)
        {
            PartFactories.TryGetValue(format, out factory);
        }
        if (factory == null)
        {
            throw new ArgumentException("No PartFactory available under the name: " + format);
        }
        if (parts.Length == 2)
        {
            return factory.NewPart(fieldName, null);
        }
        return factory.NewPart(fieldName, parts[2]);
    }

    internal static IFunction? GetFunction(string functionName)
    {
        lock (RegistryLock)
        {
            return Functions.TryGetValue(functionName, out var function) ? function : null;
        }
    }

    /// <summary>Formats a message with the given parameters.</summary>
    /// <param name="parameters">A map of named parameters.</param>
    /// <returns>The formatted message.</returns>
    public string Format(IReadOnlyDictionary<string, object?> parameters)
    {
        var sb = new StringBuilder();
        Format(parameters, sb);
        return sb.ToString();
    }

    /// <summary>Formats a message with the given parameters.</summary>
    /// <param name="parameters">A map of named parameters.</param>
    /// <param name="target">The target string builder to write the formatted message to.</param>
    public void Format(IReadOnlyDictionary<string, object?> parameters, StringBuilder target)
    {
        ArgumentNullException.ThrowIfNull(parameters);
        ArgumentNullException.ThrowIfNull(target);
        rootPart.Write(target, parameters);
    }

    /// <summary>
    /// Formats an object to a string and writes the result to a string builder. This method usually
    /// uses the object's <c>ToString()</c> method unless there is an <see cref="IObjectFormatter"/>
    /// that supports the object.
    /// </summary>
    /// <param name="obj">The object to be formatted.</param>
    /// <param name="target">The target string builder.</param>
    public static void FormatObject(object? obj, StringBuilder target)
    {
        if (obj is string s)
        {
            target.Append(s);
        }
        else
        {
            bool handled = false;
            // Snapshot the registry to avoid holding the lock while formatting.
            IObjectFormatter[] formatters;
            lock (RegistryLock)
            {
                formatters = ObjectFormatters.ToArray();
            }
            foreach (var formatter in formatters)
            {
                if (formatter.SupportsObject(obj))
                {
                    formatter.Format(target, obj);
                    handled = true;
                    break;
                }
            }
            if (!handled)
            {
                // Mirrors Java String.valueOf(obj): null becomes the literal "null".
                target.Append(obj?.ToString() ?? "null");
            }
        }
    }

    /// <summary>
    /// Splits a string around commas not preceded by a backslash, with the Java
    /// <c>Pattern.split(input, limit)</c> semantics (a positive limit caps the number of
    /// resulting segments and trailing empty strings are kept).
    /// </summary>
    internal static string[] SplitOnComma(string input, int limit)
    {
        var result = new List<string>();
        int last = 0;
        foreach (Match match in CommaSeparatorRegex.Matches(input))
        {
            if (limit > 0 && result.Count == limit - 1)
            {
                break;
            }
            result.Add(input.Substring(last, match.Index - last));
            last = match.Index + match.Length;
        }
        result.Add(input[last..]);
        return result.ToArray();
    }

    /// <summary>Unescapes "\," sequences back into a literal comma.</summary>
    public static string UnescapeComma(string value)
    {
        ArgumentNullException.ThrowIfNull(value);
        return value.Replace("\\,", ",");
    }
}
