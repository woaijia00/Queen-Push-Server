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
package com.tgx.queen.io.filter.websocket;

import java.nio.ByteBuffer;
import java.util.Random;

import com.tgx.queen.base.util.CryptUtil;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.io.bean.protocol.impl.X100_HandShake;
import com.tgx.queen.io.filter.TgxRc4Filter;
import com.tgx.queen.io.filter.WSCommandFilter;
import com.tgx.queen.socket.aio.impl.AioFilterChain;
import com.tgx.queen.socket.aio.impl.AioSession;
import com.tgx.queen.socket.aio.inf.IAioContext.ChannelState;
import com.tgx.queen.socket.aio.inf.IAioContext.DecodeState;
import com.tgx.queen.socket.aio.websocket.WSContext;
import com.tgx.queen.socket.aio.websocket.WSFrame;


/**
 * @author william
 */
public class WebSocketProtocolFilter
        extends
        AioFilterChain
{
	
	private final static CryptUtil cryptUtil = new CryptUtil();
	private final static Random    random    = new Random();
	
	public WebSocketProtocolFilter(String name) {
		super(name);
		new WSCommandFilter(this);
		new TgxRc4Filter(this);
	}
	
	public WebSocketProtocolFilter() {
		this(name);
	}
	
	public final static String name = "ws-scoket-filter";
	
	@Override
	public ResultType preEncode(AioSession aIoSession, Object content) {
		if (content == null) return ResultType.NOT_OK;
		else if (content instanceof X100_HandShake) return ResultType.INSIDE;
		if (content instanceof WSFrame) return ResultType.HANDLED;
		return ResultType.NOT_OK;
	}
	
	@Override
	public Object encode(AioSession aIoSession, Object content, ResultType preResult) throws Exception {
		ByteBuffer toWrite = aIoSession.sending;
		toWrite.clear();
		// ---
		if (ResultType.HANDLED.equals(preResult))
		{
			WSFrame toEncode = (WSFrame) content;
			toWrite.put(toEncode.getFrameFin());
			toWrite.put(toEncode.getPayload_length());
			if (toEncode.mask != null) toWrite.put(toEncode.mask);
			if (toEncode.payload_length > 0) toWrite.put(toEncode.payload);
		}
		else if (content instanceof X100_HandShake)
		{
			String[] urlSplit = IoUtil.splitURL(aIoSession.getRemoteUrl());
			WSContext context = (WSContext) aIoSession.getContext();
			byte[] x = new byte[13];
			random.nextBytes(x);
			//formatter:off
			String handshake = "GET /tgx_ws_push HTTP/1.1\r\n" + 
							   "Host: " + urlSplit[IoUtil.HOST] + "\r\n" + 
							   "Upgrade: websocket\r\n" + 
							   "Connection: Upgrade\r\n" + 
							   "Sec-WebSocket-Key: " + (context.sec_key = CryptUtil.base64Encoder(cryptUtil.sha1(x), 0, 76)) + "\r\n" + 
							   "Origin: http://" + urlSplit[IoUtil.HOST] + "\r\n" + 
							   "Sec-WebSocket-Protocol: tgxpush, tgxchat\r\n" + 
							   "Sec-WebSocket-Version: 13\r\n\r\n";
			//formatter:on
			context.sec_accept_expect = CryptUtil.base64Encoder(cryptUtil.sha1((context.sec_key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes()), 0, 76);
			toWrite.put(handshake.getBytes());
		}
		// ***
		toWrite.flip();
		return toWrite;
	}
	
	@Override
	public ResultType preDecode(AioSession aIoSession, Object content) {
		if (content == null || aIoSession == null) return ResultType.NOT_OK;
		WSContext context = (WSContext) aIoSession.getContext();
		if (content instanceof ByteBuffer || content instanceof byte[]) return DecodeState.DECODED_HANDSHANKE.equals(context.dState) ? ResultType.INSIDE : ResultType.OK;
		return ResultType.NOT_OK;
	}
	
	@Override
	public Object decode(AioSession aIoSession, Object content, ResultType preResult) throws Exception {
		WSContext context = (WSContext) aIoSession.getContext();
		ByteBuffer byteBuffer = content instanceof byte[] ? ByteBuffer.wrap((byte[]) content) : (ByteBuffer) content;
		switch (context.dState) {
			case DECODED_HANDSHANKE:
				int c1 = 0;
				while (byteBuffer.hasRemaining())
				{
					c1 = byteBuffer.get();
					context.recvBuf.put((byte) c1);
					if (c1 != '\n' && byteBuffer.hasRemaining()) continue;
					context.recvBuf.flip();
					byte[] t = new byte[context.recvBuf.remaining()];
					context.recvBuf.get(t);
					context.recvBuf.clear();
					String x = new String(t);
					context.handshake = context.handshake == null ? x : context.handshake + x;
					if (x.startsWith("HTTP/1.1"))
					{
						String[] split = x.split(" ", 0);
						if (split[1].equals("101")) context.ws_handshake_state |= WSContext.HS_State_HTTP_101;
					}
					else if (x.equalsIgnoreCase("Upgrade: websocket\r\n")) context.ws_handshake_state |= WSContext.HS_State_UPGRADE;
					else if (x.equalsIgnoreCase("Connection: Upgrade\r\n")) context.ws_handshake_state |= WSContext.HS_State_CONNECTION;
					else if (x.startsWith("Sec-WebSocket-Accept"))
					{
						String[] split = x.split(" ", 2);
						if (!split[1].startsWith(context.sec_accept_expect)) throw new SecurityException("key-error:" + context.sec_accept_expect + " | " + context.sec_key + " | " + split[1]);
						context.ws_handshake_state |= WSContext.HS_State_SEC_ACCEPT;
					}
					else if (x.equals("\r\n") || context.ws_handshake_state == (WSContext.HS_State_ACCEPT_OK)) break;
					
				}
				if (context.ws_handshake_state == (WSContext.HS_State_ACCEPT_OK))
				{
					context.dState = DecodeState.DECODING_FRAME;
					context.cState = ChannelState.NORMAL;
					context.onChange(aIoSession);
					context.sec_accept_expect = context.sec_key = context.sub_protocol = context.sec_protocol = null;// 优化wscontext
					byteBuffer.position(byteBuffer.limit());
					return new X100_HandShake(context.handshake);
				}
				else throw new SecurityException("web-socket-handshake error!");
			case DECODING_FRAME:
				switch (decodeWSFrame(context, byteBuffer)) {
					case NEED_DATA:
					case IGNORE:
						return null;
					case NEXT_STEP:
						return context.carrier;
				}
				break;
		}
		return null;
	}
	
	private DecodeResult decodeWSFrame(WSContext context, ByteBuffer buffer) {
		if (!buffer.hasRemaining()) return DecodeResult.NEED_DATA;
		int need = context.dataNeed;	WSFrame carrier = context.carrier;
		switch (context.decodingIndex) {
			case 0:
				if (carrier == null) context.carrier = carrier = new WSFrame();
				else carrier.reset();
				byte b = buffer.get();
				context.decodingIndex++;
				carrier.frame_opcode = (byte) (b & 0x0F);
				carrier.frame_fin = (b & 0x80) != 0;
				if (!buffer.hasRemaining()) break;
			case 1:
				carrier.payload_mask = buffer.get();
				context.decodingIndex++;
				int length = carrier.payload_mask & 0x7F;
				need = 0;
				if (length == 126) need = 2;
				else if (length == 127) need = 8;
				else carrier.payload_length = length;
				if ((carrier.payload_mask & 0x80) != 0) need += 4;
				context.dataNeed = need;
			default:
				if (need > 0)// 存在非单字节 payload 长度或mask
				{
					if (buffer.remaining() < need)
					{
						context.decodingIndex += buffer.remaining();
						context.dataNeed -= buffer.remaining();
						context.recvBuf.put(buffer);
						return DecodeResult.NEED_DATA;
					}
					else
					{
						int tar_index = context.decodingIndex + need;
						while (context.decodingIndex < tar_index)
						{
							context.recvBuf.put(buffer.get());
							context.decodingIndex++;
						}
						context.recvBuf.flip();
						if ((need & 2) != 0) carrier.payload_length = context.recvBuf.getShort() & 0xFFFF;
						else if ((need & 8) != 0) carrier.payload_length = context.recvBuf.getLong() & 0xFFFFFFFFFFFFFFFFL;
						if ((need & 4) != 0)
						{
							carrier.mask = new byte[4];
							context.recvBuf.get(carrier.mask);
						}
						context.recvBuf.clear();
					}
				}
				context.dataNeed = need = (int) carrier.payload_length;
				if (need == 0)
				{
					context.decodingIndex = 0;
					return carrier.isNoCtrl() ? DecodeResult.IGNORE : DecodeResult.NEXT_STEP;
				}
				if (buffer.remaining() < need)
				{
					context.decodingIndex += buffer.remaining();
					context.dataNeed -= buffer.remaining();
					context.recvBuf.put(buffer);
					return DecodeResult.NEED_DATA;
				}
				else
				{
					int tar_index = context.decodingIndex + need;
					while (context.decodingIndex < tar_index)
					{
						context.recvBuf.put(buffer.get());
						context.decodingIndex++;
					}
					context.recvBuf.flip();
					carrier.payload = new byte[(int) carrier.payload_length];
					context.recvBuf.get(carrier.payload);
					context.decodingIndex = 0;
					context.dataNeed = 0;
					context.recvBuf.clear();
					return DecodeResult.NEXT_STEP;
				}
		}
		return DecodeResult.NEED_DATA;
	}
	
}
