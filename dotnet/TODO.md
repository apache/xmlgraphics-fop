# Port TODO — Apache FOP → C# / .NET 10

Status legend: `[x]` done · `[~]` in progress · `[ ]` not started.

The port proceeds **bottom-up**: each phase depends only on phases above it, and each item should
land with passing tests before the next begins. Line counts are the Java LOC of the corresponding
package, as a rough size signal.

---

## Phase 0 — Foundation & scaffolding  `[x]`

- [x] Analyse the Java codebase (modules, package map, dependency stack, LOC).
- [x] Create `dotnet/` solution: `global.json` (.NET 10), `Directory.Build.props`, `Fop.slnx`.
- [x] Establish projects: `Fop.Util`, `Fop.Events`, `Fop.Core`, test projects.
- [x] Wire modern libraries: SixLabors.ImageSharp / .Drawing / Fonts.
- [x] Author `CLAUDE.md` (conventions + library mapping) and this `TODO.md`.

## Phase 1 — Utilities & events  `[~]`

- [x] `Fop.Util.CharUtilities` (+ `CharClass`) — whitespace/codepoint helpers.
- [x] `Fop.Util.CompareUtil` — equals/hashCode helpers with Java `double` semantics.
- [x] `Fop.Events` — `Event`, `EventSeverity`, `IEventListener`, `IEventBroadcaster`,
      `CompositeEventListener`, `DefaultEventBroadcaster` (DispatchProxy producers), `EventException`.
- [x] Tests: `BasicEventTests` (mirrors `BasicEventTestCase`), `CharUtilitiesTests`, `CompareUtilTests`.
- [x] `org.apache.fop.util` helpers: `StringUtils`, `HexEncoder`, `ListUtil`, `LanguageTags`,
      `ConversionUtils`, `XMLUtil`, `QName`, `UnitConv` (+ a `Locale` stand-in). `UnitConv` uses
      `System.Numerics.Matrix3x2` in place of `AffineTransform`.
- [x] `fop-util` text: `AdvancedMessageFormat` (+ parts: if/equals/choice/hex/glyph-name/locator
      and a `java.text.ChoiceFormat` port) in `Fop.Util.Text`. Service-loader registration replaced
      with explicit `RegisterPartFactory`/`RegisterObjectFormatter`/`RegisterFunction`.
- [x] `Fop.Events` extras: `EventFormatter` (template rendering via `AdvancedMessageFormat` with a
      pluggable `IEventModelMessageSource` replacing Java XML resource bundles) and
      `EventExceptionManager` (`IExceptionFactory` registry).
- [ ] More `org.apache.fop.util`: `ColorUtil`, `DataURLUtil`, `LogUtil`, `ContentHandlerFactory*`,
      color helpers.
- [ ] Replace the `StringUtils.ISoftHyphenSupport`, `XMLUtil` SAX, and
      `GlyphNameFieldPart.GlyphNameResolver` TODOs once the painter/SAX/glyph layers are ported.

## Phase 2 — Data types & traits  `[~]`  (~2,500 LOC)

- [x] `Fop.DataTypes`: `INumeric`, `ILength`, `IPercentBase`, `IPercentBaseContext`,
      `ICompoundDatatype`, `LengthBase`, `FODimension`, `KeepValue`, `URISpecification`,
      `SimplePercentBaseContext`, `ValidationPercentBaseContext` (+ temporary
      `Fop.Fo.Expr.PropertyException` stand-in).
- [x] `Fop.Traits`: `MinOptMax` (readonly record struct), `Direction`, `WritingMode`,
      `BorderStyle`, `RuleStyle`, `Visibility`, `ITraitEnum`.
- [ ] `Fop.Traits` remainder: `BorderProps`, `SpaceVal`, `WritingModeTraits*` (need the property
      layer; colour + length now available via `Fop.Colors` / `Fop.DataTypes`).
- [x] Colour handling — `Fop.Colors`: managed `FopColor` model + `ColorUtil` parsing/serialization,
      `ColorWithFallback`, `OCAColor`/`OCAColorSpace`. (ICC/named-profile/CIE-Lab resolution deferred
      until `FOUserAgent` is ported.)

## Phase 3 — Fonts  `[~]`  (~26,000 LOC)

- [x] `Fop.Fonts` core: `Font`, `FontInfo`, `IFontMetrics`, `Typeface`, `FontTriplet`, `FontType`,
      `FontUtil`, `EmbeddingMode`, `CodePointMapping` (8 built-in single-byte encodings),
      `ISingleByteEncoding`.
- [ ] `LazyFont`, `CustomFont`/`CustomFontMetricsMapper`, `CIDFont`, `MultiByteFont`, `FontReader`,
      `FontCache`, `FontManager`, `FontDetector` — the loading/instantiation machinery.
- [ ] TrueType/OpenType/Type1 loading & metrics via **SixLabors.Fonts** (replacing FOP's own parsers
      where practical; port the parsers where embedding needs raw tables).
