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

namespace Fop.Fo;

/// <summary>The <c>table-layout</c> property values.</summary>
public enum TableLayout
{
    /// <summary>Automatic column sizing (best-effort; treated like fixed in this cut).</summary>
    Auto,

    /// <summary>Fixed column sizing driven by <c>fo:table-column</c> widths.</summary>
    Fixed,
}

/// <summary>
/// A table, <c>fo:table</c>. Port of the role of <c>org.apache.fop.fo.flow.table.Table</c>, scoped
/// to a separated border model for the current pipeline.
/// </summary>
public sealed class FoTable(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "table";

    /// <summary>The specified table <c>width</c>, or <c>null</c> for <c>auto</c>/unset.</summary>
    public FoLength? Width => ParseOptionalLength("width", percentBaseMpt: 0);

    /// <summary>The <c>table-layout</c> (defaults to <see cref="TableLayout.Auto"/>).</summary>
    public TableLayout TableLayout => Properties.GetRaw("table-layout")?.Trim().ToLowerInvariant() switch
    {
        "fixed" => TableLayout.Fixed,
        _ => TableLayout.Auto,
    };

    /// <summary>Whether <c>border-collapse</c> is requested (read but a separated model is used).</summary>
    public bool BorderCollapse => Properties.GetRaw("border-collapse")?.Trim().ToLowerInvariant() switch
    {
        "collapse" or "collapse-with-precedence" => true,
        _ => false,
    };

    /// <summary>Space before the table (optimum).</summary>
    public FoLength SpaceBefore =>
        Properties.GetLength("space-before", Properties.GetLength("space-before.optimum", FoLength.Zero));

    /// <summary>Space after the table (optimum).</summary>
    public FoLength SpaceAfter =>
        Properties.GetLength("space-after", Properties.GetLength("space-after.optimum", FoLength.Zero));

    /// <summary>The resolved box-model properties (borders, padding, background colour).</summary>
    public BoxProperties Box => Properties.GetBox();

    /// <summary>The declared <c>fo:table-column</c> children, in document order.</summary>
    public IEnumerable<FoTableColumn> Columns => ChildObjects.OfType<FoTableColumn>();

    /// <summary>The table header, if present.</summary>
    public FoTableHeader? Header => ChildObjects.OfType<FoTableHeader>().FirstOrDefault();

    /// <summary>The table footer, if present.</summary>
    public FoTableFooter? Footer => ChildObjects.OfType<FoTableFooter>().FirstOrDefault();

    /// <summary>The table bodies, in document order.</summary>
    public IEnumerable<FoTableBody> Bodies => ChildObjects.OfType<FoTableBody>();

    private FoLength? ParseOptionalLength(string name, double percentBaseMpt)
    {
        string? raw = Properties.GetRaw(name);
        if (raw is null)
        {
            return null;
        }

        string trimmed = raw.Trim();
        if (trimmed.Length == 0 || trimmed.Equals("auto", StringComparison.OrdinalIgnoreCase))
        {
            return null;
        }

        return FoLength.TryParse(trimmed, Properties.FontSizeMpt, percentBaseMpt);
    }
}

/// <summary>
/// The kind of a column width declaration on <c>fo:table-column</c>: an absolute length, a
/// percentage of the table width, a proportional share, or unset/auto.
/// </summary>
public enum ColumnWidthKind
{
    /// <summary>No usable width specified (distribute equally).</summary>
    Auto,

    /// <summary>An absolute length (already resolved to millipoints).</summary>
    Absolute,

    /// <summary>A percentage of the table width.</summary>
    Percent,

    /// <summary>A proportional share, <c>proportional-column-width(N)</c>.</summary>
    Proportional,
}

