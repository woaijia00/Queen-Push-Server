/**
 * - Copyright (c) 2013 Zhang Zhuo All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.tgx.queen.base.disruptor.handler.inf;

import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.inf.ISerialTick;


/**
 * @author william
 * @param <T>
 */
public interface EventOp
        extends
        ISerialTick
{
	/**
	 * 仅在被 producer 执行 publish 操作的时候执行一次
	 * 
	 * @param event
	 */
	public void onTranslate(final Event event);
	
	public EventOp op(final Event event);
	
	public EventOp errOp(final Event event);
	
	public boolean hasError();
	
	public static enum Error
	{
		NO_ERROR,
		AIO_ACCEPT_ERROR,
		AIO_CONNECT_ERROR,
		AIO_READ_EOF,
		AIO_READ_ZERO,
		AIO_WRITE_EOF,
		AIO_WRITE_ZERO,
		AIO_READ_ERROR,
		AIO_WRITE_ERROR,
		MQ_CONSUMER_ERROR,
		MQ_PUBLISH_ERROR;
	}
	
	public final static int AIO_ACCEPT_SERIAL            = -0x2002010;
	public final static int AIO_CONNECT_SERIAL           = AIO_ACCEPT_SERIAL + 1;
	public final static int AIO_CLOSE_SERIAL             = AIO_CONNECT_SERIAL + 1;
	
	public final static int AIO_CM_SERVER_READ_SERIAL    = AIO_CLOSE_SERIAL + 1;
	public final static int AIO_CM_CLIENT_READ_SERIAL    = AIO_CM_SERVER_READ_SERIAL + 1;
	public final static int AIO_CM_IM_CLIENT_READ_SERIAL = AIO_CM_CLIENT_READ_SERIAL + 1;
	public final static int AIO_IM_CLIENT_READ_SERIAL    = AIO_CM_IM_CLIENT_READ_SERIAL + 1;
	public final static int AIO_IM_NODE_READ_SERIAL      = AIO_IM_CLIENT_READ_SERIAL + 1;
	
	public final static int AIO_WRITE_SERIAL             = AIO_IM_NODE_READ_SERIAL + 1;
	public final static int AIO_CM_WRITE_SERIAL          = AIO_WRITE_SERIAL + 1;
	public final static int AIO_IM_WRITE_SERIAL          = AIO_CM_WRITE_SERIAL + 1;
	public final static int AIO_TO_WRITE_SERIAL          = AIO_IM_WRITE_SERIAL + 1;
	public final static int AIO_WROTE_SERIAL             = AIO_TO_WRITE_SERIAL + 1;
	
	public final static int TGX_CREATE_SESSION_SERIAL    = AIO_WROTE_SERIAL + 1;
	public final static int TGX_CLOSE_SERIAL             = TGX_CREATE_SESSION_SERIAL + 1;
	public final static int TGX_CHANNEL_SERIAL           = TGX_CLOSE_SERIAL + 1;
	public final static int TGX_ENCRYPT_HS_SERIAL        = TGX_CHANNEL_SERIAL + 1;
	
	public final static int TGX_CM_CLIENT_SERIAL         = TGX_ENCRYPT_HS_SERIAL + 1;       //performance test
	public final static int TGX_CM_REGISTER_SERIAL       = TGX_CM_CLIENT_SERIAL + 1;
	public final static int TGX_CM_LOGIN_SERIAL          = TGX_CM_REGISTER_SERIAL + 1;
	public final static int TGX_CM_LOGOUT_SERIAL         = TGX_CM_LOGIN_SERIAL + 1;
	public final static int TGX_CM_PUSH_SERIAL           = TGX_CM_LOGOUT_SERIAL + 1;
	public final static int TGX_CM_LOGIC_SERIAL          = TGX_CM_PUSH_SERIAL + 1;
	public final static int TGX_IM_LOGIN_SERIAL          = TGX_CM_LOGIC_SERIAL + 1;
	public final static int TGX_IM_DB_LOGIN_SERIAL       = TGX_IM_LOGIN_SERIAL + 1;
	public final static int TGX_IM_LOGOUT_SERIAL         = TGX_IM_DB_LOGIN_SERIAL + 1;
	public final static int TGX_ROUTE_MESSAGE_SERIAL     = TGX_IM_LOGOUT_SERIAL + 1;
	public final static int TGX_NODE_EXCHANGE_SERIAL     = TGX_ROUTE_MESSAGE_SERIAL + 1;
	public final static int TGX_SYNC_CLUSTER_SERIAL      = TGX_NODE_EXCHANGE_SERIAL + 1;
	public final static int TGX_TRANSP_MESSAGE_SERIAL    = TGX_SYNC_CLUSTER_SERIAL + 1;     //透传消息
	                                                                                         
}
