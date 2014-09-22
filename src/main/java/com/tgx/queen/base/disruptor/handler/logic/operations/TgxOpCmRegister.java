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
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.base.util.MixUtil;
import com.tgx.queen.db.impl.push.TgxClientDao;
import com.tgx.queen.io.bean.protocol.impl.X06_RequestClientID;
import com.tgx.queen.io.bean.protocol.impl.X07_ResponseClientID;
import com.tgx.queen.push.bean.TgxClient;


/**
 * @author Zhangzhuo
 */
public enum TgxOpCmRegister
        implements
        EventOp
{
	INSTANCE;
	
	@Override
	public void onTranslate(final Event event) {
		
	}
	
	private final TgxClientDao clientDao = TgxClientDao.getInstance();
	
	@Override
	public EventOp op(final Event event) {
		TgxClient client = null;
		if (event.attach != null) switch (event.attach.getSerialNum()) {
			case X06_RequestClientID.COMMAND:
				if (event.session.getIndex() > 0) break;
				X06_RequestClientID x06 = (X06_RequestClientID) event.attach;
				// TODO 注册逻辑补全,把附属信息装入数据库
				client = clientDao.registerOne(x06.getHashInfo(), x06.thirdparty_push_token, x06.client_type);
				X07_ResponseClientID x07 = new X07_ResponseClientID();
				if (client != null)
				{
					System.arraycopy(IoUtil.hex2bin(client.getClientIdHex()), 0, x07.client_id, 0, x07.client_id.length);
					System.arraycopy(MixUtil.mixToken(client.getClientIndex()), 0, x07.client_token, 0, x07.client_token.length);
					event.attach = x07;
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
		return TGX_CM_REGISTER_SERIAL;
	}
}
