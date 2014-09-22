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

public class TgxRingBuffer<T>
{
	private T[]  storage;
	private long size;
	private long sequence = -1;
	private long gating   = -1;
	
	public TgxRingBuffer(int bit_pos) {
		size = 1 << bit_pos;
	}
	
	public final long size() {
		return size;
	}
	
	public TgxRingBuffer<T> init(T[] ext) {
		storage = ext;
		return this;
	}
	
	public T get(int sequence) {
		int index = (int) (sequence & (size - 1));
		return storage[index];
	}
	
	public boolean add(T t) {
		if (next(1) < 0) return false;
		int index = (int) (sequence & (size - 1));
		storage[index] = t;
		return true;
	}
	
	public T lruAdd(T t) {
		T r = null;
		while (next(1) < 0)
			r = remove();
		int index = (int) (sequence & (size - 1));
		storage[index] = t;
		return r;
	}
	
	public T remove() {
		if (gating < sequence)
		{
			int index = (int) (gating & (size - 1));
			gating++;
			T t = storage[index];
			storage[index] = null;
			return t;
		}
		return null;
	}
	
	public long next(int n) {
		long nextValue = sequence;
		long nextSequence = nextValue + n;
		long wrapPoint = nextSequence - size;
		long cachedGatingSequence = gating;
		if (wrapPoint > cachedGatingSequence || cachedGatingSequence > nextValue) { return -1; }
		return sequence = nextSequence;
	}
	
	public boolean isEmpty() {
		return sequence - gating == 0;
	}
	
	public void clear() {
		for (int i = 0; i < size; i++)
			storage[i] = null;
		storage = null;
		sequence = -1;
		gating = -1;
	}
}
