/**
 * - Copyright (c) 2013 Zhang Zhuo All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.tgx.queen.base.util;

import java.util.HashSet;
import java.util.TreeSet;


/**
 * @author Zhangzhuo
 */
public class TestSkipSet
{
	public static void main(String... a) {
		SkipSet<Integer> set = new SkipSet<>(16);
		long t = System.currentTimeMillis();
		for (int j = 0, i = 0, x = 0; j < 200; j++)
		{
			t = System.currentTimeMillis();
			for (i = 0; i < 10000; i++, x++)
			{
				set.insert(x);
			}
			System.out.println(j + " size:" + set.size() + " deta: " + (System.currentTimeMillis() - t));
		}
		set.clear();
		System.out.println("wait jconsole");
		
		TreeSet<Integer> tSet = new TreeSet<Integer>();
		for (int j = 0, i = 0, x = 0; j < 200; j++)
		{
			t = System.currentTimeMillis();
			for (i = 0; i < 10000; i++, x++)
			{
				tSet.add(x);
			}
			System.out.println(j + " tSize:" + tSet.size() + " deta: " + (System.currentTimeMillis() - t));
		}
		tSet.clear();
		System.out.println("wait jconsole");
		
		HashSet<Integer> hSet = new HashSet<Integer>();
		for (int j = 0, i = 0, x = 0; j < 200; j++)
		{
			t = System.currentTimeMillis();
			for (i = 0; i < 10000; i++, x++)
			{
				hSet.add(x);
			}
			System.out.println(j + " tSize:" + hSet.size() + " deta: " + (System.currentTimeMillis() - t));
		}
		hSet.clear();
		System.out.println("wait jconsole");
	}
}
