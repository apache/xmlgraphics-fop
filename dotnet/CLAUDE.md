# Apache FOP — C# / .NET 10 Port

This directory hosts a modern, idiomatic C# port of [Apache FOP](https://xmlgraphics.apache.org/fop/)
(Formatting Objects Processor): an engine that reads an XSL-FO document and renders it to PDF,
PostScript, AFP, PCL, RTF, plain text and bitmap images.

The Java sources live in the sibling module directories at the repository root
(`fop-core/`, `fop-events/`, `fop-util/`, `fop/`, …). They are the **source of truth** for the
port: every C# type should be traceable to the Java type it derives from.

---

## Scope and reality check

The Java codebase is large: **~2,100 source files / ~384,000 lines** across the modules below.

| Java module        | Java files | Notes                                                          |
|--------------------|-----------:|----------------------------------------------------------------|
| `fop-core`         | ~2,013     | The whole engine: fo tree, layout, area tree, renderers, fonts |
| `fop-events`       | 18         | Event infrastructure (**ported**)                              |
| `fop-util`         | 3          | Small text utilities                                           |
| `fop`              | 41         | CLI front-end, examples, tests                                 |
| `fop-sandbox`      | 22         | Experimental renderers                                         |

A faithful port is a multi-month, incremental effort. This repository is structured so that work
can proceed **bottom-up, one self-contained slice at a time**, each slice built and tested before
the next begins. `TODO.md` tracks the phased plan; the section below records the conventions every
slice must follow.

---

## Solution layout

```
dotnet/
  Fop.slnx                  # solution (new XML format)
  global.json               # pins the .NET 10 SDK
  Directory.Build.props     # shared settings: net10.0, nullable, implicit usings, doc gen
  src/
    Fop.Util/               # dependency-free utilities  (port of fop-util + org.apache.fop.util)
    Fop.Events/             # event broadcasting          (port of fop-events)
    Fop.Core/               # the engine                  (port of fop-core) — in progress
  tests/
    Fop.Util.Tests/         # xUnit
    Fop.Events.Tests/       # xUnit (mirrors fop-events test cases)
```

Dependency direction: `Fop.Util` ← `Fop.Events` ← `Fop.Core`. Nothing depends on `Fop.Core`.

### Build & test

```bash
cd dotnet
dotnet build Fop.slnx
dotnet test  Fop.slnx
```

Requires the .NET 10 SDK (see `global.json`).

---

## Porting conventions

These rules keep the port consistent and reviewable against the Java original.

1. **Namespaces mirror packages.** `org.apache.fop.events` → `Fop.Events`,
   `org.apache.fop.util` → `Fop.Util`, `org.apache.fop.fo.flow` → `Fop.Fo.Flow`, etc.
   Drop the `org.apache` prefix; PascalCase each segment.

2. **One Java type per file**, named the same (interfaces gain the C# `I` prefix:
   `EventListener` → `IEventListener`).

3. **Idiomatic C#, not transliterated Java.** Prefer:
   - Properties over `getX()`/`setX()`.
   - `enum` over the Java "typesafe enum" singleton pattern (see `EventSeverity`).
   - Expression-bodied members, pattern matching, `switch` expressions, collection expressions.
   - Nullable reference types (`Nullable` is enabled solution-wide) — annotate honestly.
   - `IReadOnlyDictionary<,>` / `IReadOnlyList<>` for the Java `Collections.unmodifiable*` views.
   - `System.Threading.Lock` for `synchronized` blocks.
   - `ArgumentNullException.ThrowIfNull` / `ArgumentOutOfRangeException.ThrowIf*` guards.

