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

import java.util.Random;


public class MixUtil
{
	private final static Random RANDOM = new Random();
	
	public final static long mixIndex(byte[] token) {
		if (token == null || token.length != 16) return -1;
		byte[] mask1 = new byte[8];
		byte[] mask2 = new byte[8];
		for (int i = 0, p; i < 16; i += 2)
		{
			p = i >> 1;
			mask1[p] = token[i];
			mask2[p] = token[i + 1];
		}
		return (IoUtil.readLong(mask1, 0) ^ IoUtil.readLong(mask2, 0)) & 0xFFFFFFFFFFFFL;
	}
	
	/**
	 * @param index
	 *            0~0xFFFFFFFFFFFFFL 才是有效数据其他的都无法生效
	 * @return
	 */
	public final static byte[] mixToken(long index) {
		byte[] mix = new byte[16];
		do
		{
			RANDOM.setSeed(System.currentTimeMillis() ^ index);
			long mix1 = RANDOM.nextLong();
			long r = (RANDOM.nextLong() & 0xFFFF000000000000L) | index;
			long mix2 = mix1 ^ r;
			for (int i = 56, j = 0; i >= 0; i -= 8)
			{
				mix[j++] = (byte) (mix1 >>> i);
				mix[j++] = (byte) (mix2 >>> i);
			}
		}
		while (mixIndex(mix) != index);
		return mix;
	}
	
	public final static long mixAddrIndex(byte[] src, int pos) {
		if (src == null || src.length < 8 || src.length < pos + 8) return -1;
		byte[] mask1 = new byte[4];
		byte[] mask2 = new byte[4];
		for (int i = 0, p, len = pos + 8; i < len; i += 2)
		{
			p = i >> 1;
			mask1[p] = src[i];
			mask2[p] = src[i + 1];
		}
		return (IoUtil.readInt(mask1, 0) ^ IoUtil.readInt(mask2, 0)) & 0xFFFFFFFFL;
		
	}
	
	public final static long mixAddr(long usrIndex) {
		byte[] mix = new byte[8];
		RANDOM.setSeed(System.currentTimeMillis() ^ usrIndex);
		int mix1 = RANDOM.nextInt();
		int r = (int) usrIndex;
		int mix2 = mix1 ^ r;
		for (int i = 24, j = 0; i >= 0; i -= 8)
		{
			mix[j++] = (byte) (mix1 >>> i);
			mix[j++] = (byte) (mix2 >>> i);
		}
		return IoUtil.readLong(mix, 0);
	}
}
