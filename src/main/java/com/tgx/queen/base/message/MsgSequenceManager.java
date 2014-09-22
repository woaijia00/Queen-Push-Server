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
package com.tgx.queen.base.message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.tgx.queen.base.util.CryptUtil;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.base.util.MixUtil;
import com.tgx.queen.im.bean.Message;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.io.bean.protocol.impl.X20_SendClientMsg;
import com.tgx.queen.io.bean.protocol.impl.X22_SendChatMsg;


/**
 * 用于服务内部接收的Msg的事件顺序，不是指全局的MsgSequence
 * 
 * @author zhuozhang
 */
public class MsgSequenceManager
{
	private static AtomicLong g_sequence = new AtomicLong(Long.MIN_VALUE);
	
	private MsgSequenceManager() {
	}
	
	public byte[] createGUid(long origin) {
		byte[] data = new byte[TgxCommand.g_msg_uid_size];
		byte[] sha1 = crypt.sha1((Long.toHexString(System.currentTimeMillis()) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes());
		int pos = 0;
		pos += IoUtil.write(sha1, 0, data, pos, sha1.length);
		pos += IoUtil.write(MixUtil.mixToken(origin), 0, data, pos, 16);
		return data;
	}
	
	public byte[] createGUid(Message msg) {
		byte[] guid = createGUid(msg.getOrigin());
		msg.setHexGuid(guid);
		msg.setSequence(g_sequence.getAndIncrement());
		return guid;
	}
	
	public byte[] createGUid(X20_SendClientMsg x20) {
		x20.setGUid(createGUid(x20.origin));
		return x20.g_msg_uid;
	}
	
	public byte[] createGUid(X22_SendChatMsg x22) {
		x22.setGUid(createGUid(x22.origin));
		return x22.g_msg_uid;
	}
	
	public void setSequence(Message msg) {
		msg.setSequence(g_sequence.getAndIncrement());
	}
	
	private CryptUtil crypt = new CryptUtil();
	
	public final static MsgSequenceManager getInstance() {
		return _instance == null ? _instance = new MsgSequenceManager() : _instance;
	}
	
	private static MsgSequenceManager _instance;
	
	public Message getGlobalMsg(String gUID) {
		return msgStateMap.get(gUID);
	}
	
	public Message putGlobalMsg(String gUID, Message msg) {
		msgStateMap.put(gUID, msg);
		return msg;
	}
	
	private Map<String, Message> msgStateMap = new HashMap<>(1 << 16);
}
