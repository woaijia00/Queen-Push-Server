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
package com.tgx.queen.socket.aio.impl;

import java.io.EOFException;
import java.nio.channels.CompletionHandler;

import com.tgx.queen.base.disruptor.handler.aio.operations.AioOpClosed;
import com.tgx.queen.base.disruptor.handler.aio.operations.AioOpRead;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.disruptor.handler.inf.EventOp.Error;
import com.tgx.queen.io.inf.IQoS;


public enum AioReadHandler
        implements
        CompletionHandler<Integer, AioSession>
{
	CM_INSTANCE(AioOpRead.CM_SERVER_READ),
	IM_INSTANCE(AioOpRead.IM_SERVER_READ),
	CM_CLIENT(AioOpRead.CM_CLIENT_READ), //测试客户端用的
	IM_CLIENT(AioOpRead.IM_CLIENT_READ);//CM 向 IM 进行连接用的
	
	@Override
	public void completed(Integer result, AioSession session) {
		AioWorker worker = (AioWorker) Thread.currentThread();
		switch (result) {
			case -1:
				worker.publish(session, null, AioOpClosed.INSTANCE, new EOFException("read EOF!"), Error.AIO_READ_EOF);
				break;
			case 0:
				//worker.publish(session, opRead, Error.AIO_READ_ZERO);
				session.readNext(this);
				break;
			default:
				IQoS recvContent;
				try
				{
					while ((recvContent = (IQoS) session.read()) != null)
						worker.publish(session, recvContent, opRead, null, Error.NO_ERROR);
					session.readNext(this);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					worker.publish(session, null, AioOpClosed.INSTANCE, e, Error.AIO_READ_ERROR);
				}
				break;
		}
	}
	
	@Override
	public void failed(Throwable exc, AioSession session) {
		AioWorker worker = (AioWorker) Thread.currentThread();
		// 此处发布的事件由 channelHandler进行处理
		if (!session.isClosed()) worker.publish(session, null, AioOpClosed.INSTANCE, exc, Error.AIO_READ_ERROR);
	}
	
	private AioReadHandler(EventOp readOp) {
		this.opRead = readOp;
	}
	
	private EventOp opRead;
}
