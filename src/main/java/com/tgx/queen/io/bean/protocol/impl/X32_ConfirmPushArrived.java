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


public class X32_ConfirmPushArrived
        extends
        TgxCommand
{
	public final static int COMMAND = 0x32;
	
	public X32_ConfirmPushArrived() {
		super(COMMAND, false);
		origin_g_msg_id = new byte[g_msg_uid_size];
	}
	
	public int    local_id;
	public long   clientIndex;
	public long   stamp;
	public byte[] origin_g_msg_id;
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		local_id = IoUtil.readUnsignedShort(data, pos);
		pos += 2;
		System.arraycopy(data, pos, origin_g_msg_id, 0, origin_g_msg_id.length);
		pos += origin_g_msg_id.length;
		clientIndex = IoUtil.readLong(data, pos);
		pos += 8;
		stamp = IoUtil.readLong(data, pos);
		pos += 8;
		return pos;
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		pos += IoUtil.writeShort(local_id, data, pos);
		pos += IoUtil.write(origin_g_msg_id, 0, data, pos, origin_g_msg_id.length);
		pos += IoUtil.writeLong(clientIndex, data, pos);
		pos += IoUtil.writeLong(stamp, data, pos);
		return pos;
	}
	
	@Override
	public int dataLength() {
		return 2 + g_msg_uid_size + 16;
	}
	
	@Override
	public int getPriority() {
		return QOS_IMMEDIATE_MESSAGE;
	}
}
