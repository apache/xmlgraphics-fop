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
- [ ] Font embedding/subsetting hooks for the PDF/PS/AFP renderers.
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
- [ ] Full property subsystem (the remaining ~290 properties, shorthands, refinement) — a curated
      subset is resolved in `PropertyList`, now backed by the expression evaluator.
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
- [ ] Knuth total-fit line/page breaking, floats, intra-row splitting, multi-region side regions
      (start/end), markers/`retrieve-marker`. (Residual TODOs: footnote reserve is greedy not
      iterative; a row-spanning cell crossing a page break paints on its origin page only; ids inside
      buffered contexts (cells/footnote bodies) aren't recorded for citations; citation-last uses the
      single recorded page under the flat model.)

## Phase 6 — Renderers  `[ ]`  (~79,000 LOC)

Independent back-ends; port in priority order. Each can be its own project (`Fop.Render.Pdf`, …).

- [ ] `Fop.Render` shared: `Renderer`, `IFDocumentHandler` (intermediate format), painter abstractions.
- [~] **PDF** — two tracks:
      - `Fop.Render.Pdf` renders the area tree to PDF via **PdfSharp** today (text, fonts, colour,
        rects) and exposes the `FopProcessor` facade — a working FO→PDF path.
      - `Fop.Pdf` is a from-scratch port of FOP's low-level PDF object model (PDFObject/Name/Number/
        String/Array/Dictionary/Reference/Null + serialization); the longer-term goal is a native
        renderer on `Fop.Pdf` (PDFDocument, page/resource/font/image objects, encryption, filters)
        so PdfSharp can become optional.
      - Still needed in the PdfSharp renderer: borders/backgrounds, images, links, leaders, bookmarks.
- [ ] Bitmap/Java2D → **ImageSharp** raster renderer (`Fop.Render.Bitmap`).
- [ ] PostScript, AFP, PCL, RTF, TXT, intermediate XML — subsequent.
- [ ] Image loading pipeline (`Fop.Imaging`) — built on ImageSharp; `ImageDimensions` started.

## Phase 7 — Front-end & integration  `[ ]`

- [ ] `Fop.Cli` — command-line tool (port of `org.apache.fop.cli`).
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
- [ ] Decide SVG story (Batik has no direct .NET equivalent — evaluate ImageSharp.Drawing + a
      dedicated SVG parser, or scope SVG out initially).
- [ ] Replace `commons-logging` call sites with `Microsoft.Extensions.Logging`.
