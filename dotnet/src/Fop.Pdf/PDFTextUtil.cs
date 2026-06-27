using System.Globalization;
using System.Text;

namespace Fop.Pdf;

/// <summary>
/// Utility for generating PDF text objects. Subclass it and implement <see cref="Write(string)"/>
/// / <see cref="Write(StringBuilder)"/> to add writing functionality.
/// </summary>
public abstract class PDFTextUtil
{
    /// <summary>The number of decimal places used for coordinate output.</summary>
    private const int Dec = 8;

    /// <summary>PDF text rendering mode: Fill text.</summary>
    public const int TrFill = 0;
    /// <summary>PDF text rendering mode: Stroke text.</summary>
    public const int TrStroke = 1;
    /// <summary>PDF text rendering mode: Fill, then stroke text.</summary>
    public const int TrFillStroke = 2;
    /// <summary>PDF text rendering mode: Neither fill nor stroke text (invisible).</summary>
    public const int TrInvisible = 3;
    /// <summary>PDF text rendering mode: Fill text and add to path for clipping.</summary>
    public const int TrFillClip = 4;
    /// <summary>PDF text rendering mode: Stroke text and add to path for clipping.</summary>
    public const int TrStrokeClip = 5;
    /// <summary>PDF text rendering mode: Fill, then stroke text and add to path for clipping.</summary>
    public const int TrFillStrokeClip = 6;
    /// <summary>PDF text rendering mode: Add text to path for clipping.</summary>
    public const int TrClip = 7;

    private bool _inTextObject;
    private string? _startText;
    private string? _endText;
    private bool _useMultiByte;
    private bool _useCid;
    private StringBuilder? _bufTJ;
    private int _textRenderingMode = TrFill;

    private string? _currentFontName;
    private double _currentFontSize;

    /// <summary>Writes PDF code.</summary>
    protected abstract void Write(string code);

    /// <summary>Writes PDF code.</summary>
    protected abstract void Write(StringBuilder code);

    private static void WriteAffineTransform(PdfAffineTransform at, StringBuilder sb)
    {
        double[] lt = at.GetMatrix();
        PDFNumber.DoubleOut(lt[0], Dec, sb);
        sb.Append(' ');
        PDFNumber.DoubleOut(lt[1], Dec, sb);
        sb.Append(' ');
        PDFNumber.DoubleOut(lt[2], Dec, sb);
        sb.Append(' ');
        PDFNumber.DoubleOut(lt[3], Dec, sb);
        sb.Append(' ');
        PDFNumber.DoubleOut(lt[4], Dec, sb);
        sb.Append(' ');
        PDFNumber.DoubleOut(lt[5], Dec, sb);
    }

    private static void WriteChar(int codePoint, StringBuilder sb, bool multibyte, bool cid)
    {
        if (!multibyte)
        {
            if (cid || codePoint < 32 || codePoint > 127)
            {
                sb.Append('\\').Append(ToOctal(codePoint));
            }
            else
            {
                switch (codePoint)
                {
                    case '(':
                    case ')':
                    case '\\':
                        sb.Append('\\');
                        break;
                    default:
                        break;
                }
                sb.Append(char.ConvertFromUtf32(codePoint));
            }
        }
        else
        {
            PDFText.ToUnicodeHex(codePoint, sb);
        }
    }

    private void WriteChar(int codePoint, StringBuilder sb) =>
        WriteChar(codePoint, sb, _useMultiByte, _useCid);

    private void CheckInTextObject()
    {
        if (!_inTextObject)
        {
            throw new InvalidOperationException("Not in text object");
        }
    }

    /// <summary>Indicates whether we are in a text object.</summary>
    public bool IsInTextObject => _inTextObject;

    /// <summary>
    /// Begins a new text object. Be sure to call <see cref="WriteTf"/> before issuing any text
    /// painting commands.
    /// </summary>
    public void BeginTextObject()
    {
        if (_inTextObject)
        {
            throw new InvalidOperationException("Already in text object");
        }
        Write("BT\n");
        _inTextObject = true;
    }

    /// <summary>Ends the current text object.</summary>
    public void EndTextObject()
    {
        CheckInTextObject();
        Write("ET\n");
        _inTextObject = false;
        InitValues();
    }

    /// <summary>Resets the state fields.</summary>
    protected virtual void InitValues()
    {
        _currentFontName = null;
        _currentFontSize = 0.0;
        _textRenderingMode = TrFill;
    }

    /// <summary>Creates a "cm" command from the transformation matrix.</summary>
    public void ConcatMatrix(PdfAffineTransform at)
    {
        if (!at.IsIdentity)
        {
            WriteTJ();
            var sb = new StringBuilder();
            WriteAffineTransform(at, sb);
            sb.Append(" cm\n");
            Write(sb);
        }
    }

