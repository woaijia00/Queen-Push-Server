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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.tgx.queen.base.util.I18nUtil;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.push.bean.Message;
import com.tgx.queen.push.inf.BroadCastable;
import com.tgx.queen.socket.aio.websocket.WSContext;


public class X34_BroadCastAll
        extends
        TgxCommand
        implements
        BroadCastable
{
	public final static int COMMAND = 0x34;
	public byte[]           msg;
	public int              local_id;
	private byte[]          msg_len;
	private long            origin;
	private long[]          clientIndex_array;
	
	public X34_BroadCastAll() {
		super(COMMAND, false);
		setCharset_Serial(getCharsetCode(charset), I18nUtil.SERIAL_BINARY);
	}
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		local_id = IoUtil.readInt(data, pos);
		pos += 4;
		origin = IoUtil.readLong(data, pos);
		pos += 8;
		int array_length = IoUtil.readInt(data, pos);
		pos += 4;
		if (array_length > 0) for (int i = 0; i < array_length; i++)
			clientIndex_array = new long[array_length];
		pos += array_length << 3;
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
		pos += IoUtil.writeInt(local_id, data, pos);
		pos += IoUtil.writeLong(origin, data, pos);
		int array_length = clientIndex_array == null ? 0 : clientIndex_array.length;
		pos += IoUtil.writeInt(array_length, data, pos);
		for (int i = 0; i < array_length; i++)
			pos += IoUtil.writeLong(clientIndex_array[i], data, pos);
		pos += IoUtil.write(msg_len, 0, data, pos, msg_len.length);
		if (msg != null && msg.length > 0) pos += IoUtil.write(msg, 0, data, pos, msg.length);
		return pos;
	}
	
	@Override
	public int dataLength() {
		msg_len = IoUtil.variableLength(msg != null ? msg.length : 0);
		return 4 + 8 + 4 + (clientIndex_array == null ? 0 : clientIndex_array.length) << 3 + msg_len.length + (msg != null ? msg.length : 0);
	}
	
	@Override
	public void dispose() {
		msg = null;
		msg_len = null;
		clientIndex_array = null;
		super.dispose();
	}
	
	public void setMsg(String message, String charset) throws UnsupportedEncodingException {
		msg = message.getBytes(charset);
		msg_len = IoUtil.variableLength(msg.length);
	}
	
	public void setClientIndexs(List<Long> clientIndex_list) {
		if (clientIndex_list == null || clientIndex_list.isEmpty()) return;
		clientIndex_array = new long[clientIndex_list.size()];
		int i = 0;
		for (Iterator<Long> it = clientIndex_list.iterator(); it.hasNext();)
			clientIndex_array[i++] = it.next();
	}
	
	@Override
	public List<Message> mkTarMsg(List<Message> event_msg_list) {
		if (event_msg_list == null) event_msg_list = new LinkedList<>();
		for (long clientIndex : clientIndex_array)
		{
			Message message = new Message();
			message.setClient(clientIndex);
			message.setHexGuid(g_msg_uid);
			message.setCharset_Serial(type_c);
			message.setPayload(msg);
			event_msg_list.add(message);
		}
		return event_msg_list;
	}
	
	@Override
	public long originUsr() {
		return origin;
	}
	
	@Override
	public int getPriority() {
		return QOS_POSTPONE_MESSAGE;
	}
}
