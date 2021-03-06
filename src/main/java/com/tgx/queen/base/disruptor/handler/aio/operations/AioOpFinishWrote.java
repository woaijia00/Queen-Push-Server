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
package com.tgx.queen.base.disruptor.handler.aio.operations;

import java.nio.channels.NotYetConnectedException;
import java.nio.channels.WritePendingException;

import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.socket.aio.impl.AioSession;
import com.tgx.queen.socket.aio.impl.AioWriteHandler;


/**
 * @author Zhangzhuo
 */
public enum AioOpFinishWrote
        implements
        EventOp
{
	INSTANCE(false),
	ERROR(true);
	
	@Override
	public void onTranslate(final Event event) {
		
	}
	
	@Override
	public EventOp op(final Event event) {
		AioSession session = event.session;
		try
		{
			session.writeNext(AioWriteHandler.INSTANCE);
		}
		catch (WritePendingException | NotYetConnectedException e)
		{
			System.out.println(session);
			e.printStackTrace();
		}
		catch (Exception e)
		{
			System.out.println(session);
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public EventOp errOp(final Event event) {
		System.out.println("finish -- 0 Ingore");
		return null;
	}
	
	@Override
	public boolean hasError() {
		return hasError;
	}
	
	private boolean hasError;
	
	private AioOpFinishWrote(boolean hasError) {
		this.hasError = hasError;
	}
	
	@Override
	public int getSerialNum() {
		return AIO_WROTE_SERIAL;
	}
}
