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

import com.tgx.queen.io.bean.protocol.impl.X101_Close;
import com.tgx.queen.io.bean.protocol.impl.X102_Ping;
import com.tgx.queen.io.bean.protocol.impl.X103_Pong;
import com.tgx.queen.io.bean.websocket.WSControl;
import com.tgx.queen.socket.aio.impl.AioContext;
import com.tgx.queen.socket.aio.impl.AioFilterChain;
import com.tgx.queen.socket.aio.impl.AioSession;
import com.tgx.queen.socket.aio.inf.IAioContext.ChannelState;
import com.tgx.queen.socket.aio.websocket.WSFrame;


public class WSControlFilter
        extends
        AioFilterChain
{
	
	public WSControlFilter(String name) {
		super(name);
	}
	
	public WSControlFilter(AioFilterChain filter) {
		this(name);
		linkAfter(filter);
	}
	
	public final static String name = "ws-control-filter";
	
	@Override
	public ResultType preEncode(AioSession session, Object content) {
		if (content == null) return ResultType.NOT_OK;
		if (content instanceof WSControl) return ResultType.INSIDE;
		return ResultType.IGNORE;
	}
	
	@Override
	public Object encode(AioSession session, Object content, ResultType preResult) throws Exception {
		if (ResultType.INSIDE.equals(preResult))
		{
			WSControl toEncode = (WSControl) content;
			if (toEncode.getControl() == WSFrame.frame_opcode_ctrl_handshake) return toEncode;
			AioContext context = session.getContext();
			WSFrame wsFrame = new WSFrame();
			wsFrame.payload = toEncode.getControlMsg();
			wsFrame.payload_length = wsFrame.payload == null ? 0 : wsFrame.payload.length;
			wsFrame.setCtrl(toEncode.getControl());
			switch (wsFrame.frame_opcode & 0x0F) {
				case WSFrame.frame_opcode_ctrl_close:
					context.cState = ChannelState.CLOSE_WAIT;
					break;
				case WSFrame.frame_opcode_ctrl_ping:
					context.cState = ChannelState.PING;
					break;
				case WSFrame.frame_opcode_ctrl_pong:
					context.cState = ChannelState.NORMAL;
					break;
				default:
					context.cState = ChannelState.NORMAL;
					break;
			}
			return wsFrame;
		}
		return null;
	}
	
	@Override
	public ResultType preDecode(AioSession session, Object content) {
		if (content != null && content instanceof WSFrame) return ResultType.INSIDE;
		return ResultType.NOT_OK;
	}
	
	@Override
	public Object decode(AioSession session, Object content, ResultType preResult) throws Exception {
		WSFrame wsFrame = (WSFrame) content;
		AioContext context = session.getContext();
		WSControl wsControl = null;
		switch (wsFrame.frame_opcode & 0x0F) {
			case WSFrame.frame_opcode_ctrl_close:
				wsControl = new X101_Close(wsFrame.payload);
				switch (context.cState) {
					case CLOSE_WAIT:
						context.cState = ChannelState.CLOSED;
						break;
					case CLOSED:
						break;
					default:
						context.cState = ChannelState.CLOSE_WAIT;
						break;
				}
				break;
			case WSFrame.frame_opcode_ctrl_ping:
				wsControl = new X102_Ping(wsFrame.payload);
				context.cState = ChannelState.PONG;
				break;
			case WSFrame.frame_opcode_ctrl_pong:
				wsControl = new X103_Pong(wsFrame.payload);
				context.cState = ChannelState.NORMAL;
				break;
			default:
				context.cState = ChannelState.NORMAL;
				break;
		}
		return wsControl;
	}
	
}
