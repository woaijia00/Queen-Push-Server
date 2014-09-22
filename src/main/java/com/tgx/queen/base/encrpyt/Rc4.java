/*******************************************************************************
 * Copyright 2013 Zhang Zhuo(william@TinyGameX.com).
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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
package com.tgx.queen.base.encrpyt;

import java.nio.ByteBuffer;
import java.util.Random;

import com.tgx.queen.base.inf.IReset;


public class Rc4
        implements
        IReset
{
	
	public byte[] getKey(String seed) {
		long curTime = System.currentTimeMillis();
		long code = hashCode();
		long tick = curTime ^ (code << 31);
		Random rd = new Random(tick);
		byte[] xc;
		if (seed == null || "".equals(seed.trim())) seed = "Tgx.Tina.Rc4" + System.nanoTime();
		xc = seed.getBytes();
		byte[] key = new byte[20];
		for (int i = 0, j = 1; i < key.length; i++)
		{
			for (byte b : xc)
			{
				long dx = System.nanoTime() ^ tick ^ rd.nextLong() ^ b;
				key[i] ^= dx >> j++;
				if (j > 40) j = 1;
			}
		}
		return key;
	}
	
	private final byte[] S = new byte[256];
	
	private void ksa(byte[] key) {
		if (initialized || key == null) return;
		for (int i = 0; i < S.length; i++)
			S[i] = (byte) i;
		for (int i = 0, j = 0; i < S.length; i++)
		{
			j = (j + (S[i] & 0xFF) + (key[i % key.length] & 0xFF)) & 0xFF;
			swap(S, i, j);
		}
		initialized = true;
	}
	
	private int     i, j;
	private boolean initialized;
	
	public void rc4Stream(ByteBuffer buffer, byte[] key) {
		if (!buffer.hasRemaining() || key == null) return;
		ksa(key);
		int limit = buffer.limit();
		for (int x = buffer.position(); x < limit; x++)
		{
			i = ((i + 1) & Integer.MAX_VALUE) & 0xFF;
			j = ((j + (S[i] & 0xFF)) & Integer.MAX_VALUE) & 0xFF;
			swap(S, i, j);
			int k = S[((S[i] & 0xFF) + (S[j] & 0xFF)) & 0xFF] & 0xFF;
			buffer.put(x, (byte) (buffer.get(x) ^ k));
		}
	}
	
	@Override
	public final void reset() {
		initialized = false;
	}
	
	public static byte[] decrypt(byte[] data, byte[] key) {
		return rc4(data, key);
	}
	
	public static byte[] encrypt(byte[] data, byte[] key) {
		return rc4(data, key);
	}
	
	private static byte[] rc4(byte[] data, byte[] key) {
		if (!isKeyValid(key)) throw new IllegalArgumentException("key is fail!");
		if (data.length < 1) throw new IllegalArgumentException("data is fail!");
		int[] S = new int[256];
		
		// KSA
		for (int i = 0; i < S.length; i++)
			S[i] = i;
		int j = 0;
		for (int i = 0; i < S.length; i++)
		{
			j = (j + S[i] + (key[i % key.length] & 0xFF)) & 0xFF;
			swap(S, i, j);
		}
		
		// PRGA
		int i = 0;
		j = 0;
		
		byte[] encodeData = new byte[data.length];
		
		for (int x = 0; x < encodeData.length; x++)
		{
			i = (i + 1) & 0xFF;
			j = (j + S[i]) & 0xFF;
			swap(S, i, j);
			int k = S[(S[i] + S[j]) & 0xFF];
			int K = (int) k;
			encodeData[x] = (byte) (data[x] ^ K);
		}
		return encodeData;
	}
	
	public static boolean isKeyValid(byte[] key) {
		byte[] bKey = key;
		int len = bKey.length;
		int num = 0;// 0x0E计数
		if (len > 0 && len <= 256)
		{
			for (int i = 0; i < len; i++)
			{
				if ((bKey[i] & 0xFF) == 0x0E)
				{
					num++;
					if (num > 3) return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public static void swap(int[] source, int a, int b) {
		int tmp = source[a];
		source[a] = source[b];
		source[b] = tmp;
	}
	
	public static void swap(byte[] source, int a, int b) {
		byte tmp = source[a];
		source[a] = source[b];
		source[b] = tmp;
	}
}