    /// <summary>Writes a "Tf" command, setting a new current font.</summary>
    public void WriteTf(string fontName, double fontSize)
    {
        CheckInTextObject();
        var sb = new StringBuilder();
        sb.Append('/');
        sb.Append(fontName);
        sb.Append(' ');
        PDFNumber.DoubleOut(fontSize, 6, sb);
        sb.Append(" Tf\n");
        Write(sb);
        _startText = _useMultiByte ? "<" : "(";
        _endText = _useMultiByte ? ">" : ")";
    }

    /// <summary>Updates the current font, writing a "Tf" only if it changed.</summary>
    public void UpdateTf(string fontName, double fontSize, bool multiByte, bool cid)
    {
        CheckInTextObject();
        if (fontName != _currentFontName || fontSize != _currentFontSize)
        {
            WriteTJ();
            _currentFontName = fontName;
            _currentFontSize = fontSize;
            _useMultiByte = multiByte;
            _useCid = cid;
            WriteTf(fontName, fontSize);
        }
    }

    /// <summary>Sets the text rendering mode (0..7, see the TR_* constants).</summary>
    public void SetTextRenderingMode(int mode)
    {
        if (mode is < 0 or > 7)
        {
            throw new ArgumentException("Illegal value for text rendering mode. Expected: 0-7");
        }
        if (mode != _textRenderingMode)
        {
            WriteTJ();
            _textRenderingMode = mode;
            Write(_textRenderingMode.ToString(CultureInfo.InvariantCulture) + " Tr\n");
        }
    }

    /// <summary>Sets the text rendering mode from the fill/stroke/clip flags.</summary>
    public void SetTextRenderingMode(bool fill, bool stroke, bool addToClip)
    {
        int mode = fill ? (stroke ? 2 : 0) : (stroke ? 1 : 3);
        if (addToClip)
        {
            mode += 4;
        }
        SetTextRenderingMode(mode);
    }

    /// <summary>Writes a "Tm" command, setting a new text transformation matrix.</summary>
    public void WriteTextMatrix(PdfAffineTransform localTransform)
    {
        var sb = new StringBuilder();
        WriteAffineTransform(localTransform, sb);
        sb.Append(" Tm ");
        Write(sb);
    }

    /// <summary>Writes a mapped char to the "TJ" buffer.</summary>
    public void WriteTJMappedChar(char ch) => WriteTJMappedCodePoint(ch);

    /// <summary>Writes a mapped code point to the "TJ" buffer.</summary>
    public void WriteTJMappedCodePoint(int codePoint)
    {
        _bufTJ ??= new StringBuilder();
        if (_bufTJ.Length == 0)
        {
            _bufTJ.Append('[');
            _bufTJ.Append(_startText);
        }
        WriteChar(codePoint, _bufTJ);
    }

    /// <summary>Writes a glyph adjust value (in thousands of text unit space) to the "TJ" buffer.</summary>
    public void AdjustGlyphTJ(double adjust)
    {
        _bufTJ ??= new StringBuilder();
        if (_bufTJ.Length == 0)
        {
            _bufTJ.Append('[');
        }
        else
        {
            _bufTJ.Append(_endText);
            _bufTJ.Append(' ');
        }
        PDFNumber.DoubleOut(adjust, Dec - 4, _bufTJ);
        _bufTJ.Append(' ');
        _bufTJ.Append(_startText);
    }

    /// <summary>
    /// Writes a "TJ" command, flushing the accumulated buffer of characters and positioning
    /// values. The buffer is reset afterwards.
    /// </summary>
    public void WriteTJ()
    {
        if (IsInString())
        {
            _bufTJ!.Append(_endText);
            _bufTJ.Append("] TJ\n");
            Write(_bufTJ);
            _bufTJ.Length = 0;
        }
    }

    private bool IsInString() => _bufTJ is { Length: > 0 };

    /// <summary>Writes a "Td" command with the specified x and y coordinates.</summary>
    public void WriteTd(double x, double y)
    {
        var sb = new StringBuilder();
        PDFNumber.DoubleOut(x, Dec, sb);
        sb.Append(' ');
        PDFNumber.DoubleOut(y, Dec, sb);
        sb.Append(" Td\n");
        Write(sb);
    }

    /// <summary>Writes a "Tj" command with the specified character code.</summary>
    public void WriteTj(int ch, bool multibyte, bool cid)
    {
        var sb = new StringBuilder();
        sb.Append(_startText);
        WriteChar(ch, sb, multibyte, cid);
        sb.Append(_endText);
        sb.Append(" Tj\n");
        Write(sb);
    }

    // Java Integer.toOctalString: unsigned octal, no leading zeros.
    private static string ToOctal(int value)
    {
        if (value == 0)
        {
            return "0";
        }
        uint u = unchecked((uint)value);
        var sb = new StringBuilder();
        while (u != 0)
        {
            sb.Insert(0, (char)('0' + (int)(u & 7)));
            u >>= 3;
        }
        return sb.ToString();
    }
}
