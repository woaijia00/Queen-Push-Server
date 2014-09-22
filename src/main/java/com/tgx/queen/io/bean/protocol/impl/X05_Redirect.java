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


public class X05_Redirect
        extends
        TgxCommand
{
	public static final int COMMAND = 0x05;
	
	public X05_Redirect() {
		super(COMMAND, false);
	}
	
	public static enum Addr_Type
	{
		IPV4,
		IPV6,
		URL
	}
	
	private byte   _type;
	private String url;
	private int    port;
	
	@Override
	public void dispose() {
		url = null;
		super.dispose();
	}
	
	public String getRedirectURL() {
		if (url == null) throw new NullPointerException();
		return "ws://" + url + ":" + port + "/";
	}
	
	public void setRedirectURL(String originUrl) {
		String[] split = IoUtil.splitURL(originUrl);
		url = split[IoUtil.HOST];
		port = Integer.parseInt(split[IoUtil.PORT]);
	}
	
	public void setType(Addr_Type _Type) {
		switch (_Type) {
			case IPV4:
				_type = 0x00;
				break;
			case IPV6:
				_type = 0x40;
				break;
			case URL:
				_type = (byte) 0x80;
				break;
		}
	}
	
	public Addr_Type getType() {
		switch (_type & 0xC0) {
			case 0x00:
				return Addr_Type.IPV4;
			case 0x40:
				return Addr_Type.IPV6;
			default:
				return Addr_Type.URL;
		}
	}
	
	@Override
	public int decodec(byte[] data, int pos, WSContext ctx) {
		_type = data[pos++];
		url = "";
		switch (_type & 0xC0) {
			case 0x00:
				for (int i = 0; i < 4; i++)
					url += Integer.toString(data[pos++] & 0xFF) + (i < 3 ? "." : "");
				break;
			case 0x40:// IPV6仅支持纯 XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX 形态地址
				for (int i = 0; i < 8; i++)
				{
					url += Integer.toString(IoUtil.readUnsignedShort(data, pos), 16) + (i < 7 ? ":" : "");
					pos += 2;
				}
				break;
			default:
				int len = _type & 0x7F;
				url = new String(data, pos, len);
				pos += len;
				break;
		}
		port = IoUtil.readUnsignedShort(data, pos);
		pos += 2;
		return pos;
	}
	
	@Override
	public int encodec(byte[] data, int pos, WSContext ctx) {
		pos += IoUtil.writeByte(_type, data, pos);
		switch (_type & 0xC0) {
			case 0x00:
				String[] ipv4 = url.split("\\.");
				for (String ipx : ipv4)
					pos += IoUtil.writeByte(Integer.parseInt(ipx, 10), data, pos);
				break;
			case 0x40:// IPV6仅支持纯 XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX:XXXX 形态地址
				String[] ipv6 = url.split(":");
				for (String ipx : ipv6)
					pos += IoUtil.writeShort(Integer.parseInt(ipx, 16), data, pos);
				break;
			default:
				byte[] strArray = url.getBytes();
				int len = strArray.length & 0x7F;
				_type |= len;
				pos += IoUtil.write(strArray, 0, data, pos, len);
				break;
		}
		pos += IoUtil.writeShort(port, data, pos);
		return pos;
	}
	
	@Override
	public int dataLength() {
		switch (_type & 0xC0) {
			case 0x00:// IPV4
				return 7;
			case 0x40:// IPV6
				return 19;
			default:// URL
				return 3 + (_type & 0x7F);
		}
	}
	
	@Override
	public int getPriority() {
		return QOS_NETWORK_CONTROL;
	}
}
