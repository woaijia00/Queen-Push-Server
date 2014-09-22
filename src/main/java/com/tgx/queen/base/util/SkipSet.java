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

import java.util.Iterator;


/*
 * The authors of this work have released all rights to it and placed it
 * in the public domain under the Creative Commons CC0 1.0 waiver
 * (http://creativecommons.org/publicdomain/zero/1.0/).
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * Retrieved from: http://en.literateprograms.org/Skip_list_(Java)?oldid=15959
 */

class SkipNode<E extends Comparable<? super E>>

{
	public final E             value;
	public final SkipNode<E>[] forward; // array of pointers
	                                    
	@SuppressWarnings ("unchecked")
	public SkipNode(int level, E value) {
		forward = new SkipNode[level + 1];
		this.value = value;
	}
	
	public SkipNode<E> dispose() {
		SkipNode<E> next = forward[0];
		for (int i = 0, size = forward.length; i < size; i++)
			forward[i] = null;
		return next;
	}
}

public class SkipSet<E extends Comparable<? super E>>
        implements
        Iterator<E>,
        Iterable<E>
{
	public static final double P = 0.5;
	public final int           MAX_LEVEL;
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0 || header.forward[0] == null;
	}
	
	private int size = 0;
	
	public final int randomLevel() {
		int lvl = (int) (Math.log(1. - Math.random()) / Math.log(1. - P));
		return Math.min(lvl, MAX_LEVEL);
	}
	
	public SkipNode<E> header;
	public SkipNode<E> iterator;
	public int         top;
	
	public SkipSet() {
		this(5);
	}
	
	public SkipSet(int max) {
		MAX_LEVEL = max;
		header = new SkipNode<E>(MAX_LEVEL, null);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		SkipNode<E> x = header.forward[0];
		while (x != null)
		{
			sb.append(x.value);
			x = x.forward[0];
			if (x != null) sb.append(",");
		}
		sb.append("}");
		return sb.toString();
	}
	
	public boolean contains(E searchValue) {
		return find(searchValue) != null;
	}
	
	public E find(E searchValue) {
		if (isEmpty() || searchValue == null) return null;
		SkipNode<E> x = header;
		for (int i = top; i >= 0; i--)
		{
			while (x.forward[i] != null && x.forward[i].value.compareTo(searchValue) < 0)
			{
				x = x.forward[i];
			}
		}
		x = x.forward[0];
		return x != null && x.value.compareTo(searchValue) == 0 ? x.value : null;
	}
	
	@SuppressWarnings ("unchecked")
	public void insert(E value) {
		SkipNode<E> x = header;
		SkipNode<E>[] update = new SkipNode[MAX_LEVEL + 1];
		
		for (int i = top; i >= 0; i--)
		{
			while (x.forward[i] != null && x.forward[i].value.compareTo(value) < 0)
			{
				x = x.forward[i];
			}
			update[i] = x;
		}
		x = x.forward[0];
		
		if (x == null || !x.value.equals(value))
		{
			int lvl = randomLevel();
			
			if (lvl > top)
			{
				for (int i = top + 1; i <= lvl; i++)
				{
					update[i] = header;
				}
				top = lvl;
			}
			x = new SkipNode<E>(lvl, value);
			for (int i = 0; i <= lvl; i++)
			{
				x.forward[i] = update[i].forward[i];
				update[i].forward[i] = x;
			}
			size++;
		}
	}
	
	@SuppressWarnings ("unchecked")
	public void add(E value) {
		SkipNode<E> x = header;
		SkipNode<E>[] update = new SkipNode[MAX_LEVEL + 1];
		for (int i = top; i >= 0; i--)
		{
			while (x.forward[i] != null && x.forward[i].value.compareTo(value) < 0)
			{
				x = x.forward[i];
			}
			update[i] = x;
		}
		x = x.forward[0];
		if (x == null || !x.value.equals(value))
		{
			int lvl = randomLevel();
			if (lvl > top)
			{
				for (int i = top + 1; i <= lvl; i++)
				{
					update[i] = header;
				}
				top = lvl;
			}
			x = new SkipNode<E>(lvl, value);
			for (int i = 0; i <= lvl; i++)
			{
				x.forward[i] = update[i].forward[i];
				update[i].forward[i] = x;
			}
		}
		else
		{
			int x_lv = x.forward.length - 1;
			x = new SkipNode<E>(x_lv, value);
			for (int i = 0; i <= x_lv; i++)
			{
				x.forward[i] = update[i].forward[i];
				update[i].forward[i] = x;
			}
		}
		size++;
	}
	
	@SuppressWarnings ("unchecked")
	public void remove(E value) {
		if (isEmpty() || value == null) return;
		SkipNode<E> x = header;
		SkipNode<E>[] update = new SkipNode[MAX_LEVEL + 1];
		for (int i = top; i >= 0; i--)
		{
			while (x.forward[i] != null && x.forward[i].value.compareTo(value) < 0)
			{
				x = x.forward[i];
			}
			update[i] = x;
		}
		x = x.forward[0];
		if (x == null) return;//no find value
		if (x.value.equals(value))
		{
			for (int i = 0; i <= top; i++)
			{
				if (update[i].forward[i] != x) break;
				update[i].forward[i] = x.forward[i];
			}
			while (top > 0 && header.forward[top] == null)
				top--;
			size--;
			x.dispose();
		}
	}
	
	public E removeFirst() {
		if (isEmpty()) return null;
		SkipNode<E> x = header.forward[0];
		E v = x != null ? x.value : null;
		remove(v);//TODO 优化这个操作
		return v;
	}
	
	public E peek() {
		if (isEmpty()) return null;
		SkipNode<E> x = header.forward[0];
		E v = x != null ? x.value : null;
		return v;
	}
	
	public void clear() {
		if (isEmpty() || header.forward[0] == null) return;
		SkipNode<E> x = header;
		while (x.forward[0] != null)
			x = x.dispose();
		size = 0;
	}
	
	@Override
	public Iterator<E> iterator() {
		iterator = header;
		return this;
	}
	
	@Override
	public boolean hasNext() {
		return iterator.forward[0] != null && iterator.forward[0].value != null;
	}
	
	@Override
	public E next() {
		E r = iterator.forward[0].value;
		iterator = iterator.forward[0];
		return r;
	}
	
	@Override
	public void remove() {
		remove(iterator.value);
	}
	
	public void toArray(E[] array) {
		int i = 0;
		for (E e : this)
			array[i++] = e;
	}
}
