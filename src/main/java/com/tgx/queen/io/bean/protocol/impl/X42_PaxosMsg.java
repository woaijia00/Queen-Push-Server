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
package com.tgx.queen.io.bean.protocol.impl;

import java.nio.ByteBuffer;

import com.tgx.queen.base.util.I18nUtil;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.im.bean.Message;
import com.tgx.queen.im.inf.Routeable;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.socket.aio.websocket.WSContext;


/**
 * @author Zhangzhuo
 */
public class X42_PaxosMsg
        extends
        TgxCommand
        implements
        Routeable
{
	public final static int COMMAND = 0x42;
	
	@Override
	public int getSerialNum() {
		return COMMAND;
	}
	
	public X42_PaxosMsg() {
		super(COMMAND, true);
		setCharset_Serial(getCharsetCode(charset), I18nUtil.SERIAL_BINARY);
	}
	
	public int     local_id;
	public long    target;  // account -- multi-client map
	public long    origin;  // my-account
	public long    client;
	public byte[]  msg;
	private byte[] msg_len;
	
	@Override
	public String toString() {
		return "X40" + local_id + "|o:" + origin + " |t:" + target + " |c:" + client + " |g:" + IoUtil.bin2Hex(g_msg_uid);
	}
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		local_id = IoUtil.readInt(data, pos);
		pos += 4;
		client = IoUtil.readLong(data, pos);
		pos += 8;
		origin = IoUtil.readLong(data, pos);
		pos += 8;
		target = IoUtil.readLong(data, pos);
		pos += 8;
		ByteBuffer buf = ByteBuffer.wrap(data, pos, data.length - pos);
		int msg_len = IoUtil.readVariableLength(buf);
		pos = buf.position();
		if (msg_len > 0)
		{
			msg = new byte[msg_len];
			System.arraycopy(data, pos, msg, 0, msg.length);
			pos += msg.length;
		}
		return pos;
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		pos += IoUtil.writeInt(local_id, data, pos);
		pos += IoUtil.writeLong(client, data, pos);
		pos += IoUtil.writeLong(origin, data, pos);
		pos += IoUtil.writeLong(target, data, pos);
		pos += IoUtil.write(msg_len, 0, data, pos, msg_len.length);
		if (msg != null && msg.length > 0) pos += IoUtil.write(msg, 0, data, pos, msg.length);
		return pos;
	}
	
	@Override
	public int dataLength() {
		msg_len = IoUtil.variableLength(msg != null ? msg.length : 0);
		return 4 + 24 + msg_len.length + (msg != null ? msg.length : 0);
	}
	
	@Override
	public void dispose() {
		msg = null;
		msg_len = null;
		super.dispose();
	}
	
	@Override
	public long originUsr() {
		return origin;
	}
	
	/**
	 * p2p消息时，target为对方的UsrIndex
	 * 当进行群聊时，此处的target为Thread-ID/同样计入UsrIndex体系，（不支持群发过程）。
	 */
	@Override
	public long targetUsr() {
		return target;
	}
	
	@Override
	public Message mkTarMsg() {
		Message message = new Message();
		message.setClient(client);
		message.setOrigin(origin);
		message.setTarget(target);
		message.setThread(-1);
		message.setHexGuid(g_msg_uid);
		message.setCharset_Serial(type_c);
		message.setPayload(msg);
		message.parcel = X23_RouteMsg.COMMAND;
		return message;
	}
	
	@Override
	public int getPriority() {
		return QOS_CLUSTER_EXCHANGE;
	}
}
