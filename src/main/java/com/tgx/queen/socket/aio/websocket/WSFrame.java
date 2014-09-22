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
package com.tgx.queen.socket.aio.websocket;

import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.socket.aio.impl.AioSession;


public class WSFrame
{
	public WSFrame() {
		setTypeBin();
	}
	
	public byte[] mask;
	
	public byte getMaskData(byte origin, int index) {
		return (byte) (origin ^ mask[index & 3]);
	}
	
	public byte[] payload;
	
	public byte[] getPayload_length() {
		int t_size = 1;
		if (payload_length > 125 && payload_length < 0xFFFF) t_size = 3;
		else if (payload_length > 0xFFFF) t_size = 9;
		byte[] x = new byte[t_size];
		if (payload_length < 126) x[0] = (byte) payload_length;
		else if (payload_length > 125 && payload_length < 0xFFFF)
		{
			x[0] = 126;
			IoUtil.writeShort((int) payload_length, x, 1);
		}
		else if (payload_length >= 0xFFFF)
		{
			x[0] = 127;
			IoUtil.writeLong(payload_length, x, 1);
		}
		if (mask != null) x[0] |= 0x80;
		return x;
	}
	
	public void setTypeTxt() {
		frame_opcode = frame_opcode_no_ctrl_txt;
	}
	
	public void setTypeBin() {
		frame_opcode = frame_opcode_no_ctrl_bin;
	}
	
	public final static byte frame_opcode_ctrl_close     = 0x08;
	public final static byte frame_opcode_ctrl_ping      = 0x09;
	public final static byte frame_opcode_ctrl_pong      = 0x0A;
	public final static byte frame_opcode_ctrl_handshake = 0x00;
	public final static byte frame_fin_more              = 0x00;
	public final static byte frame_fin_no_more           = (byte) 0x80;
	public final static byte frame_opcode_no_ctrl_cont   = 0x00;
	public final static byte frame_opcode_no_ctrl_txt    = 0x01;
	public final static byte frame_opcode_no_ctrl_bin    = 0x02;
	public final static byte frame_max_header_size       = 14;
	public final static int  frame_max_payload_size      = AioSession.SO_PP_BUF - frame_max_header_size;
	
	public long              payload_length;
	public byte              payload_mask;                                                              // mask | first payload_length
	public byte              frame_opcode;
	public boolean           frame_fin;
	public boolean           frame_fragment;
	
	public final static byte getFragmentFrame() {
		return (byte) (frame_fin_more | frame_opcode_no_ctrl_cont);
	}
	
	public final static byte getFragmentEndFrame() {
		return (byte) (frame_fin_no_more | frame_opcode_no_ctrl_cont);
	}
	
	public final static byte getFirstFragmentFrame(byte frame_opcode) {
		return (byte) (frame_fin_more | frame_opcode);
	}
	
	public final static byte getFrame(byte frame_opcode) {
		return (byte) (frame_fin_no_more | frame_opcode);
	}
	
	public byte getFrameFin() {
		if (frame_fragment) return frame_fin ? getFragmentEndFrame() : getFragmentFrame();
		else return getFrame(frame_opcode);
	}
	
	public void setCtrl(byte frame_ctrl_code) {
		frame_opcode = frame_ctrl_code;
	}
	
	public boolean isNoCtrl() {
		return (frame_opcode & 0x08) == 0;
	}
	
	public boolean isCtrlClose() {
		return (frame_opcode & 0x0F) == frame_opcode_ctrl_close;
	}
	
	public boolean isCtrlPing() {
		return (frame_opcode & 0x0F) == frame_opcode_ctrl_ping;
	}
	
	public boolean isCtrlPong() {
		return (frame_opcode & 0x0F) == frame_opcode_ctrl_pong;
	}
	
	public boolean isCtrlHandShake() {
		return (frame_opcode & 0x0F) == frame_opcode_ctrl_handshake;
	}
	
	public void reset() {
		mask = null;
		payload = null;
		payload_length = 0;
		payload_mask = 0;
		frame_opcode = 0;
		frame_fragment = false;
		frame_fin = false;
	}
}
