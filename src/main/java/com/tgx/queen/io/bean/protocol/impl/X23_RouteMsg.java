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

import java.nio.ByteBuffer;

import com.tgx.queen.base.util.I18nUtil;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.socket.aio.websocket.WSContext;


public class X23_RouteMsg
        extends
        TgxCommand
{
	public final static int COMMAND = 0x23;
	
	public X23_RouteMsg() {
		super(COMMAND, true);
		setCharset_Serial(getCharsetCode(charset), I18nUtil.SERIAL_BINARY);
		stamp = System.currentTimeMillis();
	}
	
	public long    origin;
	public long    target;
	public long    thread;
	public long    stamp;
	public byte[]  msg;
	private byte[] msg_len;
	
	@Override
	public void dispose() {
		msg = null;
		msg_len = null;
		super.dispose();
	}
	
	@Override
	public int dataLength() {
		msg_len = IoUtil.variableLength(msg != null ? msg.length : 0);
		return 8 + 8 + 8 + 8 + msg_len.length + (msg != null ? msg.length : 0);
	}
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		origin = IoUtil.readLong(data, pos);
		pos += 8;
		target = IoUtil.readLong(data, pos);
		pos += 8;
		thread = IoUtil.readLong(data, pos);
		pos += 8;
		stamp = IoUtil.readLong(data, pos);
		pos += 8;
		ByteBuffer buf = ByteBuffer.wrap(data, pos, data.length - pos);
		int msg_len = IoUtil.readVariableLength(buf);
		if (msg_len > 0)
		{
			msg = new byte[msg_len];
			buf.get(msg);
		}
		pos = buf.position();
		return pos;
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		pos += IoUtil.writeLong(origin, data, pos);
		pos += IoUtil.writeLong(target, data, pos);
		pos += IoUtil.writeLong(thread, data, pos);
		pos += IoUtil.writeLong(stamp, data, pos);
		pos += IoUtil.write(msg_len, 0, data, pos, msg_len.length);
		if (msg != null && msg.length > 0) pos += IoUtil.write(msg, 0, data, pos, msg.length);
		return pos;
	}
	
	@Override
	public int getPriority() {
		return QOS_IMMEDIATE_MESSAGE;
	}
}
