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


public class X06_RequestClientID
        extends
        TgxCommand
{
	public final static int COMMAND = 0x06;
	
	public X06_RequestClientID() {
		super(COMMAND, false);
		imsi = new byte[16];
		imei = new byte[15];
		appId = new byte[32];
	}
	
	public byte[]  imsi, imei, appId, appKey;
	public String  client_type, domain, thirdparty_push_token;
	public long    wifi_mac, bluetooth_mac;
	private byte[] hash_info;
	
	@Override
	public void dispose() {
		client_type = null;
		domain = null;
		imsi = null;
		imei = null;
		appId = null;
		appKey = null;
		hash_info = null;
		super.dispose();
	}
	
	@Override
	public int dataLength() {
		//formatter:off
		return 6 + 6 + imsi.length + imei.length + appId.length +
			   1 + (appKey == null || appKey.length == 0 ? 0 : appKey.length) +
			   2 + (client_type == null || client_type.equals("") ? 0 : client_type.length()) + 
			   1 + ((domain == null || domain.equals("") ? 0 : domain.length())) +
			   1 + ((thirdparty_push_token == null || thirdparty_push_token.equals("") ? 0 : thirdparty_push_token.length()));
		//fotmatter:on
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		pos += IoUtil.writeMac(wifi_mac, data, pos);
		pos += IoUtil.writeMac(bluetooth_mac, data, pos);
		pos += IoUtil.write(imei, 0, data, pos, imei.length);
		pos += IoUtil.write(imsi, 0, data, pos, imsi.length);
		pos += IoUtil.write(appId, 0, data, pos, appId.length);
		pos += IoUtil.writeByte(appKey == null ? 0 : appKey.length, data, pos);
		if (appKey != null && appKey.length > 0) pos += IoUtil.write(appKey, 0, data, pos, appKey.length);
		if (client_type == null || client_type.equals("")) pos += IoUtil.writeShort(0, data, pos);
		else
		{
			byte[] client_type_x = client_type.getBytes();
			pos += IoUtil.writeShort(client_type_x.length, data, pos);
			pos += IoUtil.write(client_type_x, 0, data, pos, client_type_x.length);
		}
		
		if (domain == null || domain.equals("")) pos += IoUtil.writeByte(0, data, pos);
		else
		{
			byte[] domain_x = domain.getBytes();
			pos += IoUtil.writeByte(domain_x.length, data, pos);
			pos += IoUtil.write(domain_x, 0, data, pos, domain_x.length);
		}
		
		if (thirdparty_push_token == null || thirdparty_push_token.equals("")) pos += IoUtil.writeByte(0, data, pos);
		else
		{
			byte[] thirdparty_push_token_x = thirdparty_push_token.getBytes();
			pos += IoUtil.writeByte(thirdparty_push_token_x.length, data, pos);
			pos += IoUtil.write(thirdparty_push_token_x, 0, data, pos, thirdparty_push_token_x.length);
		}
		return pos;
	}
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		int start = pos;
		wifi_mac = IoUtil.readMac(data, pos);
		pos += 6;
		bluetooth_mac = IoUtil.readMac(data, pos);
		pos += 6;
		System.arraycopy(data, pos, imei, 0, imei.length);
		pos += imei.length;
		System.arraycopy(data, pos, imsi, 0, imsi.length);
		pos += imsi.length;
		System.arraycopy(data, pos, appId, 0, appId.length);
		pos += appId.length;
		int appKey_len = data[pos++] & 0xFF;
		if (appKey_len > 0)
		{
			appKey = new byte[appKey_len];
			System.arraycopy(data, pos, appKey, 0, appKey.length);
			pos += appKey.length;
		}
		
		int client_type_len = IoUtil.readUnsignedShort(data, pos);
		pos += 2;
		if (client_type_len > 0)
		{
			byte[] client_type_x = new byte[client_type_len];
			System.arraycopy(data, pos, client_type_x, 0, client_type_x.length);
			client_type = new String(client_type_x);
			pos += client_type_x.length;
		}
		int domain_len = data[pos++] & 0xFF;
		if (domain_len > 0)
		{
			byte[] domain_x = new byte[domain_len];
			System.arraycopy(data, pos, domain_x, 0, domain_x.length);
			domain = new String(domain_x);
			pos += domain_x.length;
		}
		
		int thirdparty_push_token_len = data[pos++] & 0xFF;
		if (thirdparty_push_token_len > 0)
		{
			byte[] thirdparty_push_token_x = new byte[thirdparty_push_token_len];
			System.arraycopy(data, pos, thirdparty_push_token_x, 0, thirdparty_push_token_x.length);
			thirdparty_push_token = new String(thirdparty_push_token_x);
			pos += thirdparty_push_token_x.length;
		}
		int end = pos;
		hash_info = new byte[end - start];
		System.arraycopy(data, start, hash_info, 0, hash_info.length);
		return pos;
	}
	
	@Override
	public int getPriority() {
		return QOS_IMMEDIATE_MESSAGE;
	}
	
	public byte[] getHashInfo() {
		return hash_info;
	}
}
