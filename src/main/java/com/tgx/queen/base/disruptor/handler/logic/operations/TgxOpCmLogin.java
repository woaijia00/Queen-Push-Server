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
import com.tgx.queen.base.util.MixUtil;
import com.tgx.queen.cm.CmServerNode;
import com.tgx.queen.io.bean.protocol.impl.X10_Login;
import com.tgx.queen.io.bean.protocol.impl.X50_CM_Login;


/**
 * @author Zhangzhuo
 */
public enum TgxOpCmLogin
        implements
        EventOp
{
	INSTANCE;
	
	@Override
	public void onTranslate(final Event event) {
		
	}
	
	@Override
	public EventOp op(final Event event) {
		if (event.session.isClosed()) return null;//当 close 先处理完结束，则跳过本次操作
		if (event.attach != null) switch (event.attach.getSerialNum()) {
			case X10_Login.COMMAND:
				X10_Login x10 = (X10_Login) event.attach;
				long clientIndex = MixUtil.mixIndex(x10.client_token);
				long usrIndex = MixUtil.mixIndex(x10.usr_token);
				if (event.session.getIndex() == -1 || event.session.getIndex() == clientIndex)
				{
					X50_CM_Login x50 = new X50_CM_Login();
					x50.clientIndex = clientIndex;
					x50.usrIndex = usrIndex;
					x50.cmIndex = CmServerNode.getCmIndex();
					System.out.println("-- -- login-- " + clientIndex);
				}
				else
				{
					//Ignore login with different clientIndex 
					return null;
				}
				return TgxOpCmWrite.INSTANCE;
			default:
				return null;
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
		return TGX_CM_LOGIN_SERIAL;
	}
	
}
