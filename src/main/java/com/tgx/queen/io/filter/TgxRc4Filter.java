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
package com.tgx.queen.io.filter;

import java.nio.ByteBuffer;

import com.tgx.queen.socket.aio.impl.AioFilterChain;
import com.tgx.queen.socket.aio.impl.AioSession;
import com.tgx.queen.socket.aio.inf.ITLSContext;
import com.tgx.queen.socket.aio.inf.ITLSContext.EncryptState;


public class TgxRc4Filter
        extends
        AioFilterChain
{
	
	public TgxRc4Filter(AioFilterChain filter) {
		super(name);
		linkFront(filter);
	}
	
	public final static String name = "tgx-rc4-fliter";
	
	@Override
	public ResultType preEncode(AioSession session, Object content) {
		if (content == null) return ResultType.NOT_OK;
		ITLSContext context = session.getContext();
		ResultType resultType;
		if (context.outState().equals(EncryptState.ENCRYPTED)) resultType = ResultType.HANDLED;
		else
		{
			resultType = ResultType.IGNORE;
			if (context.needUpdateKeyOut())
			{
				context.swapKeyOut(context.getReRollKey());
				context.getEncryptRc4().reset();
				context.cryptOut();
			}
		}
		return resultType;
	}
	
	@Override
	public Object encode(AioSession session, Object content, ResultType preResult) throws Exception {
		ITLSContext context = session.getContext();
		if (content instanceof ByteBuffer)
		{
			context.getEncryptRc4().rc4Stream((ByteBuffer) content, context.getRc4KeyOut());
			if (context.needUpdateKeyOut())
			{
				context.getEncryptRc4().reset();
				context.swapKeyOut(context.getReRollKey());
			}
		}
		return content;
	}
	
	@Override
	public ResultType preDecode(AioSession session, Object content) {
		if (content == null) return ResultType.NOT_OK;
		ITLSContext context = session.getContext();
		if (context.needUpdateKeyIn())
		{
			context.swapKeyIn(context.getReRollKey());
			context.getDecryptRc4().reset();
			context.cryptIn();
		}
		return context.inState().equals(EncryptState.PLAIN) ? ResultType.IGNORE : ResultType.OK;
	}
	
	@Override
	public Object decode(AioSession session, Object content, ResultType preResult) throws Exception {
		ITLSContext context = session.getContext();
		if (content instanceof ByteBuffer)
		{
			ByteBuffer buf = (ByteBuffer) content;
			if (context.needUpdateKeyIn())
			{
				context.swapKeyIn(context.getReRollKey());
				context.getDecryptRc4().reset();
			}
			if (buf.position() == 0) context.getDecryptRc4().rc4Stream(buf, context.getRc4KeyIn());
		}
		return content;
	}
}
