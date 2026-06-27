namespace Fop.Pdf;

/// <summary>
/// A minimal 2D affine transform, standing in for <c>java.awt.geom.AffineTransform</c> as used by
/// <see cref="PDFTextUtil"/>.
/// </summary>
/// <remarks>
/// The six components follow Java's <c>AffineTransform.getMatrix(double[])</c> ordering:
/// <c>[m00, m10, m01, m11, m02, m12]</c> = (scaleX, shearY, shearX, scaleY, translateX, translateY).
/// Kept inline here so the PDF object model stays standalone; a full port would map this onto
/// <see cref="System.Numerics.Matrix3x2"/>.
/// </remarks>
public readonly struct PdfAffineTransform(
    double scaleX, double shearY, double shearX, double scaleY, double translateX, double translateY)
{
    /// <summary>m00 (scaleX).</summary>
    public double ScaleX { get; } = scaleX;

    /// <summary>m10 (shearY).</summary>
    public double ShearY { get; } = shearY;

    /// <summary>m01 (shearX).</summary>
    public double ShearX { get; } = shearX;

    /// <summary>m11 (scaleY).</summary>
    public double ScaleY { get; } = scaleY;

    /// <summary>m02 (translateX).</summary>
    public double TranslateX { get; } = translateX;

    /// <summary>m12 (translateY).</summary>
    public double TranslateY { get; } = translateY;

    /// <summary>Returns the six matrix components in Java's getMatrix() order.</summary>
    public double[] GetMatrix() => [ScaleX, ShearY, ShearX, ScaleY, TranslateX, TranslateY];

    /// <summary>Indicates whether this is the identity transform.</summary>
    public bool IsIdentity =>
        ScaleX == 1.0 && ShearY == 0.0 && ShearX == 0.0
        && ScaleY == 1.0 && TranslateX == 0.0 && TranslateY == 0.0;
}
