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

import java.nio.ByteBuffer;

import com.tgx.queen.socket.aio.impl.AioContext;
import com.tgx.queen.socket.aio.impl.AioSession;


public class WSContext
        extends
        AioContext

{
	public int ws_version, ws_handshake_state;
	public String sec_key, sec_accept_expect, sub_protocol, sec_protocol, handshake;
	public WSFrame carrier;
	public WSContext() {
		super();
		ws_version = 13;
		recvBuf = ByteBuffer.allocate(MAX_SESSION_PAYLOAD_LENGTH);
	}
	
	@Override
	public int onChange(AioSession session) {
		return 0;
	}
	
	@Override
	public final void dispose() {
		sec_key = sec_accept_expect = sub_protocol = sec_protocol = handshake = null;
		super.dispose();
	}
	
	@Override
	public void reset() {
		sec_key = sec_accept_expect = sub_protocol = sec_protocol = handshake = null;
		super.reset();
	}
	
	public final static int MAX_SESSION_PAYLOAD_LENGTH = AioSession.SO_PP_BUF - 2;
	
	public final static int HS_State_GET               = 1 << 0;
	public final static int HS_State_HOST              = 1 << 1;
	public final static int HS_State_UPGRADE           = 1 << 2;
	public final static int HS_State_CONNECTION        = 1 << 3;
	public final static int HS_State_SEC_KEY           = 1 << 4;
	public final static int HS_State_ORIGIN            = 1 << 5;
	public final static int HS_State_SEC_PROTOCOL      = 1 << 6;
	public final static int HS_State_SEC_VERSION       = 1 << 7;
	public final static int HS_State_HTTP_101          = 1 << 8;
	public final static int HS_State_SEC_ACCEPT        = 1 << 9;
	
	public final static int HS_State_ACCEPT_OK         = HS_State_HTTP_101 | HS_State_SEC_ACCEPT | HS_State_UPGRADE | HS_State_CONNECTION;
	public final static int HS_State_CLIENT_OK         = HS_State_GET | HS_State_HOST | HS_State_UPGRADE | HS_State_CONNECTION | HS_State_SEC_KEY | HS_State_SEC_VERSION | HS_State_ORIGIN;
}