- [x] Custom-font registration in `Fop.Render.Pdf` (register TTF/OTF by family/style or scan a
      directory with OpenType name-table detection; PdfSharp embeds them; Liberation fallback).
- [ ] Font subsetting hooks for the PDF/PS/AFP renderers (PdfSharp embeds full faces today).
- [ ] `FontEventListener` wiring (currently stubbed in `Typeface`/`FontInfo`).
- [ ] `Fop.ComplexScripts` (bidi + shaping; `IPositionable`/`ISubstitutable` stand-ins exist) — large;
      can be deferred behind a feature flag.

## Phase 4 — FO tree  `[~]`  (~44,000 LOC)

- [x] `Fop.Fo` core: `FONode`, `FObj`, `FOText`, a modern inheriting `PropertyList`, `FoLength`,
      and `FoTreeBuilder` (XmlReader). Concrete FOs: root, layout-master-set, simple-page-master,
      region-body, page-sequence, flow, block, inline.
- [x] `Fop.Fo.Expr` property-expression evaluator (arithmetic with `div`/`mod`, unit math,
      `from-parent`/`from-nearest-specified-value`/`inherited-property-value`, `max`/`min`/`abs`/
      `round`/`ceiling`/`floor`, `rgb`/`system-color`), gated into `PropertyList`.
- [~] Full property subsystem (the remaining ~290 properties, shorthands, refinement) — a curated
      subset is resolved in `PropertyList`, backed by the expression evaluator and a **shorthand
      expansion layer** (`PropertyShorthands`): `margin`, `size`, `font`, `background`,
      `page-break-before/after/inside` and `white-space` expand to their longhands, with the cascade
      longhand-on-element &gt; shorthand-on-element &gt; inherited. (border/padding shorthands are
      handled by `BoxPropertyResolver`.) Remaining: the full ~290-property maker set, corresponding
      writing-mode-relative property mapping, and validation.
- [ ] Remaining flow/pagination/table FOs; `FOEventHandler`, validation.

## Phase 5 — Area tree & layout  `[~]`  (~46,000 LOC)

- [x] `Fop.Layout` — flat area model (`AreaTree`/`PageArea`/`TextRun`/`RectFill`), `IFontMeasurer`,
      and a `LayoutEngine` doing block stacking, greedy line breaking, alignment/justification,
      nested-block indent, and pagination.
- [x] **Tables** (`fo:table` + column/header/body/footer/row/cell): %/proportional/absolute column
      widths, row heights, per-cell box model, column spanning, **row spanning**, row-level
      pagination with best-effort header repetition.
- [x] Box model (borders/padding/backgrounds), painted **across page breaks**, + `fo:external-graphic`.
- [x] **Lists** (`fo:list-block`/item/label/body) with provisional label/body geometry and nesting.
- [x] **Static content / regions**: `fo:region-before`/`after` bands + `fo:static-content` running
      headers/footers + `fo:page-number`.
- [x] **Keeps & breaks**: `break-before`/`break-after` (page/column/even-page/odd-page),
      `keep-together.within-page` (move-whole-to-next-page).
- [x] **Nesting** — tables and lists lay out inside table cells / list bodies / kept blocks (unified
      block/table/list layout over the paginating-or-buffered target abstraction). Kept blocks taller
      than a page now split instead of overflowing.
- [x] **Footnotes** (`fo:footnote`/`footnote-body`): body at the page bottom with a separator; the
      footnote reserve reduces body height.
- [x] **`fo:page-number-citation`/`-last`**: forward/backward `ref-id` references resolved via a
      two-pass layout (unknown ref-ids → "?").
- [x] **Knuth–Plass total-fit** line breaking (box/glue/penalty + active-node DP) as the default.
- [x] **`fo:block-container`** — absolute/fixed/auto positioning + `reference-orientation` rotation
      (transform group in the area tree, PdfSharp transforms in the renderer).
- [x] **PDF bookmarks** (`fo:bookmark-tree` → document outline).
- [x] **ids inside buffered contexts** (table cells, list items, footnote bodies) are recorded against
      the page their buffer is placed on, so `fo:page-number-citation` to such content resolves
      (previously rendered "?").
- [ ] Total-fit *page* breaking, floats, intra-row splitting; rotated-group link annotations. (Residual
      approximations: footnote reserve is greedy not iterative; a row-spanning cell crossing a page
      break paints on its origin page only; citation-last equals the single recorded page under the flat
      area model.)

## Phase 6 — Renderers  `[ ]`  (~79,000 LOC)

Independent back-ends; port in priority order. Each can be its own project (`Fop.Render.Pdf`, …).

