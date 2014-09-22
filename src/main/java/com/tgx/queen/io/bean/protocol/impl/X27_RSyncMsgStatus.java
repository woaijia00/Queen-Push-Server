/**
 * - Copyright (c) 2013 Zhang Zhuo All rights reserved. Redistribution and use
 * in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 1. Redistributions of source
 * code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. 2. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific written permission. THIS SOFTWARE
 * IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tgx.queen.io.bean.protocol.impl;

import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.im.bean.Message;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.socket.aio.websocket.WSContext;


public class X27_RSyncMsgStatus
        extends
        TgxCommand
{
	public final static int COMMAND          = 0x27;
	public byte[]           origin_g_msg_uid = new byte[g_msg_uid_size];
	public long             origin;
	public long             target;
	
	private byte            msgStatus;
	
	public X27_RSyncMsgStatus() {
		super(COMMAND, false);
	}
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		origin = IoUtil.readLong(data, pos);
		pos += 8;
		target = IoUtil.readLong(data, pos);
		pos += 8;
		msgStatus = data[pos++];
		System.arraycopy(data, pos, origin_g_msg_uid, 0, g_msg_uid_size);
		pos += origin_g_msg_uid.length;
		return pos;
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		pos += IoUtil.writeLong(origin, data, pos);
		pos += IoUtil.writeLong(target, data, pos);
		data[pos++] = msgStatus;
		pos += IoUtil.write(origin_g_msg_uid, 0, data, pos, g_msg_uid_size);
		return pos;
	}
	
	@Override
	public int dataLength() {
		return g_msg_uid_size + 1 + 8 + 8;
	}
	
	public X27_RSyncMsgStatus setStatus(Message.MsgStatus msgStatus) {
		switch (msgStatus) {
			case STATUS_READ:
				this.msgStatus = Message.READ;
				return this;
			default:
				this.msgStatus = Message.RECV;
		}
		return this;
	}
	
	public Message.MsgStatus getStatus() {
		switch (msgStatus) {
			case Message.READ:
				return Message.MsgStatus.STATUS_READ;
			default:
				return Message.MsgStatus.STATUS_RECV;
		}
	}
	
	@Override
	public int getPriority() {
		return QOS_NO_CONFIRM_MESSAGE;
	}
}
