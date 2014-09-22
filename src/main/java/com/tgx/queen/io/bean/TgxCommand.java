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
package com.tgx.queen.io.bean;

import com.tgx.queen.base.inf.IDisposable;
import com.tgx.queen.base.util.CryptUtil;
import com.tgx.queen.base.util.I18nUtil;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.io.bean.inf.ICommand;
import com.tgx.queen.io.inf.IQoS;
import com.tgx.queen.socket.aio.websocket.WSContext;


public abstract class TgxCommand
        implements
        IDisposable,
        ICommand,
        IQoS
{
	protected TgxCommand(int command, boolean has_g_msg_id) {
		this.command = command;
		this.has_g_msg_id = has_g_msg_id;
		initGUid(has_g_msg_id ? new byte[g_msg_uid_size] : null);
		setVersion();
		setEncrypt();
		setCompress();
	}
	
	public final static int    version             = 0x1;
	public final static int    g_msg_uid_size      = 36;
	public String              charset             = "UTF-8";
	public int                 command;
	public byte                type_c;
	public byte                h_attr;
	public byte[]              g_msg_uid;
	protected final static int min_no_msg_uid_size = 1 + 1 + 1 + 4;
	protected final static int min_msg_uid_size    = min_no_msg_uid_size + g_msg_uid_size;
	
	private final boolean      has_g_msg_id;
	
	public boolean isTypeBin() {
		return I18nUtil.isTypeBin(type_c);
	}
	
	public boolean isTypeTxt() {
		return I18nUtil.isTypeTxt(type_c);
	}
	
	@Override
	public int getSerialNum() {
		return command;
	}
	
	protected void addCrc(byte[] data, int lastPos) {
		IoUtil.writeInt(CryptUtil.crc321(data, 0, lastPos), data, lastPos);
	}
	
	protected void checkCrc(byte[] data, int lastPos) {
		int l_crc = CryptUtil.crc321(data, 0, lastPos);
		int crc = IoUtil.readInt(data, lastPos);
		if (l_crc != crc) throw new SecurityException("crc check failed!");
	}
	
	public void decode(byte[] data, WSContext ctx) {
		h_attr = data[0];
		int pos = 2;
		if (g_msg_uid != null && isGlobalMsg())
		{
			System.arraycopy(data, pos, g_msg_uid, 0, g_msg_uid.length);
			pos += g_msg_uid.length;
		}
		type_c = data[pos++];
		charset = getCharset(type_c);
		checkCrc(data, decodec(data, pos, ctx));
	}
	
	protected String getCharset(byte type_c) {
		return I18nUtil.getCharset(type_c);
	}
	
	protected int getCharsetCode(String charset) {
		return I18nUtil.getCharsetCode(charset);
	}
	
	private int length() {
		return (isGlobalMsg() ? min_msg_uid_size : min_no_msg_uid_size) + dataLength();
	}
	
	//TODO 此处修改为传递 ByteBuffer Argument 的形态来进行 encode，去除new byteArray的过程
	public byte[] encode(WSContext ctx) {
		byte[] data = new byte[length()];
		int pos = 0;
		pos += IoUtil.writeByte(h_attr, data, pos);
		pos += IoUtil.writeByte(command, data, pos);
		if (g_msg_uid != null && isGlobalMsg()) pos += IoUtil.write(g_msg_uid, 0, data, pos, g_msg_uid.length);
		pos += IoUtil.writeByte(type_c, data, pos);
		addCrc(data, encodec(data, pos, ctx));
		return data;
	}
	
	public boolean isCmd() {
		return (h_attr & 0x80) == 0;
	}
	
	public int getVerison() {
		return h_attr & 0x0F;
	}
	
	public boolean isCompress() {
		return (h_attr & 0x20) != 0;
	}
	
	public boolean isEncrypt() {
		return (h_attr & 0x10) != 0;
	}
	
	public boolean isGlobalMsg() {
		return (h_attr & 0x40) == 0;
	}
	
	private void initGUid(byte[] g_id) {
		g_msg_uid = g_id;
		if (g_id != null) h_attr &= ~0x40;
		else h_attr |= 0x40;
	}
	
	public void setGUid(byte[] g_id) {
		if (!has_g_msg_id) throw new UnsupportedOperationException();
		initGUid(g_id);
	}
	
	public void setEncrypt() {
		h_attr |= 0x10;
	}
	
	public void setCompress() {
		h_attr |= 0x20;
	}
	
	public void setEvent() {
		h_attr |= 0x80;
	}
	
	public void setVersion() {
		h_attr |= version;
	}
	
	public final void setCharset_Serial(int charset_, int serial_) {
		type_c = I18nUtil.getCharset_Serial(charset_, serial_);
	}
	
	public final void setCharset_Serial(byte type_c) {
		this.type_c = type_c;
	}
	
	@Override
	public void dispose() {
		charset = null;
		g_msg_uid = null;
	}
	
	@Override
	public boolean isDisposable() {
		return true;
	}
	
	public void setGUid(String gUid) {
		setGUid(IoUtil.hex2bin(gUid));
	}
	
	private long sequence;
	
	@Override
	public int compareTo(IQoS o) {
		long seqDiff = getSequence() - o.getSequence();
		int prirityDiff = getPriority() - o.getPriority();
		return prirityDiff == 0 ? (seqDiff == 0 ? 0 : (seqDiff > 0 ? 1 : -1)) : prirityDiff;
	}
	
	@Override
	public long getSequence() {
		return sequence;
	}
	
	@Override
	public IQoS setSequence(long sequence) {
		this.sequence = sequence;
		return this;
	}
	
}
