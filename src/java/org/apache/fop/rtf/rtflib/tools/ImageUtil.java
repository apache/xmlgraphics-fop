package org.apache.fop.rtf.rtflib.tools;

/*-----------------------------------------------------------------------------
 * jfor - Open-Source XSL-FO to RTF converter - see www.jfor.org
 *
 * ====================================================================
 * jfor Apache-Style Software License.
 * Copyright (c) 2002 by the jfor project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed
 * by the jfor project (http://www.jfor.org)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "jfor" must not be used to endorse
 * or promote products derived from this software without prior written
 * permission.  For written permission, please contact info@jfor.org.
 *
 * 5. Products derived from this software may not be called "jfor",
 * nor may "jfor" appear in their name, without prior written
 * permission of info@jfor.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE JFOR PROJECT OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * Contributor(s):
-----------------------------------------------------------------------------*/

/**  Misc.utilities for images handling
 *  This class belongs to the <fo:external-graphic> tag processing.
 *  @author <a href="mailto:a.putz@skynamics.com">Andreas Putz</a>
 */
public class ImageUtil
{

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////

	/**
	 * Private constructor.
	 */
	private ImageUtil ()
	{
	}


	//////////////////////////////////////////////////
	// @@ Public static methods
	//////////////////////////////////////////////////

	/**
	 * Determines the digits from a string.
	 *
	 * @param value String with digits
	 *
	 * @return
	 *  -1      There is no digit\n
	 *  number  The digits as integer
	 */
	public static int getInt (String value)
	{
		String retString = new String ();
		StringBuffer s = new StringBuffer (value);
		int len = s.length ();

		for (int i = 0; i < len; i++)
		{
			if (Character.isDigit (s.charAt (i)))
			{
				retString += s.charAt (i);
			}
		}

		if (retString.length () == 0)
		{
			return -1;
		}
		else
		{
			return Integer.parseInt (retString);
		}
	}

	/**
	 * Checks the string for percent character at the end of string.
	 *
	 * @param value String with digits
	 *
	 * @return
	 * true    The string contains a % value
	 * false   Other string
	 */
	public static boolean isPercent (String value)
	{
		if (value.endsWith ("%"))
		{
			return true;

		}

		return false;
	}

	/**
	 * Compares two hexadecimal values.
	 *
	 * @param pattern Target
	 * @param data Data
	 * @param searchAt Position to start compare
	 * @param searchForward Direction to compare byte arrays
	 *
	 * @return
	 *  true    If equal\n
	 *  false   If different
	 */
	public static boolean compareHexValues (byte[] pattern, byte[] data, int searchAt,
											boolean searchForward)
	{
		if (searchAt >= data.length)
		{
			return false;

		}

		int pLen = pattern.length;

		if (searchForward)
		{
			if (pLen >= (data.length - searchAt))
			{
				return false;

			}

			for (int i = 0; i < pLen; i++)
			{
				if (pattern[i] != data[searchAt + i])
				{
					return false;
				}
			}

			return true;
		}
		else
		{
			if (pLen > (searchAt + 1))
			{
				return false;

			}

			for (int i = 0; i < pLen; i++)
			{
				if (pattern[pLen - i - 1] != data[searchAt - i])
				{
					return false;
				}
			}

			return true;
		}
	}

	/**
	 * Determines a integer value from a hexadecimal byte array.
	 *
	 * @param data Image
	 * @param start Start index to read from
	 * @param end End index until to read
	 *
	 * @return A number
	 */
	public static int getIntFromByteArray (byte[] data, int startAt, int length,
										   boolean searchForward)
	{
		int bit = 8;
		int bitMoving = length * bit;
		int retVal = 0;

		if (startAt >= data.length)
		{
			return retVal;

		}

		if (searchForward)
		{
			if (length >= (data.length - startAt))
			{
				return retVal;

			}

			for (int i = 0; i < length; i++)
			{
				bitMoving -= bit;
				int iData = (int) data[startAt + i];
				if (iData < 0)
					iData += 256;
				retVal += iData << bitMoving;
			}
		}
		else
		{
			if (length > (startAt + 1))
			{
				return retVal;

			}

			for (int i = 0; i < length; i++)
			{
				bitMoving -= bit;
				int iData = (int) data[startAt - i];
				if (iData < 0)
					iData += 256;
				retVal += iData << bitMoving;			}
		}

		return retVal;
	}
}