- [ ] `Fop.Render` shared: `Renderer`, `IFDocumentHandler` (intermediate format), painter abstractions.
- [~] **PDF** — two tracks:
      - `Fop.Render.Pdf` renders the area tree to PDF via **PdfSharp** today (text, fonts, colour,
        rects) and exposes the `FopProcessor` facade — a working FO→PDF path.
      - `Fop.Pdf` is a from-scratch port of FOP's low-level PDF object model (PDFObject/Name/Number/
        String/Array/Dictionary/Reference/Null + serialization).
      - **`Fop.Render.Pdf.Native`** is a native, PdfSharp-free renderer built on that model: it writes
        the file structure (objects/xref/trailer) directly and emits pages, text, vector graphics,
        rules/backgrounds, link annotations and the document outline. It **embeds raster images**
        (`Fop.Imaging.RasterImage`: JPEG pass-through as DCTDecode, other formats decoded to FlateDecode
        RGB + an `/SMask` for alpha) and **embeds TrueType/OpenType fonts** (a self-contained
        `TrueTypeFont` parser reads `cmap`/`hmtx`/`head`/`hhea`/`OS-2`/`post` for the `/Widths` and
        descriptor; the face is embedded as `/FontFile2`), falling back to a metric-compatible
        standard-14 font when no program is available. Exposed via `FopProcessor.ConvertNative` and the
        CLI `-native` flag; PdfSharp's reader re-opens the output. Remaining: font **subsetting** (full
        faces are embedded today), CID/Unicode fonts beyond WinAnsi, encryption, stream filters.
- [ ] Bitmap/Java2D → **ImageSharp** raster renderer (`Fop.Render.Bitmap`).
- [x] **Text-family back-ends** (`Fop.Render.Text`): plain text, Markdown and HTML, rendered from the
      FO tree's logical structure (a shared `DocExtractor` → paragraphs/headings/lists/tables/links/
      images model) rather than the positioned area tree. Wired into the CLI via the output extension
      or the `-txt`/`-md`/`-html` flags.
- [ ] PostScript, AFP, PCL, RTF, intermediate XML — subsequent.
- [ ] Image loading pipeline (`Fop.Imaging`) — built on ImageSharp; `ImageDimensions` started.
- [x] **SVG** (`fo:instream-foreign-object`) — `Fop.Svg` parses a static SVG subset (basic shapes,
      `path` with arc→Bezier, `g`/`transform`, presentation attrs + `style`, simple `text`) into
      renderer-neutral vector primitives; `Fop.Layout` scales them onto the object's content box as
      area-tree `VectorPath`s/`TextRun`s and the PdfSharp renderer paints them (`Fop.Render.Pdf`
      replacing Batik). Not modelled: gradients/patterns, filters, clipping, `<use>`, animation.
- [x] **text-decoration** (underline/overline/line-through): painted over the glyphs (a line-through
      overlays the text) using FOP's `renderTextDecoration` geometry (thickness `descender/8`;
      underline `baseline + descender/2`, overline `baseline - 1.1*capHeight`, line-through
      `baseline - 0.45*capHeight`) with per-line colours taken from the FO that declares the
      decoration (mirroring `CommonTextDecoration`); and **letter-spacing** (per-glyph tracking between
      glyphs, `(n-1)` gaps per word, drawn glyph-by-glyph). SVG embedding honours `preserveAspectRatio`
      (default `xMidYMid meet`).

## Phase 7 — Front-end & integration  `[~]`

- [x] `Fop.Cli` — command-line tool (port of `org.apache.fop.cli`): `fop in.fo out.pdf`, the
      `-fo`/`-pdf` flags, an `-xml`/`-xsl` XSLT-to-FO path (via `XslCompiledTransform`), `-fontdir`
      font registration, and `-version`/`-help`. Packable as a global .NET tool (`ToolCommandName=fop`).
- [ ] `FopFactory` / `Fop` user-facing API, configuration (`Fop.Configuration`).
- [x] Hyphenation (`Fop.Hyphenation`): `TernaryTree` + `HyphenationTree` (Liang algorithm),
      `PatternParser` (XmlReader), `Hyphenator` with an embedded English pattern set, wired into
      `Fop.Layout` line breaking via `hyphenate="true"`. (More languages/fuller patterns can be
      dropped in as embedded resources.)
- [ ] Accessibility / tagged PDF, SVG (Batik) integration strategy.

## Cross-cutting / ongoing

- [ ] Port JUnit suites alongside each module; add golden-file output comparisons vs. the Java FOP
      for end-to-end equivalence once a renderer works.
- [ ] CI workflow: `dotnet build` + `dotnet test` on the `dotnet/` solution.
- [x] Decide SVG story: a dedicated, dependency-light SVG parser (`Fop.Svg`) flattens a static SVG
      subset to vector primitives painted by the existing PdfSharp renderer (no Batik, no
      ImageSharp.Drawing dependency). Richer SVG (gradients, filters, clipping, `<use>`) is future work.
- [ ] Replace `commons-logging` call sites with `Microsoft.Extensions.Logging`.
