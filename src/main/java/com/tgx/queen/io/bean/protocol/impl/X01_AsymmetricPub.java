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
package com.tgx.queen.io.bean.protocol.impl;

import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.socket.aio.websocket.WSContext;


public class X01_AsymmetricPub
        extends
        TgxCommand
{
	public final static int COMMAND = 0x01;
	
	public X01_AsymmetricPub() {
		super(COMMAND, false);
	}
	
	public byte[] pubKey;
	public int    pubKey_id;
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		pubKey_id = IoUtil.readInt(data, pos);
		pos += 4;
		pubKey = new byte[data.length - min_no_msg_uid_size - 4];
		System.arraycopy(data, pos, pubKey, 0, pubKey.length);
		pos += pubKey.length;
		return pos;
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		if (pubKey == null) throw new NullPointerException("public key hasn't been set!");
		pos += IoUtil.writeInt(pubKey_id, data, pos);
		pos += IoUtil.write(pubKey, 0, data, pos, pubKey.length);
		return pos;
	}
	
	@Override
	public void dispose() {
		pubKey = null;
		super.dispose();
	}
	
	@Override
	public int dataLength() {
		return pubKey.length + 4;
	}
	
	@Override
	public int getPriority() {
		return QOS_NETWORK_CONTROL;
	}
}