4. **Preserve observable behaviour.** When Java behaviour is quirky but observable, match it and
   leave a comment (e.g. `CharUtilities.Format` emits lowercase hex because Java's
   `Integer.toString(c,16)` does; `CharUtilities.Rlm` keeps FOP's `U+202F` value).

5. **Keep source files pure ASCII.** Express non-ASCII characters as `'\uXXXX'` escapes, never as
   literal glyphs — invisible/control characters in source are a correctness and review hazard.

6. **Every ported type gets tests.** Where a JUnit test exists, port it (same scenarios, same
   assertions) so equivalence is demonstrable — `Fop.Events.Tests.BasicEventTests` mirrors
   `org.apache.fop.events.BasicEventTestCase`.

---

## Library mapping (Java → modern .NET)

The original FOP leans heavily on Java AWT/Java2D and Apache XML Graphics Commons. The port replaces
that stack with modern, cross-platform, managed libraries:

| Java / Apache dependency                     | .NET replacement                                  |
|----------------------------------------------|---------------------------------------------------|
| `java.awt.image`, `javax.imageio`            | **SixLabors.ImageSharp** (decode/encode/identify) |
| `java.awt.geom`, 2D drawing                  | **SixLabors.ImageSharp.Drawing**                  |
| `java.awt.Font`, font metrics/TrueType/OpenType | **SixLabors.Fonts**                            |
| PDF output writer (`org.apache.fop.render.pdf`) | **PdfSharp** (in `Fop.Render.Pdf`; layout/metrics via its `XGraphics`) |
| `java.util.logging` / commons-logging        | `Microsoft.Extensions.Logging` abstractions       |
| `javax.xml.transform` / SAX / DOM            | `System.Xml` (`XmlReader`, `XDocument`, `XslCompiledTransform`) |
| `java.lang.reflect.Proxy` (dynamic proxies)  | `System.Reflection.DispatchProxy`                 |
| Apache **Batik** (SVG rendering)             | **`Fop.Svg`** — bespoke static-SVG parser → vector primitives |
| Java `ServiceLoader` / `META-INF/services`   | DI registration / `[assembly:]` discovery / explicit registries |
| Resource bundles (`*.properties`/XML bundles)| `System.Resources` / embedded resources           |
| `BigDecimal`, `BitSet`                       | `decimal`, `System.Collections.BitArray`          |

### What has been done so far

- **`Fop.Util`** — `CharUtilities`, `CompareUtil`, and the helper set `StringUtils`, `HexEncoder`,
  `ConversionUtils`, `LanguageTags`, `ListUtil`, `QName`, `UnitConv` (via `System.Numerics.Matrix3x2`),
  `XMLUtil`, plus a `Locale` stand-in. `Fop.Util.Text` holds the `AdvancedMessageFormat` engine
  (with its part handlers and a `ChoiceFormat` port).
- **`Fop.Events`** — full port of the event infrastructure. The Java `event-model.xml` +
  dynamic-proxy mechanism is replaced by `DispatchProxy` plus an `[Event(Severity=…)]` attribute and
  reflection over producer-method parameter names (`DefaultEventBroadcaster`). `EventFormatter`
  renders templates via `AdvancedMessageFormat` over a pluggable `IEventModelMessageSource`;
  `EventExceptionManager` uses an `IExceptionFactory` registry.
- **`Fop.DataTypes`** — the XSL-FO datatype layer: `INumeric`/`ILength`/`IPercentBase` interfaces,
  `LengthBase`, `FODimension`, `KeepValue`, `URISpecification`, percent-base contexts (with a
  temporary `Fop.Fo.Expr.PropertyException` stand-in).
- **`Fop.Traits`** — `MinOptMax` (readonly record struct) and the trait enums `Direction`,
  `WritingMode`, `BorderStyle`, `RuleStyle`, `Visibility` + `ITraitEnum`.
- **`Fop.Configuration`** — `IConfiguration`/`DefaultConfiguration`/`DefaultConfigurationBuilder`
  over `System.Xml`.
- **`Fop.Fonts`** — `FontType`, `FontTriplet`, `FontUtil`, `IFontMetrics`, abstract `Typeface`,
  `EmbeddingMode`, `CodePointMapping` (all 8 built-in single-byte encodings), the `Font` instance
  (width/kerning/char-mapping) and the `FontInfo` registry with fuzzy lookup/fallback.
- **`Fop.Colors`** — the managed `FopColor` model replacing `java.awt.Color`, plus colour
  parsing/serialization (`#hex`, `rgb()`, `cmyk()`, `fop-rgb-icc()` pseudo-profiles, named/system
  colours), `ColorWithFallback`, `OCAColor`/`OCAColorSpace`. Real ICC resolution deferred.
- **`Fop.Hyphenation`** — `Hyphen`, `Hyphenation`, `ByteVector`/`CharVector`, and the `TernaryTree`
  search tree (the SAX pattern loaders are deferred).
- **`Fop.Pdf`** — the low-level PDF object model: `PDFObject` base, `PDFName`, `PDFNumber`,
  `PDFString`/`PDFText`, `PDFArray`, `PDFDictionary`, `PDFReference`, `PDFNull`, with byte-faithful
  serialization and an `IPdfDocument` owner abstraction (full `PDFDocument` and encryption deferred).
- **`Fop.Core`** — project established with the SixLabors dependencies wired in;
  `Fop.Imaging.ImageDimensions` is the first ImageSharp-backed utility (image identify) and the seed
  of the image pipeline.
- **`Fop.Fo`** — the formatting-object layer: `FoLength`, the inheriting `PropertyList` (a modern
  reformulation of FOP's PropertyMaker subsystem over a curated property set), `FONode`/`FObj`/`FOText`,
  the concrete FOs (root, page masters, page-sequence, flow, block, inline), and the
  `FoTreeBuilder` XML parser.
- **`Fop.Layout`** — the area model (`AreaTree`/`PageArea`/`TextRun`), the `IFontMeasurer` contract,
  and a `LayoutEngine` that stacks blocks, breaks lines (greedy), aligns/justifies, and paginates.
- **`Fop.Render.Pdf`** — renders the area tree to PDF via **PdfSharp**, with an embedded-Liberation
  `IFontResolver`, a PdfSharp-backed `IFontMeasurer`, and the high-level `FopProcessor` facade
  (FO in → PDF out).
- **`Fop.Svg`** — a dependency-light SVG parser (replacing Batik) that flattens a static SVG subset
  (basic shapes, `path` with arc→Bezier conversion, `g`/`transform`, presentation attributes + the
  `style` attribute for fill/stroke/opacity, simple `text`) into renderer-neutral vector primitives.
  `Fop.Layout` scales these onto an `fo:instream-foreign-object`'s content box as area-tree
  `VectorPath`s/`TextRun`s; the PdfSharp renderer paints them.
- **`Fop.Render.Pdf.Native`** — a native, **PdfSharp-free** PDF renderer built on the `Fop.Pdf` object
  model: it writes the file structure (objects/xref/trailer) directly and emits pages, text, vector
  graphics, rules/backgrounds, link annotations and the document outline. It **embeds raster images**
  (`Fop.Imaging.RasterImage`: JPEG → DCTDecode pass-through, other formats → FlateDecode RGB with an
  `/SMask` for alpha) and **embeds + subsets TrueType/OpenType fonts** as Type0 composite fonts
  (Identity-H, `CIDFontType2`, `/ToUnicode`) so any Unicode the face covers renders -- not just WinAnsi
  -- with the standard-14 fonts as a metric-compatible fallback (`TrueTypeSubsetter` rebuilds
  `glyf`/`loca` keeping glyph ids; a sample drops ~630 KB → ~80 KB). Content streams are
  FlateDecode-compressed, and the document can be **encrypted** (standard RC4-128 handler with
  owner/user passwords + permissions via `PdfEncryptionOptions`). Exposed via
  `FopProcessor.ConvertNative` and the CLI `-native` flag.
- **`Fop.Render.Text`** — text-family back-ends (plain text, Markdown, HTML) rendered from the FO
  tree's *logical* structure via a shared `DocExtractor` (paragraphs/headings/lists/tables/links/
  images), rather than from the positioned area tree.
- **`Fop.Cli`** — the `fop` command-line front-end: `fop in.fo out.pdf`, `-fo`/`-pdf`, an
  `-xml`/`-xsl` XSLT-to-FO path, `-fontdir` font registration, `-native` (native renderer), the
  `-txt`/`-md`/`-html` output formats (also inferred from the output extension), and `-version`/
  `-help`. Packable as a global .NET tool (`ToolCommandName=fop`).

A **working end-to-end FO→PDF pipeline** exists for a substantial XSL-FO subset:
- block/inline text, fonts, colour, alignment/justification, indents, pagination;
- the **box model** (borders, padding, backgrounds), painted across page breaks, and
  **external-graphic images**;
- **tables** (%/proportional/absolute columns, header/body/footer, per-cell box, column & row
  spanning, row pagination);
- **lists** (`fo:list-block` with provisional label/body geometry, nesting);
- **static content** — running headers/footers via `fo:region-before`/`after` + `fo:static-content`,
  with `fo:page-number`;
- **keeps & breaks** (`break-before`/`after` page/even/odd, `keep-together.within-page`);
- **nesting** — tables and lists lay out inside table cells / list bodies / kept blocks (a unified
  block/table/list layout over one paginating-or-buffered target abstraction);
- **footnotes** (`fo:footnote`/`footnote-body`, rendered at the page bottom with the reserve
  reducing body height) and **`fo:page-number-citation`/`-last`** (forward/backward references
  resolved by a two-pass layout);
- **markers** (`fo:marker`/`retrieve-marker`, e.g. "current chapter" running headers) and
  **side regions** (`fo:region-start`/`end`);
- **hyperlinks** (`fo:basic-link` → PDF internal/external link annotations) and **leaders**
  (`fo:leader` dots/rule fills, e.g. a clickable table of contents);
- a **property-expression evaluator** (`fo.expr`: arithmetic with `div`/`mod`, units, `from-parent`,
  `inherited-property-value`, `max`/`min`/`abs`/`round`, `rgb`, …) feeding `PropertyList`;
- **hyphenation** (Liang algorithm over the ported `TernaryTree`, embedded English patterns) wired
  into line breaking when `hyphenate="true"`;
- **Knuth–Plass total-fit line breaking** (box/glue/penalty model + active-node DP, the algorithm
  FOP itself uses) as the default paragraph breaker;
- **custom-font registration** — register TTF/OTF fonts by family/style (or scan a directory, with
  OpenType `name`-table family detection) so documents use real embedded fonts, Liberation as the
  built-in fallback; the measurer and PdfSharp resolver share one registry;
- **PDF bookmarks** (`fo:bookmark-tree` → a nested, page-targeted document outline);
- **`fo:block-container`** — absolute/fixed/auto positioning and `reference-orientation` rotation
  (90/180/270) via a transform group rendered with PdfSharp transforms;
- **embedded SVG** (`fo:instream-foreign-object`) — a static SVG subset flattened to vector paths and
  painted to PDF (the `Fop.Svg` parser; no Batik dependency);
- **text-decoration** (underline/overline/line-through) painted **over** the glyphs, positioned from
  the font descender/cap-height and coloured by the FO that declares the decoration -- matching FOP's
  `renderTextDecoration` (thickness `descender/8`, underline `baseline + descender/2`, overline
  `baseline - 1.1*capHeight`, line-through `baseline - 0.45*capHeight`); and **letter-spacing**
  (per-glyph tracking between glyphs, `(n-1)` gaps per word, drawn glyph-by-glyph).

The solution has 17 library projects and **992 passing tests** on .NET 10. See `samples/hello.fo`
(a clickable TOC with leaders, links, a marker header, and page-number citations) and
`samples/svg-decoration.fo` (embedded SVG, text-decoration and letter-spacing). The `fop` CLI renders
a document with `fop in.fo out.pdf`.

---

## Architecture of the engine (target design)

FOP's pipeline, which the port reproduces stage by stage:

```
XSL-FO (XML)
   │  SAX/XmlReader parse
   ▼
FO Tree            org.apache.fop.fo[.flow|.pagination|.properties|.expr]  → Fop.Fo.*
   │  property resolution, refinement
   ▼
Layout Managers    org.apache.fop.layoutmgr.*                              → Fop.LayoutManager.*
   │  line/page breaking (Knuth), tables, lists
   ▼
Area Tree          org.apache.fop.area.*                                   → Fop.Area.*
   │
   ▼
Renderers          org.apache.fop.render.{pdf,ps,afp,pcl,rtf,txt,bitmap}   → Fop.Render.*
   │  fonts (Fop.Fonts) + images (Fop.Imaging) + complexscripts
   ▼
PDF / PS / AFP / PCL / RTF / TXT / PNG / TIFF
```

Cross-cutting subsystems: `Fop.Fonts` (font loading/metrics/embedding),
`Fop.ComplexScripts` (bidi & shaping), `Fop.Hyphenation`, `Fop.DataTypes`/`Fop.Traits`
(lengths, colours, percentages), `Fop.Configuration`, `Fop.Events` (done).

See `TODO.md` for the phase ordering and current status.
