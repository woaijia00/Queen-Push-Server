/**
 * -
 * Copyright (c) 2013 Zhang Zhuo
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote
 * products derived from this software without specific written permission.
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.tgx.queen.base.util;

public class I18nUtil
{
	public final static String getCharset(byte data) {
		String charset;
		switch (data & 0xF0) {
			case CHARSET_ASCII:
				charset = "ASCII";
				break;
			case CHARSET_UTF_8:
				charset = "UTF-8";
				break;
			case CHARSET_UTF_8_NB:
				charset = "UTF-16";
				break;
			case CHARSET_UTC_BE:
				charset = "UTF-16BE";
				break;
			case CHARSET_UTC_LE:
				charset = "UTF-16LE";
				break;
			case CHARSET_GBK:
				charset = "GBK";
				break;
			case CHARSET_GB2312:
				charset = "GB2312";
				break;
			case CHARSET_GB18030:
				charset = "GB18030";
				break;
			case CHARSET_ISO_8859_1:
				charset = "ISO-8859-1";
				break;
			default:
				charset = "UTF-8";
				break;
		}
		return charset;
	}
	
	public final static int getCharsetCode(String charset) {
		if (charset.equals("ASCII")) return CHARSET_ASCII;
		if (charset.equals("UTF-8")) return CHARSET_UTF_8;
		if (charset.equals("UTF-16")) return CHARSET_UTF_8_NB;
		if (charset.equals("UTF-16BE")) return CHARSET_UTC_BE;
		if (charset.equals("UTF-16LE")) return CHARSET_UTC_LE;
		if (charset.equals("GBK")) return CHARSET_GBK;
		if (charset.equals("GB2312")) return CHARSET_GB2312;
		if (charset.equals("GB18030")) return CHARSET_GB18030;
		if (charset.equals("ISO-8859-1")) return CHARSET_ISO_8859_1;
		if (charset.equals("ISO-8859-15")) return CHARSET_ISO_8859_15;
		return CHARSET_UTF_8;
	}
	
	public final static int CHARSET_ASCII       = 0x00;
	public final static int CHARSET_UTF_8       = 0x01 << 4;
	public final static int CHARSET_UTF_8_NB    = 0x02 << 4;
	public final static int CHARSET_UTC_BE      = 0x03 << 4;
	public final static int CHARSET_UTC_LE      = 0x04 << 4;
	public final static int CHARSET_GBK         = 0x05 << 4;
	public final static int CHARSET_GB2312      = 0x06 << 4;
	public final static int CHARSET_GB18030     = 0x07 << 4;
	public final static int CHARSET_ISO_8859_1  = 0x08 << 4;
	public final static int CHARSET_ISO_8859_15 = 0x09 << 4;
	
	public final static int SERIAL_BINARY       = 0x00;
	public final static int SERIAL_PROXY        = 0x01;
	public final static int SERIAL_JSON         = 0x02;
	public final static int SERIAL_XML          = 0x03;
	
	public final static byte getCharset_Serial(int charset_, int serial_) {
		return (byte) (charset_ | serial_);
	}
	
	public final static boolean isTypeBin(byte type_c) {
		return (type_c & 0x0F) == SERIAL_BINARY;
	}
	
	public final static boolean isTypeTxt(byte type_c) {
		return (type_c & 0x0F) != SERIAL_BINARY;
	}
	
	public final static boolean isTypeJson(byte type_c) {
		return (type_c & 0x0F) != SERIAL_JSON;
	}
	
	public final static boolean isTypeXml(byte type_c) {
		return (type_c & 0x0F) != SERIAL_XML;
	}
	
	public final static boolean checkType(byte type_c, byte expect) {
		return (type_c & 0x0F) == expect;
	}
}
