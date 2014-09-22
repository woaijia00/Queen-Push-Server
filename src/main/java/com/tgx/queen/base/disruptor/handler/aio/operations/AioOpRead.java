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
package com.tgx.queen.base.disruptor.handler.aio.operations;

import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpChannelHandle;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpClientMesssage;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpClose;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpCmExchange;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpCmLogic;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpCmLogin;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpCmLogout;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpCmPush;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpCmRegister;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpEncryptHandShake;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpImExchange;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpImRoute;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpSyncCluster;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpWrite;
import com.tgx.queen.io.bean.protocol.impl.X00_EncrytRequest;
import com.tgx.queen.io.bean.protocol.impl.X01_AsymmetricPub;
import com.tgx.queen.io.bean.protocol.impl.X02_EncryptionRc4;
import com.tgx.queen.io.bean.protocol.impl.X03_ResponseEncrypt;
import com.tgx.queen.io.bean.protocol.impl.X04_EncryptStart;
import com.tgx.queen.io.bean.protocol.impl.X05_Redirect;
import com.tgx.queen.io.bean.protocol.impl.X06_RequestClientID;
import com.tgx.queen.io.bean.protocol.impl.X07_ResponseClientID;
import com.tgx.queen.io.bean.protocol.impl.X100_HandShake;
import com.tgx.queen.io.bean.protocol.impl.X101_Close;
import com.tgx.queen.io.bean.protocol.impl.X102_Ping;
import com.tgx.queen.io.bean.protocol.impl.X103_Pong;
import com.tgx.queen.io.bean.protocol.impl.X10_Login;
import com.tgx.queen.io.bean.protocol.impl.X11_LoginResult;
import com.tgx.queen.io.bean.protocol.impl.X12_Logout;
import com.tgx.queen.io.bean.protocol.impl.X14_Disconnet;
import com.tgx.queen.io.bean.protocol.impl.X20_SendClientMsg;
import com.tgx.queen.io.bean.protocol.impl.X21_VerifyMsgArrived;
import com.tgx.queen.io.bean.protocol.impl.X23_RouteMsg;
import com.tgx.queen.io.bean.protocol.impl.X24_ConfirmRouteArrived;
import com.tgx.queen.io.bean.protocol.impl.X25_VerfiyRouteConfirm;
import com.tgx.queen.io.bean.protocol.impl.X26_SyncMsgStatus;
import com.tgx.queen.io.bean.protocol.impl.X27_RSyncMsgStatus;
import com.tgx.queen.io.bean.protocol.impl.X31_PushMsg;
import com.tgx.queen.io.bean.protocol.impl.X32_ConfirmPushArrived;
import com.tgx.queen.io.bean.protocol.impl.X33_VerfiyPushConfirm;
import com.tgx.queen.io.bean.protocol.impl.X34_BroadCastAll;
import com.tgx.queen.io.bean.protocol.impl.X41_VerifyExchangeMsgArrived;
import com.tgx.queen.io.bean.protocol.impl.X43_PaxosMsgConfirm;
import com.tgx.queen.io.bean.protocol.impl.X50_CM_Login;
import com.tgx.queen.io.bean.protocol.impl.X52_CM_Logout;
import com.tgx.queen.io.bean.protocol.impl.X60_IM_Login;
import com.tgx.queen.io.bean.protocol.impl.X62_IM_Logout;
import com.tgx.queen.socket.aio.impl.AioContext;
import com.tgx.queen.socket.aio.inf.IAioContext;


/**
 * @author Zhangzhuo
 */
