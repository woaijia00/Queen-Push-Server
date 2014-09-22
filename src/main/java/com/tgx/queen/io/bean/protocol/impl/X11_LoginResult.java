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
import com.tgx.queen.base.util.TimeUtil;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.socket.aio.websocket.WSContext;


public class X11_LoginResult
        extends
        TgxCommand
{
	public final static int COMMAND = 0x11;
	
	public X11_LoginResult() {
		super(COMMAND, false);
	}
	
	@Override
	public void dispose() {
		base64_token = null;
		im_usr_token = null;
		super.dispose();
	}
	
	public final static short UNKNOWN      = -1;
	public final static short OK           = 200;
	public final static short FORBIDDEN    = 403;
	public final static short NOT_FOUND    = 404;
	public short              responseCode = UNKNOWN;
	public String             base64_token;
	public byte[]             im_usr_token = new byte[16];
	public long               _s_time;
	public long               a_time;
	public long               r_time;
	public long               _r_time;
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		responseCode = IoUtil.readShort(data, pos);
		pos += 2;
		int len = IoUtil.readInt(data, pos);
		pos += 4;
		if (len > 0) base64_token = new String(data, pos, len);
		pos += len;
		System.arraycopy(data, pos, im_usr_token, 0, im_usr_token.length);
		pos += im_usr_token.length;
		_s_time = IoUtil.readLong(data, pos);
		pos += 8;
		a_time = IoUtil.readLong(data, pos);
		pos += 8;
		r_time = IoUtil.readLong(data, pos);
		pos += 8;
		_r_time = System.currentTimeMillis();
		return pos;
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		pos += IoUtil.writeShort(responseCode, data, pos);
		if (base64_token == null || base64_token.equals("")) pos += IoUtil.writeInt(0, data, pos);
		else
		{
			byte[] base64_token_x = base64_token.getBytes();
			pos += IoUtil.writeShort(base64_token_x.length, data, pos);
			pos += IoUtil.write(base64_token_x, 0, data, pos, base64_token_x.length);
		}
		pos += IoUtil.write(im_usr_token, 0, data, pos, im_usr_token.length);
		pos += IoUtil.writeLong(_s_time, data, pos);
		pos += IoUtil.writeLong(a_time, data, pos);
		pos += IoUtil.writeLong(TimeUtil.CURRENT_TIME_CACHE, data, pos);
		return pos;
	}
	
	@Override
	public int dataLength() {
		return 6 + ((base64_token == null || base64_token.equals("")) ? 0 : base64_token.length()) + 16 + 24;
	}
	
	public X11_LoginResult setLoginOk() {
		responseCode = OK;
		return this;
	}
	
	public X11_LoginResult setLoginFailed(short errorCode) {
		responseCode = errorCode;
		return this;
	}
	
	public long getNetDelay() {
		return ((_r_time - _s_time) - (r_time - a_time)) >> 1;
	}
	
	public long getDetaTime() {
		return ((a_time - _s_time) + (r_time - _r_time)) >> 1;
	}
	
	@Override
	public int getPriority() {
		return QOS_IMMEDIATE_MESSAGE;
	}
	
}
