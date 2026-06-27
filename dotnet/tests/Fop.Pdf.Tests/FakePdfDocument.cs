using Fop.Pdf;

namespace Fop.Pdf.Tests;

/// <summary>
/// A minimal <see cref="IPdfDocument"/> for tests, standing in for the not-yet-ported PDFDocument.
/// </summary>
internal sealed class FakePdfDocument : IPdfDocument
{
    private int _objectCount;

    public int AllocateObjectNumber() => ++_objectCount;

    public bool IsEncryptionActive => false;
}

/// <summary>A concrete throwaway PDFObject for exercising the abstract base.</summary>
internal sealed class DummyPDFObject : PDFObject
{
}
