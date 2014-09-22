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

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.tgx.queen.base.disruptor.handler.aio.operations.AioOpAccept;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.socket.aio.inf.IAioServer;


public enum AioAcceptor
        implements
        CompletionHandler<AsynchronousSocketChannel, IAioServer>
{
	INSTANCE;
	
	@Override
	public void completed(AsynchronousSocketChannel result, IAioServer server) {
		AioWorker worker = (AioWorker) Thread.currentThread();
		AioSession session = null;
		try
		{
			//此处设计认为在单一端口连接应答条件下,不应该规划不同Filter 用于应答链路
			session = server.createSession(result, filter);
			worker.publish(session, AioOpAccept.INSTANCE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			worker.publish(session, AioOpAccept.ERROR, e, EventOp.Error.AIO_ACCEPT_ERROR);
		}
		finally
		{
			server.pendingAccept();
		}
	}
	
	@Override
	public void failed(Throwable exc, IAioServer server) {
		AioWorker worker = (AioWorker) Thread.currentThread();
		worker.publish(null, AioOpAccept.ERROR, exc, EventOp.Error.AIO_ACCEPT_ERROR);
		server.pendingAccept();
	}
	
	private AioFilterChain filter;
	
	public AioAcceptor setFilter(AioFilterChain filter) {
		this.filter = filter;
		return this;
	}
}
