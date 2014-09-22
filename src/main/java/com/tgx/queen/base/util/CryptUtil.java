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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class CryptUtil
{
	
	static boolean LOADEDLIB = false;
	static
	{
		try
		{
			System.loadLibrary("tgxCryptUtil");
			LOADEDLIB = true;
		}
		catch (Throwable e)
		{
			// Ignore
		}
		
	}
	
	public native byte[] _pubKey(String passwd);
	
	/*
	 * 尚未单独实现 native byte[] eccEncrypt(byte[] publicKey, byte[] contents);
	 */
	/**
	 * @param passwd
	 *            解码密钥
	 * @param chiper
	 *            接收到的密文
	 * @return 解密后的结果
	 */
	public native byte[] _eccDecrypt(String passwd, byte[] chiper);
	
	/**
	 * @param seed
	 *            生成时的随机seed
	 * @param publicKey
	 *            公钥
	 * @param chiper
	 *            将要传递到解密方的密文存储buf
	 * @return rc4对称密钥(明文)
	 */
	public native byte[] _getRc4Key(String seed, byte[] publicKey, byte[] chiper);
	
	/**
	 * @return 密文buf的空间容量
	 */
	native int _getVlsize();
	
	public byte[] getChiperBuf() {
		return new byte[_getVlsize()];
	}
	
	/**
	 * xor ^ <0-127>
	 * 
	 * @param src
	 * @param xor
	 *            key
	 * @param xor_s
	 *            key_1
	 * @param xor_e
	 *            key_2
	 */
	public final static byte xorArrays(byte[] src, byte xor, byte xor_s, byte xor_e) {
		if (src == null || src.length == 0) return xor;
		if ((xor_s & 0xFF) == 0xFF && (xor_e & 0xFF) == 0xFF) return xor;// test
		else
		{
			int length = src.length;
			for (int i = 0; i < length; i++)
			{
				IoUtil.writeByte((src[i] & 0xFF) ^ xor, src, i);
				xor = (byte) (xor < xor_e ? xor + 1 : xor_s);
			}
			return xor;
		}
	}
	
	private static int[] crc_t; // CRC table
	                            
	private final static void mk() {
		int c, k;
		if (crc_t == null) crc_t = new int[256];
		for (int n = 0; n < 256; n++)
		{
			c = n;
			for (k = 0; k < 8; k++)
				c = (c & 1) == 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
			crc_t[n] = c;
		}
	}
	
	private final static int update(byte[] buf, int off, int len) {
		int c = 0xFFFFFFFF;
		int n;
		if (crc_t == null) mk();
		for (n = off; n < len + off; n++)
		{
			c = crc_t[(c ^ buf[n]) & 0xFF] ^ (c >>> 8);
		}
		return c;
	}
	
	public final static int crc320(byte[] buf, int off, int len) {
		return update(buf, off, len) ^ 0xFFFFFFFF;
	}
	
	public final static void releaseTable() {
		if (crc_t != null) crc_t = null;
		Thread.yield();
	}
	
	public final static int adler32(byte[] buf, int off, int len) {
		int s1 = 1 & 0x0000FFFF;
		int s2 = (1 >> 16) & 0x0000FFFF;
		len += off;
		for (int j = off; j < len; j++)
		{
			s1 += (buf[j] & 0x000000FF);
			s2 += s1;
		}
		s1 = s1 % 0xFFF1;
		s2 = s2 % 0xFFF1;
		return (int) ((s2 << 16) & 0xFFFF0000) | (int) (s1 & 0x0000FFFF);
	}
	
	public static int crc321(byte[] buf, int off, int len) {
		int crc = 0xffffffff;
		while (len-- != 0)
		{
			crc ^= buf[off++] & 0xFF;
			for (int i = 0; i < 8; i++)
			{
				if ((crc & 1) == 1)
				{
					crc >>>= 1;
					crc ^= 0xEDB88320;
				}
				else crc >>>= 1;
			}
		}
		return crc;
	}
	
	public static long crc641(byte[] buf, int off, int len) {
		long crc = 0xFFFFFFFFFFFFFFFFL;
		while (len-- != 0)
		{
			crc ^= buf[off++] & 0xFF;
			for (int i = 0; i < 8; i++)
			{
				if ((crc & 1) == 1)
				{
					crc >>>= 1;
					crc ^= 0x95AC9329AC4BC9B5L;
				}
				else crc >>>= 1;
			}
		}
		return crc;
	}
	
	final static String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	
	final static char   pad   = '=';
	
	public final static byte[] base64Decoder(char[] src, int start) throws IOException {
		if (src == null || src.length == 0) return null;
		char[] four = new char[4];
		int i = 0, l, aux;
		char c;
		boolean padded;
		ByteArrayOutputStream dst = new ByteArrayOutputStream(src.length >> 1);
		while (start < src.length)
		{
			i = 0;
			do
			{
				if (start >= src.length)
				{
					if (i > 0) throw new IOException("bad BASE 64 In->");
					else return dst.toByteArray();
				}
				c = src[start++];
				if (chars.indexOf(c) != -1 || c == pad) four[i++] = c;
				else if (c != '\r' && c != '\n') throw new IOException("bad BASE 64 In->");
			}
			while (i < 4);
			padded = false;
			for (i = 0; i < 4; i++)
			{
				if (four[i] != pad && padded) throw new IOException("bad BASE 64 In->");
				else if (!padded && four[i] == pad) padded = true;
			}
			if (four[3] == pad)
			{
				if (start < src.length) throw new IOException("bad BASE 64 In->");
				l = four[2] == pad ? 1 : 2;
			}
			else l = 3;
			for (i = 0, aux = 0; i < 4; i++)
				if (four[i] != pad) aux |= chars.indexOf(four[i]) << (6 * (3 - i));
			
			for (i = 0; i < l; i++)
				dst.write((aux >>> (8 * (2 - i))) & 0xFF);
		}
		dst.flush();
		byte[] result = dst.toByteArray();
		dst.close();
		dst = null;
		return result;
	}
	
	public final static String base64Encoder(byte[] src, int start, int wrapAt) {
		return base64Encoder(src, start, src.length, wrapAt);
	}
	
	public final static String base64Encoder(byte[] src, int start, int length, int wrapAt) {
		if (src == null || src.length == 0) return null;
		StringBuffer encodeDst = new StringBuffer();
		int lineCounter = 0;
		length = start + length > src.length ? src.length : start + length;
		while (start < length)
		{
			int buffer = 0, byteCounter;
			for (byteCounter = 0; byteCounter < 3 && start < length; byteCounter++, start++)
				buffer |= (src[start] & 0xFF) << (16 - (byteCounter << 3));
			if (wrapAt > 0 && lineCounter == wrapAt)
			{
				encodeDst.append("\r\n");
				lineCounter = 0;
			}
			char b1 = chars.charAt((buffer << 8) >>> 26);
			char b2 = chars.charAt((buffer << 14) >>> 26);
			char b3 = (byteCounter < 2) ? pad : chars.charAt((buffer << 20) >>> 26);
			char b4 = (byteCounter < 3) ? pad : chars.charAt((buffer << 26) >>> 26);
			encodeDst.append(b1).append(b2).append(b3).append(b4);
			lineCounter += 4;
		}
		return encodeDst.toString();
	}
	
	public final static String quoted_print_Encoding(String src, String charSet) {
		if (src == null || src.equals("")) return null;
		int maxLine = 76;
		try
		{
			byte[] encodeData = src.getBytes(charSet);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			char[] charArry;
			for (int i = 0, l = 0; i < encodeData.length; i++)
			{
				
				if (encodeData[i] >= '!' && encodeData[i] <= '~' && encodeData[i] != '=')
				{
					if (l == maxLine)
					{
						buffer.write("=\r\n".getBytes());
						l = 0;
					}
					buffer.write(encodeData[i]);
					l++;
				}
				else
				{
					if (l > maxLine - 3)
					{
						buffer.write("=\r\n".getBytes());
						l = 0;
					}
					buffer.write('=');
					charArry = Integer.toHexString(encodeData[i] & 0xFF).toUpperCase().toCharArray();
					if (charArry.length < 2) buffer.write('0');
					for (char c : charArry)
						buffer.write(c);
					l += 3;
				}
				
			}
			buffer.flush();
			encodeData = null;
			String result = new String(buffer.toByteArray(), charSet);
			buffer.close();
			return result;
		}
		catch (UnsupportedEncodingException e)
		{
			// #debug error
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// #debug error
			e.printStackTrace();
		}
		return src;
	}
	
	public final static String quoted_print_Decoding(String src, String charSet) {
		if (src == null || src.equals("")) return null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int length = src.length();
		try
		{
			boolean canIntParse;
			String encode;
			int wr;
			for (int i = 0, k; i < length;)
			{
				k = i + 1;
				canIntParse = src.charAt(i) == '=';
				if (canIntParse)
				{
					encode = src.substring(k, i += 3);
					if (encode.equals("\r\n") || encode.equals("\n")) continue;
					wr = Integer.parseInt(encode, 16);
				}
				else
				{
					wr = src.charAt(i++);
					if (wr < '!' || wr > '~') continue;
				}
				baos.write(wr);
			}
			baos.flush();
			return new String(baos.toByteArray(), charSet);
		}
		catch (UnsupportedEncodingException e)
		{
			// #debug error
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// #debug error
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// #debug error
			e.printStackTrace();
		}
		finally
		{
			try
			{
				baos.close();
			}
			catch (IOException e)
			{
				// #debug error
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private MessageDigest MD5     = create("MD5");
	private MessageDigest SHA_1   = create("SHA-1");
	private MessageDigest SHA_256 = create("SHA-256");
	
	private final byte[] digest(String digestName, byte[] input) {
		return digest(digestName, input, 0, input.length);
	}
	
	private final byte[] digest(String digestName, byte[] input, int offset, int len) {
		if (input == null || digestName == null) throw new NullPointerException();
		if (input.length < len || len <= 0 || offset < 0 || offset >= len) throw new ArrayIndexOutOfBoundsException();
		MessageDigest md = null;
		switch (digestName.toUpperCase()) {
			case "MD5":
				md = MD5;
				break;
			case "SHA-1":
				md = SHA_1;
				break;
			case "SHA-256":
				md = SHA_256;
				break;
			default:
				throw new IllegalArgumentException();
		}
		md.reset();
		md.update(input, offset, len);
		return md.digest();
	}
	
	private final MessageDigest create(String name) {
		try
		{
			return MessageDigest.getInstance(name);
		}
		catch (NoSuchAlgorithmException ne)
		{
			return null;
		}
	}
	
	public final byte[] md5(byte[] input, int offset, int len) {
		return digest("MD5", input, offset, len);
	}
	
	public final byte[] md5(byte[] input) {
		return digest("MD5", input);
	}
	
	public final byte[] sha1(byte[] input, int offset, int len) {
		return digest("SHA-1", input, offset, len);
	}
	
	public final byte[] sha1(byte[] input) {
		return digest("SHA-1", input);
	}
	
	public final byte[] sha256(byte[] input, int offset, int len) {
		return digest("SHA-256", input, offset, len);
	}
	
	public final byte[] sha256(byte[] input) {
		return digest("SHA-256", input);
	}
	
}