public enum AioOpRead
        implements
        EventOp
{
	CM_CLIENT_READ {
		public int getSerialNum() {
			return AIO_CM_CLIENT_READ_SERIAL;
		}
		
		@Override
		public EventOp op(Event event) {
			switch (event.attach.getSerialNum()) {
				case X00_EncrytRequest.COMMAND:
				case X01_AsymmetricPub.COMMAND:
				case X02_EncryptionRc4.COMMAND:
				case X03_ResponseEncrypt.COMMAND:
				case X04_EncryptStart.COMMAND:
					return TgxOpEncryptHandShake.INSTANCE;
				case X05_Redirect.COMMAND:
				case X07_ResponseClientID.COMMAND:
				case X11_LoginResult.COMMAND:
					return TgxOpClientMesssage.INSTANCE;
				case X14_Disconnet.COMMAND:
					return TgxOpClose.INSTANCE;
				case X21_VerifyMsgArrived.COMMAND:
				case X23_RouteMsg.COMMAND:
				case X25_VerfiyRouteConfirm.COMMAND:
				case X27_RSyncMsgStatus.COMMAND:
				case X31_PushMsg.COMMAND:
				case X33_VerfiyPushConfirm.COMMAND:
				case X100_HandShake.SerialNum:
					return TgxOpClientMesssage.INSTANCE;
				case X101_Close.SerialNum:
					AioContext context = event.session.getContext();
					if (context.cState.equals(IAioContext.ChannelState.CLOSE_WAIT))
					{
						event.attach = new X101_Close(null);
						return TgxOpWrite.INSTANCE;
					}
					else
					{
						return AioOpClosed.INSTANCE.op(event);
					}
				case X102_Ping.SerialNum:
				case X103_Pong.SerialNum:
					return TgxOpClientMesssage.INSTANCE;
			}
			return null;
		}
	},
	IM_CLIENT_READ {
		public int getSerialNum() {
			return AIO_IM_CLIENT_READ_SERIAL;
		}
		
		@Override
		public EventOp op(Event event) {
			switch (event.attach.getSerialNum()) {
				case X07_ResponseClientID.COMMAND:
				case X11_LoginResult.COMMAND:
					return TgxOpCmLogic.INSTANCE;
				case X14_Disconnet.COMMAND:
					return TgxOpClose.INSTANCE;
				case X23_RouteMsg.COMMAND:
				case X27_RSyncMsgStatus.COMMAND:
					return TgxOpCmLogic.INSTANCE;
				case X100_HandShake.SerialNum:
					return TgxOpChannelHandle.INSTANCE;
				case X101_Close.SerialNum:
					AioContext context = event.session.getContext();
					if (context.cState.equals(IAioContext.ChannelState.CLOSE_WAIT))
					{
						event.attach = new X101_Close(null);
						return TgxOpWrite.INSTANCE;
					}
					else
					{
						return AioOpClosed.INSTANCE.op(event);
					}
				case X102_Ping.SerialNum:
				case X103_Pong.SerialNum:
					return TgxOpChannelHandle.INSTANCE;
			}
			return null;
		}
	},
	CM_SERVER_READ {
		public int getSerialNum() {
			return AIO_CM_SERVER_READ_SERIAL;
		}
		
		@Override
		public EventOp op(Event event) {
			switch (event.attach.getSerialNum()) {
				case X00_EncrytRequest.COMMAND:
				case X01_AsymmetricPub.COMMAND:
				case X02_EncryptionRc4.COMMAND:
				case X03_ResponseEncrypt.COMMAND:
				case X04_EncryptStart.COMMAND:
					return TgxOpEncryptHandShake.INSTANCE;
				case X06_RequestClientID.COMMAND:
					return TgxOpCmRegister.INSTANCE;
				case X10_Login.COMMAND:
					return TgxOpCmLogin.INSTANCE;
				case X12_Logout.COMMAND:
					return TgxOpCmLogout.INSTANCE;
				case X14_Disconnet.COMMAND:
					return TgxOpClose.INSTANCE;
				case X20_SendClientMsg.COMMAND:
				case X24_ConfirmRouteArrived.COMMAND:
				case X26_SyncMsgStatus.COMMAND:
					return TgxOpCmLogic.INSTANCE;
				case X32_ConfirmPushArrived.COMMAND:
				case X34_BroadCastAll.COMMAND:
					return TgxOpCmPush.INSTANCE;
				case X41_VerifyExchangeMsgArrived.COMMAND:
				case X43_PaxosMsgConfirm.COMMAND:
					return TgxOpCmExchange.INSTANCE;
				case X100_HandShake.SerialNum:
					return TgxOpChannelHandle.INSTANCE;
				case X101_Close.SerialNum:
					AioContext context = event.session.getContext();
					if (context.cState.equals(IAioContext.ChannelState.CLOSE_WAIT))
					{
						event.attach = new X101_Close(null);
						return TgxOpWrite.INSTANCE;
					}
					else
					{
						return AioOpClosed.INSTANCE.op(event);
					}
				case X102_Ping.SerialNum:
				case X103_Pong.SerialNum:
					return TgxOpChannelHandle.INSTANCE;
			}
			return null;
		}
	},
	IM_SERVER_READ {
		public int getSerialNum() {
			return AIO_IM_CLIENT_READ_SERIAL;
		}
		
		@Override
		public EventOp op(Event event) {
			switch (event.attach.getSerialNum()) {
				case X14_Disconnet.COMMAND:
					return TgxOpClose.INSTANCE;
				case X20_SendClientMsg.COMMAND:
				case X24_ConfirmRouteArrived.COMMAND:
				case X26_SyncMsgStatus.COMMAND:
					return TgxOpImRoute.INSTANCE;
				case X50_CM_Login.COMMAND:
				case X52_CM_Logout.COMMAND:
					return TgxOpImExchange.INSTANCE;
				case X60_IM_Login.COMMAND:
				case X62_IM_Logout.COMMAND:
					return TgxOpSyncCluster.INSTANCE_IM;
				case X100_HandShake.SerialNum:
					return TgxOpChannelHandle.INSTANCE;
				case X101_Close.SerialNum:
					AioContext context = event.session.getContext();
					if (context.cState.equals(IAioContext.ChannelState.CLOSE_WAIT))
					{
						event.attach = new X101_Close(null);
						return TgxOpWrite.INSTANCE;
					}
					else
					{
						return AioOpClosed.INSTANCE.op(event);
					}
				case X102_Ping.SerialNum:
				case X103_Pong.SerialNum:
					return TgxOpChannelHandle.INSTANCE;
			}
			return null;
		}
	};
	
	@Override
	public void onTranslate(Event event) {
		
	}
	
	@Override
	public EventOp errOp(final Event event) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean hasError() {
		return false;
	}
}
