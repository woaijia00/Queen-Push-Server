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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.tgx.queen.base.message.MsgSequenceManager;
import com.tgx.queen.base.util.I18nUtil;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.push.bean.Message;
import com.tgx.queen.push.inf.BroadCastable;
import com.tgx.queen.socket.aio.websocket.WSContext;


public class XE0_RequestLogUploadCmd
        extends
        TgxCommand
        implements
        BroadCastable
{
	public final static int COMMAND = 0xE0;
	
	public XE0_RequestLogUploadCmd() {
		super(COMMAND, false);
		setCharset_Serial(getCharsetCode(charset), I18nUtil.SERIAL_BINARY);
	}
	
	public byte    log_level;
	public boolean bLogUpload;
	public int     local_id;
	public long[]  client_index_array;
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		local_id = IoUtil.readInt(data, pos);
		pos += 4;
		bLogUpload = (data[pos++] == 1);
		if (bLogUpload)
		{
			log_level = data[pos++];
		}
		
		int array_length = IoUtil.readInt(data, pos);
		pos += 4;
		if (array_length > 0) for (int i = 0; i < array_length; i++)
			client_index_array = new long[array_length];
		pos += array_length << 3;
		
		return pos;
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		pos += IoUtil.writeInt(local_id, data, pos);
		if (bLogUpload)
		{
			data[pos++] = 1;
			data[pos++] = log_level;
		}
		else
		{
			data[pos++] = 0;
		}
		
		int array_length = client_index_array == null ? 0 : client_index_array.length;
		pos += IoUtil.writeInt(array_length, data, pos);
		for (int i = 0; i < array_length; i++)
			pos += IoUtil.writeLong(client_index_array[i], data, pos);
		
		return pos;
	}
	
	@Override
	public int dataLength() {
		int datalen = 4 + 1 + (bLogUpload ? 1 : 0) + 4 + 4 + (client_index_array == null ? 0 : client_index_array.length) << 3;
		return datalen;
	}
	
	@Override
	public long originUsr() {
		return 0;
	}
	
	@Override
	public List<Message> mkTarMsg(List<Message> event_msg_list) {
		if (event_msg_list == null) event_msg_list = new LinkedList<>();
		for (long clientIndex : client_index_array)
		{
			Message message = new Message(XE1_LogUploadCmd.COMMAND);
			message.setClient(clientIndex);
			// message.setHexGuid(g_msg_uid);
			message.setCharset_Serial(type_c);
			XE1_LogUploadCmd xe1 = new XE1_LogUploadCmd();
			xe1.bLogUpload = bLogUpload;
			xe1.log_level = log_level;
			xe1.setGUid(MsgSequenceManager.getInstance().createGUid(0));
			byte[] x = new byte[xe1.dataLength()];
			xe1.encodec(x, 0, null);
			message.setPayload(x);
			event_msg_list.add(message);
		}
		return event_msg_list;
	}
	
	public void setClientIndexArray(List<Long> clientIndex_list) {
		if (clientIndex_list == null || clientIndex_list.isEmpty()) return;
		client_index_array = new long[clientIndex_list.size()];
		int i = 0;
		for (Iterator<Long> it = clientIndex_list.iterator(); it.hasNext();)
			client_index_array[i++] = it.next();
	}
	
	@Override
	public int getPriority() {
		return QOS_POSTPONE_MESSAGE;
	}
}
