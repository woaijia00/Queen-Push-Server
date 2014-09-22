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


/**
 * X55 携带 localId-myClientIndex 构成当前 client x20消息的x21回执
 * target:origin:thread为x23消息转义需要使用
 * cIndexArray 作为 CM 下发消息的 session 选择标示
 * 
 * @author Zhangzhuo
 */
public class X55_CmRouteMsg
        extends
        TgxCommand
{
	public final static int COMMAND = 0x55;
	
	public X55_CmRouteMsg() {
		super(COMMAND, true);
	}
	
	public int    localId;
	public long   myClientIndex;
	public long   target;
	public long   origin;
	public long   thread;
	public long[] cIndexArray;
	public byte[] msg;
	private int   msgLen;
	private int   cIndexArrayLen;
	
	@Override
	public void dispose() {
		msg = null;
		cIndexArray = null;
		super.dispose();
	}
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		localId = IoUtil.readUnsignedShort(data, pos);
		pos += 2;
		myClientIndex = IoUtil.readLong(data, pos);
		pos += 8;
		origin = IoUtil.readLong(data, pos);
		pos += 8;
		target = IoUtil.readLong(data, pos);
		pos += 8;
		thread = IoUtil.readLong(data, pos);
		pos += 8;
		cIndexArrayLen = IoUtil.readUnsignedShort(data, pos);
		pos += 2;
		if (cIndexArrayLen > 0) cIndexArray = new long[cIndexArrayLen];
		for (int i = 0; i < cIndexArrayLen; i++, pos += 8)
			cIndexArray[i] = IoUtil.readLong(data, pos);
		msgLen = IoUtil.readUnsignedShort(data, pos);
		pos += 2;
		msg = new byte[msgLen];
		System.arraycopy(data, pos, msg, 0, msgLen);
		pos += msgLen;
		return pos;
	}
	
	@Override
	public int dataLength() {
		msgLen = msg != null ? msg.length : 0;
		cIndexArrayLen = cIndexArray == null || cIndexArray.length == 0 ? 0 : cIndexArray.length;
		return 2 + 32 + 2 + (cIndexArrayLen << 3) + 2 + msgLen;
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		pos += IoUtil.writeShort(localId, data, pos);
		pos += IoUtil.writeLong(myClientIndex, data, pos);
		pos += IoUtil.writeLong(origin, data, pos);
		pos += IoUtil.writeLong(target, data, pos);
		pos += IoUtil.writeLong(thread, data, pos);
		pos += IoUtil.writeShort(cIndexArrayLen, data, pos);
		if (cIndexArrayLen > 0) for (long _index : cIndexArray)
			pos += IoUtil.writeLong(_index, data, pos);
		if (msgLen > 0) pos += IoUtil.write(msg, 0, data, pos, msg.length);
		return pos;
	}
	
	@Override
	public int getPriority() {
		return QOS_IMMEDIATE_MESSAGE;
	}
}
