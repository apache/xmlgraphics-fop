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

using System.Reflection;
using System.Xml;
using System.Xml.Xsl;
using Fop.Render.Pdf;

namespace Fop.Cli;

/// <summary>
/// Entry point for the <c>fop</c> command-line tool. Reads an XSL-FO document (or an XML source
/// transformed through an XSLT stylesheet) and renders it to a PDF file, mirroring the basic usage of
/// the Java <c>org.apache.fop.cli.Main</c>.
/// </summary>
public static class Program
{
    /// <summary>Process entry point. Returns 0 on success, 1 on a usage or processing error.</summary>
    public static int Main(string[] args)
    {
        CommandLineOptions? options = CommandLineOptions.Parse(args, out string? error);

        if (error is not null)
        {
            Console.Error.WriteLine("fop: " + error);
            Console.Error.WriteLine();
            PrintUsage(Console.Error);
            return 1;
        }

        if (options is null)
        {
            return 1;
        }

        if (options.ShowVersion)
        {
            Console.WriteLine("fop (Apache FOP C# port) " + VersionString());
            return 0;
        }

        if (options.ShowHelp)
        {
            PrintUsage(Console.Out);
            return 0;
        }

        try
        {
            Run(options);
            Console.WriteLine($"Wrote {options.OutputFile}");
            return 0;
        }
        catch (FileNotFoundException ex)
        {
            Console.Error.WriteLine($"fop: input file not found: {ex.FileName ?? ex.Message}");
            return 1;
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("fop: " + ex.Message);
            return 1;
        }
    }

    private static void Run(CommandLineOptions options)
    {
        var processor = new FopProcessor();
        foreach (string dir in options.FontDirectories)
        {
            if (Directory.Exists(dir))
            {
                processor.RegisterFontsDirectory(dir);
            }
            else
            {
                Console.Error.WriteLine($"fop: warning: font directory not found: {dir}");
            }
        }

        using FileStream output = File.Create(options.OutputFile!);

        if (options.FoFile is not null)
        {
            if (!File.Exists(options.FoFile))
            {
                throw new FileNotFoundException("FO file not found.", options.FoFile);
            }

            using FileStream input = File.OpenRead(options.FoFile);
            if (options.Native)
            {
                processor.ConvertNative(input, output);
            }
            else
            {
                processor.Convert(input, output);
            }
        }
        else
        {
            // XML + XSLT: transform to FO in memory, then render that.
            using var foStream = new MemoryStream();
            Transform(options.XmlFile!, options.XsltFile!, foStream);
            foStream.Position = 0;
            if (options.Native)
            {
                processor.ConvertNative(foStream, output);
            }
            else
            {
                processor.Convert(foStream, output);
            }
        }
    }

    /// <summary>Runs the XSLT <paramref name="xsltFile"/> over <paramref name="xmlFile"/> into <paramref name="output"/>.</summary>
    private static void Transform(string xmlFile, string xsltFile, Stream output)
    {
        if (!File.Exists(xmlFile))
        {
            throw new FileNotFoundException("XML file not found.", xmlFile);
        }

        if (!File.Exists(xsltFile))
        {
            throw new FileNotFoundException("XSLT file not found.", xsltFile);
        }

        var xslt = new XslCompiledTransform();
        xslt.Load(xsltFile);

        using XmlReader xml = XmlReader.Create(xmlFile);
        // Leave the writer open so the caller can rewind and read the produced FO.
        using var writer = XmlWriter.Create(output, new XmlWriterSettings { CloseOutput = false });
        xslt.Transform(xml, writer);
    }

    private static string VersionString()
    {
        Version? v = Assembly.GetExecutingAssembly().GetName().Version;
        return v is null ? "0.1.0" : $"{v.Major}.{v.Minor}.{v.Build}";
    }

    internal static void PrintUsage(TextWriter w)
    {
        w.WriteLine("Usage: fop <input.fo> <output.pdf>");
        w.WriteLine("       fop -fo <input.fo> -pdf <output.pdf>");
        w.WriteLine("       fop -xml <data.xml> -xsl <style.xsl> -pdf <output.pdf>");
        w.WriteLine();
        w.WriteLine("Options:");
        w.WriteLine("  -fo <file>        XSL-FO input document");
        w.WriteLine("  -xml <file>       XML source to transform (with -xsl) into XSL-FO");
        w.WriteLine("  -xsl <file>       XSLT stylesheet that produces XSL-FO from the -xml source");
        w.WriteLine("  -pdf <file>       PDF output file");
        w.WriteLine("  -fontdir <dir>    register all TTF/OTF fonts in <dir> (repeatable)");
        w.WriteLine("  -native           use the native (PdfSharp-free) PDF renderer");
        w.WriteLine("  -version          print the version and exit");
        w.WriteLine("  -help             print this help and exit");
    }
}
