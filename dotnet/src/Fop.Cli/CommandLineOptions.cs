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

namespace Fop.Cli;

/// <summary>
/// The parsed command line for the <c>fop</c> tool: which input drives the run (a ready FO file, or an
/// XML source plus an XSLT stylesheet to transform into FO), where the PDF is written, and the option
/// flags. A pragmatic port of the option handling in <c>org.apache.fop.cli.CommandLineOptions</c>,
/// scoped to the PDF output this port supports.
/// </summary>
internal sealed class CommandLineOptions
{
    /// <summary>The input FO file, or <c>null</c> when an XML+XSLT pair is the source instead.</summary>
    public string? FoFile { get; private set; }

    /// <summary>The input XML data file for an XSLT transform, or <c>null</c>.</summary>
    public string? XmlFile { get; private set; }

    /// <summary>The XSLT stylesheet file, or <c>null</c>.</summary>
    public string? XsltFile { get; private set; }

    /// <summary>The output PDF file, or <c>null</c> when unset.</summary>
    public string? OutputFile { get; private set; }

    /// <summary>Directories to scan and register fonts from (TTF/OTF) before rendering.</summary>
    public IReadOnlyList<string> FontDirectories => fontDirectories;

    private readonly List<string> fontDirectories = new();

    /// <summary>Whether to render with the native (PdfSharp-free) PDF renderer.</summary>
    public bool Native { get; private set; }

    /// <summary>Whether <c>-version</c> was requested.</summary>
    public bool ShowVersion { get; private set; }

    /// <summary>Whether <c>-help</c> was requested (or no arguments were given).</summary>
    public bool ShowHelp { get; private set; }

    /// <summary>
    /// Parses <paramref name="args"/>. On a usage error, <paramref name="error"/> is set and the call
    /// returns <c>null</c>. A <c>-help</c>/<c>-version</c> request returns a populated options object
    /// with the corresponding flag set (and no error).
    /// </summary>
    public static CommandLineOptions? Parse(string[] args, out string? error)
    {
        error = null;
        var o = new CommandLineOptions();
        var positionals = new List<string>();

        if (args.Length == 0)
        {
            o.ShowHelp = true;
            return o;
        }

        for (int i = 0; i < args.Length; i++)
        {
            string arg = args[i];
            switch (arg)
            {
                case "-help" or "-h" or "--help" or "-?":
                    o.ShowHelp = true;
                    return o;

                case "-version" or "-v" or "--version":
                    o.ShowVersion = true;
                    return o;

                case "-fo":
                    if (!TryNext(args, ref i, arg, out string? fo, out error)) { return null; }
                    o.FoFile = fo;
                    break;

                case "-xml":
                    if (!TryNext(args, ref i, arg, out string? xml, out error)) { return null; }
                    o.XmlFile = xml;
                    break;

                case "-xsl" or "-xslt":
                    if (!TryNext(args, ref i, arg, out string? xsl, out error)) { return null; }
                    o.XsltFile = xsl;
                    break;

                case "-pdf":
                    if (!TryNext(args, ref i, arg, out string? pdf, out error)) { return null; }
                    o.OutputFile = pdf;
                    break;

                case "-fontdir" or "-fonts":
                    if (!TryNext(args, ref i, arg, out string? dir, out error)) { return null; }
                    o.fontDirectories.Add(dir!);
                    break;

                case "-native":
                    o.Native = true;
                    break;

                default:
                    if (arg.StartsWith('-'))
                    {
                        error = $"Unknown option: {arg}";
                        return null;
                    }

                    positionals.Add(arg);
                    break;
            }
        }

        // Positional arguments: "<in.fo> [out.pdf]" when -fo/-xml were not used explicitly.
        if (positionals.Count > 0 && o.FoFile is null && o.XmlFile is null)
        {
            o.FoFile = positionals[0];
            if (positionals.Count > 1 && o.OutputFile is null)
            {
                o.OutputFile = positionals[1];
            }

            if (positionals.Count > 2)
            {
                error = "Too many arguments.";
                return null;
            }
        }
        else if (positionals.Count == 1 && o.OutputFile is null)
        {
            // A lone positional alongside -fo/-xml is the output file.
            o.OutputFile = positionals[0];
        }
        else if (positionals.Count > 0)
        {
            error = "Unexpected extra arguments: " + string.Join(' ', positionals);
            return null;
        }

        return Validate(o, out error) ? o : null;
    }

    private static bool Validate(CommandLineOptions o, out string? error)
    {
        error = null;

        bool hasFo = o.FoFile is not null;
        bool hasXml = o.XmlFile is not null || o.XsltFile is not null;

        if (!hasFo && !hasXml)
        {
            error = "No input specified. Provide an FO file (or an -xml/-xsl pair).";
            return false;
        }

        if (hasFo && hasXml)
        {
            error = "Specify either an FO input (-fo) or an XML+XSLT pair (-xml/-xsl), not both.";
            return false;
        }

        if (hasXml && (o.XmlFile is null || o.XsltFile is null))
        {
            error = "An XSLT transform needs both -xml and -xsl.";
            return false;
        }

        if (o.OutputFile is null)
        {
            error = "No output file specified (give a second argument or use -pdf <file>).";
            return false;
        }

        return true;
    }

    private static bool TryNext(string[] args, ref int i, string flag, out string? value, out string? error)
    {
        if (i + 1 >= args.Length)
        {
            value = null;
            error = $"Option {flag} requires a value.";
            return false;
        }

        value = args[++i];
        error = null;
        return true;
    }
}