/// <summary>
/// A resolved <c>column-width</c> specification. For <see cref="ColumnWidthKind.Absolute"/> the value
/// is millipoints; for <see cref="ColumnWidthKind.Percent"/> it is a percentage (0..100); for
/// <see cref="ColumnWidthKind.Proportional"/> it is the share count.
/// </summary>
/// <param name="Kind">The kind of width.</param>
/// <param name="Value">The numeric value, interpreted per <paramref name="Kind"/>.</param>
public readonly record struct ColumnWidthSpec(ColumnWidthKind Kind, double Value)
{
    /// <summary>An unset/auto width.</summary>
    public static readonly ColumnWidthSpec Auto = new(ColumnWidthKind.Auto, 0);

    /// <summary>Resolves the width to millipoints given a table width (used for percentages).</summary>
    /// <returns>The width in millipoints, or <c>null</c> for proportional/auto kinds.</returns>
    public double? ResolveMpt(double tableWidthMpt) => Kind switch
    {
        ColumnWidthKind.Absolute => Value,
        ColumnWidthKind.Percent => tableWidthMpt * Value / 100.0,
        _ => null,
    };
}

/// <summary>
/// A table column, <c>fo:table-column</c>. Port of the role of
/// <c>org.apache.fop.fo.flow.table.TableColumn</c>.
/// </summary>
public sealed class FoTableColumn(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "table-column";

    /// <summary>The 1-based <c>column-number</c>, or <c>null</c> when unset (sequential assignment).</summary>
    public int? ColumnNumber
    {
        get
        {
            string? raw = Properties.GetRaw("column-number");
            if (raw is not null
                && int.TryParse(raw.Trim(), NumberStyles.Integer, CultureInfo.InvariantCulture, out int n)
                && n >= 1)
            {
                return n;
            }

            return null;
        }
    }

    /// <summary>The <c>number-columns-repeated</c> (default 1).</summary>
    public int NumberColumnsRepeated
    {
        get
        {
            string? raw = Properties.GetRaw("number-columns-repeated");
            if (raw is not null
                && int.TryParse(raw.Trim(), NumberStyles.Integer, CultureInfo.InvariantCulture, out int n)
                && n >= 1)
            {
                return n;
            }

            return 1;
        }
    }

    /// <summary>The resolved box-model properties (borders, padding, background colour).</summary>
    public BoxProperties Box => Properties.GetBox();

    /// <summary>
    /// Parses the <c>column-width</c> declaration. Supports absolute lengths, percentages of the
    /// table width, and <c>proportional-column-width(N)</c>; anything unrecognised resolves to
    /// <see cref="ColumnWidthSpec.Auto"/>.
    /// </summary>
    public ColumnWidthSpec ColumnWidth => ParseColumnWidth(Properties.GetRaw("column-width"), Properties.FontSizeMpt);

    /// <summary>Parses a <c>column-width</c> value into a <see cref="ColumnWidthSpec"/>.</summary>
    public static ColumnWidthSpec ParseColumnWidth(string? raw, double fontSizeMpt)
    {
        if (string.IsNullOrWhiteSpace(raw))
        {
            return ColumnWidthSpec.Auto;
        }

        string text = raw.Trim();
        if (text.Equals("auto", StringComparison.OrdinalIgnoreCase))
        {
            return ColumnWidthSpec.Auto;
        }

        if (text.StartsWith("proportional-column-width(", StringComparison.OrdinalIgnoreCase)
            && text.EndsWith(')'))
        {
            string inner = text["proportional-column-width(".Length..^1].Trim();
            if (double.TryParse(inner, NumberStyles.Float, CultureInfo.InvariantCulture, out double shares)
                && shares > 0)
            {
                return new ColumnWidthSpec(ColumnWidthKind.Proportional, shares);
            }

            return ColumnWidthSpec.Auto;
        }

        if (text.EndsWith('%'))
        {
            if (double.TryParse(text[..^1], NumberStyles.Float, CultureInfo.InvariantCulture, out double pct))
            {
                return new ColumnWidthSpec(ColumnWidthKind.Percent, pct);
            }

            return ColumnWidthSpec.Auto;
        }

        FoLength? len = FoLength.TryParse(text, fontSizeMpt);
        return len is not null
            ? new ColumnWidthSpec(ColumnWidthKind.Absolute, len.Value.Millipoints)
            : ColumnWidthSpec.Auto;
    }
}

/// <summary>Base for the row-container parts of a table (header/body/footer).</summary>
public abstract class FoTablePart(PropertyList properties) : FObj(properties)
{
    /// <summary>The rows in this part, in document order.</summary>
    public IEnumerable<FoTableRow> Rows => ChildObjects.OfType<FoTableRow>();

