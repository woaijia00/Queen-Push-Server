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
import com.tgx.queen.io.bean.protocol.impl.X12_Logout;
import com.tgx.queen.io.bean.protocol.impl.X14_Disconnet;
import com.tgx.queen.socket.aio.inf.ISessionManager;


/**
 * @author Zhangzhuo
 */
public enum TgxOpImLogout
        implements
        EventOp
{
	
	INSTANCE;
	
	@Override
	public void onTranslate(final Event event) {
	}
	
	@Override
	public EventOp op(final Event event) {
		ISessionManager sm = event.session.getMyManager();
		long clientIndex = -1;
		switch (event.attach.getSerialNum()) {
			case X12_Logout.COMMAND:
				X12_Logout x12 = (X12_Logout) event.attach;
				clientIndex = x12.clientIndex;
				break;
			case X14_Disconnet.COMMAND:
				X14_Disconnet x14 = (X14_Disconnet) event.attach;
				clientIndex = x14.clientIndex;
				break;
			default:
				return null;
		}
		if (event.session.isClosed() || clientIndex == event.session.getIndex())
		{
			sm.clearSession(clientIndex, event.session);
			sm.rmSession(event.session);
		}
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
		return TGX_IM_LOGOUT_SERIAL;
	}
	
}
