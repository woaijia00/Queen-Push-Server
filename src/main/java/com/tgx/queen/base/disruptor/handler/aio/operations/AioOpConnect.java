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

import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpSessionCreate;
import com.tgx.queen.socket.aio.impl.AioConnector;
import com.tgx.queen.socket.aio.inf.IConnectCallBack;


public enum AioOpConnect
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
		if (connectCallBack != null) connectCallBack.onSessionCreated(event);
		return TgxOpSessionCreate.INSTANCE_CLIENT;
	}
	
	@Override
	public EventOp errOp(final Event event) {
		if (event.ex != null) event.ex.printStackTrace();
		if (connectCallBack != null && event.errAttach instanceof AioConnector)
		{
			AioConnector connector = (AioConnector) event.attach;
			connectCallBack.onConnectFaild(connector);
		}
		return null;
	}
	
	@Override
	public boolean hasError() {
		return hasError;
	}
	
	private boolean hasError;
	
	private AioOpConnect(boolean hasError) {
		this.hasError = hasError;
	}
	
	private IConnectCallBack connectCallBack;
	
	public void setCallBack(IConnectCallBack callback) {
		connectCallBack = callback;
	}
	
	@Override
	public int getSerialNum() {
		return AIO_CONNECT_SERIAL;
	}
}