    /// <summary>
    /// Whether this part declares cells directly (no <c>fo:table-row</c> wrappers). XSL-FO permits
    /// <c>fo:table-cell</c> children directly under a body part, using
    /// <c>starts-row</c>/<c>ends-row</c> to delimit rows.
    /// </summary>
    public bool HasDirectCells => ChildObjects.OfType<FoTableCell>().Any();

    /// <summary>The direct cells (when rows are not wrapped), in document order.</summary>
    public IEnumerable<FoTableCell> DirectCells => ChildObjects.OfType<FoTableCell>();
}

/// <summary>A table header, <c>fo:table-header</c>.</summary>
public sealed class FoTableHeader(PropertyList properties) : FoTablePart(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "table-header";
}

/// <summary>A table body, <c>fo:table-body</c>.</summary>
public sealed class FoTableBody(PropertyList properties) : FoTablePart(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "table-body";
}

/// <summary>A table footer, <c>fo:table-footer</c>.</summary>
public sealed class FoTableFooter(PropertyList properties) : FoTablePart(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "table-footer";
}

/// <summary>A table row, <c>fo:table-row</c>. Port of <c>org.apache.fop.fo.flow.table.TableRow</c>.</summary>
public sealed class FoTableRow(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "table-row";

    /// <summary>The minimum row <c>height</c>, or <c>null</c> when unset/auto.</summary>
    public FoLength? Height
    {
        get
        {
            string? raw = Properties.GetRaw("height");
            if (raw is null)
            {
                return null;
            }

            string trimmed = raw.Trim();
            if (trimmed.Length == 0 || trimmed.Equals("auto", StringComparison.OrdinalIgnoreCase))
            {
                return null;
            }

            return FoLength.TryParse(trimmed, Properties.FontSizeMpt);
        }
    }

    /// <summary>The resolved box-model properties (borders, padding, background colour).</summary>
    public BoxProperties Box => Properties.GetBox();

    /// <summary>The cells in this row, in document order.</summary>
    public IEnumerable<FoTableCell> Cells => ChildObjects.OfType<FoTableCell>();
}

/// <summary>A table cell, <c>fo:table-cell</c>. Port of <c>org.apache.fop.fo.flow.table.TableCell</c>.</summary>
public sealed class FoTableCell(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "table-cell";

    /// <summary>The <c>number-columns-spanned</c> (default 1).</summary>
    public int NumberColumnsSpanned => ReadSpan("number-columns-spanned");

    /// <summary>The <c>number-rows-spanned</c> (default 1), honoured by the layout engine's table grid.</summary>
    public int NumberRowsSpanned => ReadSpan("number-rows-spanned");

    /// <summary>The explicit 1-based <c>column-number</c> for this cell, or <c>null</c> when unset.</summary>
    public int? ColumnNumber
    {
        get
        {
            string? raw = Properties.GetRaw("column-number");
            if (raw is not null
                && int.TryParse(raw.Trim(), NumberStyles.Integer, CultureInfo.InvariantCulture, out int n)
                && n >= 1)
            {
                return n;
            }

            return null;
        }
    }

    /// <summary>Whether this cell starts a new row (for the wrapper-less body form).</summary>
    public bool StartsRow => Properties.GetRaw("starts-row")?.Trim().ToLowerInvariant() == "true";

    /// <summary>Whether this cell ends the current row (for the wrapper-less body form).</summary>
    public bool EndsRow => Properties.GetRaw("ends-row")?.Trim().ToLowerInvariant() == "true";

    /// <summary>The resolved box-model properties (borders, padding, background colour).</summary>
    public BoxProperties Box => Properties.GetBox();

    /// <summary>The block-level children of this cell.</summary>
    public IEnumerable<FoBlock> Blocks => ChildObjects.OfType<FoBlock>();

    private int ReadSpan(string name)
    {
        string? raw = Properties.GetRaw(name);
        if (raw is not null
            && int.TryParse(raw.Trim(), NumberStyles.Integer, CultureInfo.InvariantCulture, out int n)
            && n >= 1)
        {
            return n;
        }

        return 1;
    }
}
