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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class StringUtils
{
	
	private static Map<String, MessageDigest> digests = new ConcurrentHashMap<String, MessageDigest>();
	
	public static Integer parseInt(Object value) {
		if (value != null)
		{
			if (value instanceof Integer)
			{
				return (Integer) value;
			}
			else if (value instanceof String)
			{
				try
				{
					return Integer.valueOf((String) value);
				}
				catch (Exception e)
				{
					return null;
				}
			}
		}
		return null;
	}
	
	public static Long parseLong(Object value) {
		if (value != null)
		{
			if (value instanceof Long) { return (Long) value; }
			if (value instanceof Integer)
			{
				return ((Integer) value).longValue();
			}
			else if (value instanceof String)
			{
				try
				{
					return Long.valueOf((String) value);
				}
				catch (Exception ex)
				{
					return 0L;
				}
			}
		}
		return 0L;
	}
	
	public static Double parseDouble(Object value) {
		if (value != null)
		{
			if (value instanceof Double) { return (Double) value; }
			if (value instanceof BigDecimal) { return Double.valueOf(value.toString()); }
			if (value instanceof Integer)
			{
				return (double) ((Integer) value).longValue();
			}
			else if (value instanceof String)
			{
				try
				{
					return Double.valueOf((String) value);
				}
				catch (Exception ex)
				{
					return null;
				}
			}
		}
		return null;
	}
	
	public static String parseString(Object value) {
		if (value != null) { return String.valueOf(value); }
		return null;
	}
	
	public static List<?> parseList(Object value) {
		if (value != null)
		{
			try
			{
				return (List<?>) value;
			}
			catch (Exception ex)
			{
				return null;
			}
		}
		return null;
	}
	
	public static Boolean parseBoolean(Object value) {
		if (value != null)
		{
			if (value instanceof Integer)
			{
				return ((Integer) value).intValue() == 1;
			}
			else if (value instanceof String) { return "1".equals(value) || "true".equals(value); }
		}
		return null;
	}
	
	public static String hash(String data) {
		return hash(data, "MD5");
	}
	
	public static String hash(String data, String algorithm) {
		try
		{
			return hash(data.getBytes("utf-8"), algorithm);
		}
		catch (UnsupportedEncodingException e)
		{
			
		}
		return data;
	}
	
	public static String hash(byte[] bytes, String algorithm) {
		synchronized (algorithm.intern())
		{
			MessageDigest digest = digests.get(algorithm);
			if (digest == null)
			{
				try
				{
					digest = MessageDigest.getInstance(algorithm);
					digests.put(algorithm, digest);
				}
				catch (Exception nsae)
				{
					return null;
				}
			}
			// Now, compute hash.
			digest.update(bytes);
			return encodeHex(digest.digest());
		}
	}
	
	public static String encodeHex(byte[] bytes) {
		StringBuilder buf = new StringBuilder(bytes.length * 2);
		int i;
		
		for (i = 0; i < bytes.length; i++)
		{
			if (((int) bytes[i] & 0xff) < 0x10)
			{
				buf.append("0");
			}
			buf.append(Long.toString((int) bytes[i] & 0xff, 16));
		}
		return buf.toString();
	}
	
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	public static boolean isNotEmpty(String str) {
		return !StringUtils.isEmpty(str);
	}
	
	public static String trim(String str) {
		return str == null ? null : str.trim();
	}
	
	/**
	 * 计算字符串字节长度
	 * 
	 * @param str
	 * @return
	 * @author 蔡永干
	 */
	public static int getStringByteLength(String str) {
		char[] t = str.toCharArray();
		int count = 0;
		for (char c : t)
		{
			if ((c >= 0x4e00) && (c <= 0x9fbb))
			{
				count = count + 2;
			}
			else
			{
				count++;
			}
		}
		return count;
	}
}
