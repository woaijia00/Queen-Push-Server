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
package com.tgx.queen.base.disruptor.handler.logic.operations;

import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.socket.aio.impl.AioReadHandler;
import com.tgx.queen.socket.aio.inf.ISessionManager;


/**
 * @author Zhangzhuo
 */
public enum TgxOpSessionCreate
        implements
        EventOp
{
	INSTANCE_CLIENT,
	
	INSTANCE_SERVER;
	
	@Override
	public void onTranslate(final Event event) {
		if (handshake) event.session.getContext().handshake();
		else event.session.getContext().noHandshake();
	}
	
	@Override
	public EventOp op(final Event event) {
		ISessionManager sm = event.session.getMyManager();
		sm.addSession(event.session);
		event.session.readNext(readHandler);
		//由于连接成功之后需要执行的只有握手过程，所以 特定实现 TgxOpWrite 就可以满足使用
		if (event.attach != null) return TgxOpWrite.INSTANCE;
		return null;
	}
	
	@Override
	public EventOp errOp(final Event event) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean hasError() {
		return false;
	}
	
	@Override
	public int getSerialNum() {
		return TGX_CREATE_SESSION_SERIAL;
	}
	
	private AioReadHandler readHandler;
	
	public TgxOpSessionCreate setReadHandler(AioReadHandler handler) {
		readHandler = handler;
		return this;
	}
	
	private boolean handshake;
	
	public TgxOpSessionCreate setHandshake() {
		handshake = true;
		return this;
	}
	
	public TgxOpSessionCreate setNoHandshake() {
		handshake = true;
		return this;
	}
}
