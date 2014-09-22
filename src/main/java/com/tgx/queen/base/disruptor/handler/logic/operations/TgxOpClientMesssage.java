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
import com.tgx.queen.client.inf.IClientMessagePrinter;
import com.tgx.queen.io.bean.protocol.impl.X00_EncrytRequest;
import com.tgx.queen.io.bean.protocol.impl.X05_Redirect;
import com.tgx.queen.io.bean.protocol.impl.X07_ResponseClientID;
import com.tgx.queen.io.bean.protocol.impl.X100_HandShake;
import com.tgx.queen.io.bean.protocol.impl.X102_Ping;
import com.tgx.queen.io.bean.protocol.impl.X103_Pong;
import com.tgx.queen.io.bean.protocol.impl.X11_LoginResult;
import com.tgx.queen.io.bean.protocol.impl.X21_VerifyMsgArrived;
import com.tgx.queen.io.bean.protocol.impl.X23_RouteMsg;
import com.tgx.queen.io.bean.protocol.impl.X25_VerfiyRouteConfirm;
import com.tgx.queen.io.bean.protocol.impl.X27_RSyncMsgStatus;
import com.tgx.queen.io.bean.protocol.impl.X31_PushMsg;
import com.tgx.queen.io.bean.protocol.impl.X33_VerfiyPushConfirm;


/**
 * @author Zhangzhuo
 */
public enum TgxOpClientMesssage
        implements
        EventOp
{
	INSTANCE;
	
	@Override
	public void onTranslate(final Event event) {
		
	}
	
	static int regcnt   = 0;
	static int msgcnt   = 0;
	static int logincnt = 0;
	
	@Override
	public EventOp op(final Event event) {
		switch (event.attach.getSerialNum()) {
			case X05_Redirect.COMMAND:
			case X07_ResponseClientID.COMMAND:
				System.out.println("recieve x07 --- " + ++regcnt);
				break;
			case X11_LoginResult.COMMAND:
				System.out.println("recieve x11 --- " + ++logincnt + "  response code - " + ((X11_LoginResult) event.attach).responseCode);
				break;
			case X21_VerifyMsgArrived.COMMAND:
			case X23_RouteMsg.COMMAND:
			case X25_VerfiyRouteConfirm.COMMAND:
			case X27_RSyncMsgStatus.COMMAND:
			case X31_PushMsg.COMMAND:
				System.out.println(" recieve x31 --- " + ++msgcnt);
				break;
			case X33_VerfiyPushConfirm.COMMAND:
				printer.println("RECV:" + event.attach);
				break;
			case X100_HandShake.SerialNum:
				event.attach = new X00_EncrytRequest();
				//event.attach = null;
				break;
			case X102_Ping.SerialNum:
				event.attach = new X103_Pong(null);
				break;
			case X103_Pong.SerialNum:
				event.attach = null;
				break;
		
		}
		return TgxOpWrite.INSTANCE;
	}
	
	int i = 0;
	
	@Override
	public EventOp errOp(final Event event) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean hasError() {
		return false;
	}
	
	private IClientMessagePrinter printer;
	
	public void setPrinter(IClientMessagePrinter msgPrinter) {
		this.printer = msgPrinter;
	}
	
	@Override
	public int getSerialNum() {
		return TGX_CM_CLIENT_SERIAL;
	}
	
}
