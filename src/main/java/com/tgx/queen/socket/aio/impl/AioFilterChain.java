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
package com.tgx.queen.socket.aio.impl;

import java.nio.ByteBuffer;

import com.tgx.queen.base.classic.task.inf.ITaskResult;
import com.tgx.queen.base.inf.IDisposable;


public abstract class AioFilterChain
        implements
        IDisposable
{
	public static enum ResultType
	{
		NOT_OK,
		NEED_DATA,
		OK,
		IGNORE,
		HANDLED,
		INSIDE
	};
	
	public static enum DecodeResult
	{
		IGNORE,
		NEED_DATA,
		NEXT_STEP
	};
	
	protected String         name;
	protected AioFilterChain nextFilter;
	protected AioFilterChain preFilter;
	protected boolean        isManual;
	
	/**
	 * 默认为单次使用过滤器,将在{@link #dispose(boolean)}中完成注销
	 * 
	 * @author Zhangzhuo
	 * @param name
	 *            处理器名称,对过滤器进行增删时的标示,需唯一化
	 * @see #IoFilter(String, boolean)
	 */
	public AioFilterChain(String name) {
		this.name = name;
	}
	
	/**
	 * @author Zhangzhuo
	 * @param name
	 *            处理器名称,对过滤器进行增删时的标示,需唯一化
	 * @param isManual
	 *            是否手动注销过滤器实例.如果需要对过滤器进行重用需要指定为true
	 */
	public AioFilterChain(String name, boolean isManual) {
		this.name = name;
		this.isManual = isManual;
	}
	
	public AioFilterChain getChainHead() {
		AioFilterChain filter = preFilter;
		while (filter != null && filter.preFilter != null)
			filter = filter.preFilter;
		return filter == null ? this : filter;
	}
	
	public AioFilterChain getChainTail() {
		AioFilterChain filter = nextFilter;
		while (filter != null && filter.nextFilter != null)
			filter = filter.nextFilter;
		return filter == null ? this : filter;
	}
	
	public AioFilterChain getChainHead(String name) {
		if (name == null) throw new NullPointerException();
		if (name.equals(this.name)) return this;
		AioFilterChain filter = preFilter;
		while (filter != null && filter.preFilter != null && !filter.name.equals(name))
			filter = filter.preFilter;
		return filter == null ? this : filter;
	}
	
	public AioFilterChain getChainTail(String name) {
		if (name == null) throw new NullPointerException();
		if (name.equals(this.name)) return this;
		AioFilterChain filter = nextFilter;
		while (filter != null && filter.nextFilter != null && !filter.name.equals(name))
			filter = filter.nextFilter;
		return filter == null ? this : filter;
	}
	
	/**
	 * Encoder 过滤器链
	 * 
	 * @author Zhangzhuo
	 * @param aIoSession
	 *            已建立的处理管道 以及携带处理过程中的Context
	 * @param content
	 *            需要进行处理的目标数据,贯穿处理链的中间过程的状态存单
	 * @return 已通过处理链完成的处理结果值
	 * @throws NullPointerException
	 *             当context 为空时抛出此异常
	 * @throws Exception
	 *             执行过程中可能出现的其他异常
	 * @see #preEncode(IConnection, Object)
	 */
	public final ByteBuffer filterChainEncode(AioSession aIoSession, Object content) throws Exception {
		if (content == null) throw new NullPointerException("Nothing to encode!");
		ResultType resultType = ResultType.OK;
		AioFilterChain preFilter = this;
		while (preFilter != null)
		{
			resultType = preFilter.preEncode(aIoSession, content);
			switch (resultType) {
				case NOT_OK:
					return null;
				case NEED_DATA:
					return null;
				case OK:
				case IGNORE:
					break;
				case HANDLED:
				case INSIDE:
					content = preFilter.encode(aIoSession, content, resultType);
					break;
			}
			preFilter = preFilter.preFilter;
		}
		return (ByteBuffer) content;
	}
	
	/**
	 * Decoder 过滤器链
	 * 
	 * @author Zhangzhuo
	 * @param aIoSession
	 *            已建立的处理管道 负责携带处理过程中的context
	 * @param content
	 *            需要进行处理的目标数据,贯穿处理链的中间过程的状态存单
	 * @return 已通过处理链完成的处理结果,符合<code>ITaskResult</code>接口规范
	 * @throws Exception
	 *             执行过程中可能出现的异常
	 * @see {@link ITaskResult}
	 */
	public final Object filterChainDecode(AioSession aIoSession, Object content) throws Exception {
		ResultType resultType = null;
		Object result = null;
		AioFilterChain nextFilter = this;
		DOHANDLE:
		{
			while (nextFilter != null)
			{
				resultType = nextFilter.preDecode(aIoSession, content);
				switch (resultType) {
					case NOT_OK:
						return null;
					case NEED_DATA:
						return null;
					case OK:
						result = content = nextFilter.decode(aIoSession, content, resultType);
						break;
					case IGNORE:
						result = null;
						break;
					case HANDLED:// 当前结果已抵达最终完成位置
					case INSIDE:
						result = nextFilter.decode(aIoSession, content, resultType);
						break DOHANDLE;
				}
				nextFilter = nextFilter.nextFilter;
			}
		}
		return result;
	}
	
	public final Object filterDecode(AioSession aIoSession, Object content) throws Exception {
		ResultType resultType = null;
		Object result = null;
		resultType = preDecode(aIoSession, content);
		switch (resultType) {
			case NOT_OK:
				return null;
			case NEED_DATA:
				return null;
			case OK:
				result = content = nextFilter.decode(aIoSession, content, resultType);
				break;
			case IGNORE:
				result = null;
				break;
			case HANDLED:// 当前结果已抵达最终完成位置
			case INSIDE:
				result = nextFilter.decode(aIoSession, content, resultType);
				break;
		}
		return result;
	}
	
	protected final void linkAfter(AioFilterChain curFilter) {
		if (curFilter == null) return;
		AioFilterChain filter = curFilter.nextFilter;
		curFilter.nextFilter = this;
		preFilter = curFilter;
		nextFilter = filter;
	}
	
	protected final void linkFront(AioFilterChain curFilter) {
		if (curFilter == null) return;
		AioFilterChain filter = curFilter.preFilter;
		curFilter.preFilter = this;
		preFilter = filter;
		nextFilter = curFilter;
	}
	
	public final static void insertBeforeFilter(AioFilterChain curFilter, String preName, String name, AioFilterChain filter) {
		if (name == null) throw new NullPointerException("filter name can't be NULL");
		if (curFilter != null)
		{
			AioFilterChain preFilter = curFilter.preFilter;
			if (curFilter.name.equals(preName))
			{
				filter.nextFilter = curFilter;
				curFilter.preFilter = filter;
				filter.preFilter = preFilter;
				if (preFilter != null) preFilter.nextFilter = filter;
			}
			else
			{
				while (preFilter.preFilter != null && !preFilter.name.equals(preName))
				{
					preFilter = preFilter.preFilter;
				}
				if (preFilter.preFilter == null)
				{
					preFilter.preFilter = filter;
					filter.nextFilter = preFilter;
				}
				else
				{
					filter.preFilter = preFilter.preFilter;
					preFilter.preFilter.nextFilter = filter;
					filter.nextFilter = preFilter;
					preFilter.preFilter = filter;
				}
			}
		}
		filter.name = name;
	}
	
	public final static void insertAfterFilter(AioFilterChain curFilter, String nextName, String name, AioFilterChain filter) {
		if (name == null) throw new NullPointerException("filter name can't be NULL");
		if (curFilter != null)
		{
			AioFilterChain afterFilter = curFilter.nextFilter;
			if (curFilter.name.equals(nextName))
			{
				curFilter.nextFilter = filter;
				filter.preFilter = curFilter;
				filter.nextFilter = afterFilter;
				afterFilter.preFilter = filter;
			}
			else
			{
				
				while (afterFilter.nextFilter != null && !afterFilter.name.equals(nextName))
				{
					afterFilter = afterFilter.nextFilter;
				}
				if (afterFilter.nextFilter == null)
				{
					afterFilter.nextFilter = filter;
					filter.preFilter = afterFilter;
				}
				else
				{
					afterFilter.nextFilter.preFilter = filter;
					filter.nextFilter = afterFilter.nextFilter;
					filter.preFilter = afterFilter;
					afterFilter.nextFilter = filter;
				}
			}
		}
		filter.name = name;
	}
	
	@Override
	public void dispose() {
		AioFilterChain filter;
		while (nextFilter != null)
		{
			filter = nextFilter.nextFilter;
			nextFilter.nextFilter = null;
			nextFilter = filter;
		}
	}
	
	@Override
	public boolean isDisposable() {
		return true;
	}
	
	public abstract ResultType preEncode(AioSession aIoSession, Object content);
	
	public abstract ResultType preDecode(AioSession aIoSession, Object content);
	
	public abstract Object encode(AioSession aIoSession, Object content, ResultType preResult) throws Exception;
	
	public abstract Object decode(AioSession aIoSession, Object content, ResultType preResult) throws Exception;
	
	public void endDecode() {
		
	}
}
