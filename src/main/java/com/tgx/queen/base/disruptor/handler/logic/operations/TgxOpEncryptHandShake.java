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
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.cm.EncryptHandler;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.io.bean.protocol.impl.X00_EncrytRequest;
import com.tgx.queen.io.bean.protocol.impl.X01_AsymmetricPub;
import com.tgx.queen.io.bean.protocol.impl.X02_EncryptionRc4;
import com.tgx.queen.io.bean.protocol.impl.X03_ResponseEncrypt;
import com.tgx.queen.io.bean.protocol.impl.X04_EncryptStart;
import com.tgx.queen.io.bean.protocol.impl.X101_Close;
import com.tgx.queen.socket.aio.impl.AioContext;
import com.tgx.queen.socket.aio.inf.ITLSContext.EncryptState;


/**
 * @author Zhangzhuo
 */
public enum TgxOpEncryptHandShake
        implements
        EventOp
{
	INSTANCE;
	
	@Override
	public void onTranslate(final Event event) {
		
	}
	
	@Override
	public EventOp op(final Event event) {
		//加密的任何阶段遇到 close 过程都必然停止握手过程
		if (event.session.isClosed()) return null;
		TgxEvent tEvent = (TgxEvent) event;
		EncryptHandler encryptHandler = (EncryptHandler) tEvent.forkHandler;
		AioContext context;
		switch (event.attach.getSerialNum()) {
			case X00_EncrytRequest.COMMAND:
				X00_EncrytRequest x00 = (X00_EncrytRequest) event.attach;
				if (!x00.isEncrypt()) throw new SecurityException("x00 -- no encrypt!");
				encryptHandler.resX01(x00.pubKey_id, event);
				break;
			case X01_AsymmetricPub.COMMAND:// client logic
				X01_AsymmetricPub x01 = (X01_AsymmetricPub) event.attach;
				context = event.session.getContext();
				context.setRc4KeyId(encryptHandler.random.nextInt() & 0xFFFF);
				X02_EncryptionRc4 x02 = new X02_EncryptionRc4();
				x02.encryption = encryptHandler.crypt.getChiperBuf();
				context.reRollKey(encryptHandler.crypt._getRc4Key("tgx-seed-" + Integer.toOctalString(x02.hashCode()) + context.getRc4KeyId(), x01.pubKey, x02.encryption));
				x02.pubKey_id = x01.pubKey_id;
				x02.rc4key_id = context.getRc4KeyId();
				event.attach = x02;
				break;
			case X02_EncryptionRc4.COMMAND:
				x02 = (X02_EncryptionRc4) event.attach;
				context = event.session.getContext();
				if (x02.pubKey_id == context.pubKey_id)
				{
					byte[] rc4Key = encryptHandler.getRc4Key(x02.pubKey_id, x02.encryption);
					if (rc4Key != null)
					{
						context.reRollKey(rc4Key);
						context.setRc4KeyId(x02.rc4key_id);
						X03_ResponseEncrypt x03 = new X03_ResponseEncrypt();
						x03.rc4Key_id = context.getRc4KeyId();
						x03.response = X03_ResponseEncrypt.OK;
						event.attach = x03;
						break;
					}
				}
				encryptHandler.resX01(-1, event);//非一致就当做 x00处理
				break;
			case X03_ResponseEncrypt.COMMAND:// client logic
				X03_ResponseEncrypt x03 = (X03_ResponseEncrypt) event.attach;
				context = event.session.getContext();
				if (x03.rc4Key_id == context.getRc4KeyId())
				{
					X04_EncryptStart x04 = new X04_EncryptStart();
					x04.rc4Key_id = context.getRc4KeyId();
					event.attach = x04;
				}
				break;
			case X04_EncryptStart.COMMAND:
				X04_EncryptStart x04 = (X04_EncryptStart) event.attach;
				context = event.session.getContext();
				if (!context.outState().equals(EncryptState.ENCRYPTED))
				{
					if (x04.rc4Key_id != context.getRc4KeyId())
					{
						event.attach = new X101_Close(null);
						break;
					}
					X04_EncryptStart _x04 = new X04_EncryptStart();
					_x04.rc4Key_id = context.getRc4KeyId();
					event.attach = _x04;
					// 接下去开始解密，并由服务端发出0x04指令接下来所有的包将进行加密传输
					System.out.println("Server-Encrypt-HandShake Ok  " + (++i));
				}
				else
				{
					// client logic
					// 接收到0x04 后续数据是加密过的，需要进行解密
					System.out.println("Client-Encrypt-HandShake Ok  " + (++i));
					event.attach = null;
				}
				break;
		}
		
		return event.attach == null ? null : TgxOpWrite.INSTANCE;
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
	
	@Override
	public int getSerialNum() {
		return TGX_ENCRYPT_HS_SERIAL;
	}
	
}
