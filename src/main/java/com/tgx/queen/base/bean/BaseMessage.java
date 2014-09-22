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
package com.tgx.queen.base.bean;

import com.tgx.queen.base.inf.IDisposable;
import com.tgx.queen.base.util.I18nUtil;
import com.tgx.queen.base.util.IoUtil;


public class BaseMessage
        implements
        IDisposable
{
	public static long CURRENT_TIME_CACHE;
	private long       clientIndex = -1;
	private byte[]     payload;           // message body;
	                                       
	private byte       type_c;
	private String     hexGuid;
	private MsgStatus  status;
	private long[]     status_times;
	public int         parcel;
	
	@Override
	public void dispose() {
		payload = null;
		status_times = null;
		hexGuid = null;
	}
	
	@Override
	public boolean isDisposable() {
		return true;
	}
	
	protected BaseMessage(BaseMessage oMsg) {
		if (oMsg == null)
		{
			status_times = new long[MsgStatus.values().length];
			status = MsgStatus.STATUS_CREATE;
		}
		else
		{
			hexGuid = oMsg.hexGuid;
			status_times = oMsg.status_times;// 如果需要追踪每个消息的时间状态就不能这么共享数据了，需要分开存储，只复制CreatTime
			payload = oMsg.payload;
			type_c = oMsg.type_c;
			status = oMsg.status;
			parcel = oMsg.parcel;
		}
	}
	
	public final byte[] getPayload() {
		if (payload == null) return null;
		byte[] r = new byte[payload.length];
		System.arraycopy(payload, 0, r, 0, payload.length);
		return r;
	}
	
	public final BaseMessage setPayload(byte[] payload) {
		this.payload = payload;
		return this;
	}
	
	public final BaseMessage setCharset_Serial(String charset, int serialType) {
		type_c = I18nUtil.getCharset_Serial(I18nUtil.getCharsetCode(charset), serialType);
		return this;
	}
	
	public final BaseMessage setCharset_Serial(byte type_c) {
		this.type_c = type_c;
		return this;
	}
	
	public final String getCharset() {
		return I18nUtil.getCharset(type_c);
	}
	
	public final boolean checkType(byte expect) {
		return I18nUtil.checkType(type_c, expect);
	}
	
	public final byte getFixAttr() {
		return type_c;
	}
	
	public final String getHexGuid() {
		return hexGuid;
	}
	
	public final BaseMessage setHexGuid(byte[] guid) {
		this.hexGuid = IoUtil.bin2Hex(guid);
		return this;
	}
	
	public final MsgStatus getStatus() {
		return status;
	}
	
	public final BaseMessage setStatus(MsgStatus status) {
		this.status = status;
		status_times[status.ordinal()] = CURRENT_TIME_CACHE;
		return this;
	}
	
	public final long getClient() {
		return clientIndex;
	}
	
	public final BaseMessage setClient(long client) {
		this.clientIndex = client;
		return this;
	}
	
	public final long getTimeStamp(MsgStatus status) {
		return status_times[status.ordinal()];
	}
	
	public static enum MsgStatus
	{
		STATUS_CREATE,
		STATUS_DELIVER,
		STATUS_ARRIVE,
		STATUS_RECV,
		STATUS_READ;
	}
	
	public final static byte RECV = 1;
	public final static byte READ = RECV + 1;
}
