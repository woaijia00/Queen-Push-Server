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
package com.tgx.queen.io.filter;

import java.util.LinkedList;
import java.util.List;

import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.io.bean.inf.ICommand;
import com.tgx.queen.io.bean.inf.ICommandFactory;
import com.tgx.queen.io.bean.protocol.impl.X00_EncrytRequest;
import com.tgx.queen.io.bean.protocol.impl.X01_AsymmetricPub;
import com.tgx.queen.io.bean.protocol.impl.X02_EncryptionRc4;
import com.tgx.queen.io.bean.protocol.impl.X03_ResponseEncrypt;
import com.tgx.queen.io.bean.protocol.impl.X04_EncryptStart;
import com.tgx.queen.io.bean.protocol.impl.X05_Redirect;
import com.tgx.queen.io.bean.protocol.impl.X06_RequestClientID;
import com.tgx.queen.io.bean.protocol.impl.X07_ResponseClientID;
import com.tgx.queen.io.bean.protocol.impl.X10_Login;
import com.tgx.queen.io.bean.protocol.impl.X11_LoginResult;
import com.tgx.queen.io.bean.protocol.impl.X12_Logout;
import com.tgx.queen.io.bean.protocol.impl.X20_SendClientMsg;
import com.tgx.queen.io.bean.protocol.impl.X21_VerifyMsgArrived;
import com.tgx.queen.io.bean.protocol.impl.X22_SendChatMsg;
import com.tgx.queen.io.bean.protocol.impl.X23_RouteMsg;
import com.tgx.queen.io.bean.protocol.impl.X24_ConfirmRouteArrived;
import com.tgx.queen.io.bean.protocol.impl.X25_VerfiyRouteConfirm;
import com.tgx.queen.io.bean.protocol.impl.X26_SyncMsgStatus;
import com.tgx.queen.io.bean.protocol.impl.X27_RSyncMsgStatus;
import com.tgx.queen.io.bean.protocol.impl.X31_PushMsg;
import com.tgx.queen.io.bean.protocol.impl.X32_ConfirmPushArrived;
import com.tgx.queen.io.bean.protocol.impl.X33_VerfiyPushConfirm;
import com.tgx.queen.io.bean.protocol.impl.X34_BroadCastAll;
import com.tgx.queen.io.bean.protocol.impl.X35_VerifyBroadCast;
import com.tgx.queen.io.bean.protocol.impl.X40_ExchangeMsg;
import com.tgx.queen.io.bean.protocol.impl.X41_VerifyExchangeMsgArrived;
import com.tgx.queen.io.bean.protocol.impl.X42_PaxosMsg;
import com.tgx.queen.io.bean.protocol.impl.X43_PaxosMsgConfirm;
import com.tgx.queen.io.bean.protocol.impl.X46_ProposalMsg;
import com.tgx.queen.io.bean.protocol.impl.X47_NotifyObserver;
import com.tgx.queen.io.bean.protocol.impl.X48_ConfirmNotify;
import com.tgx.queen.io.bean.protocol.impl.X50_CM_Login;
import com.tgx.queen.io.bean.protocol.impl.X52_CM_Logout;
import com.tgx.queen.io.bean.protocol.impl.X54_SendClientMsg;
import com.tgx.queen.io.bean.protocol.impl.X55_CmRouteMsg;
import com.tgx.queen.io.bean.protocol.impl.X60_IM_Login;
import com.tgx.queen.io.bean.protocol.impl.X62_IM_Logout;
import com.tgx.queen.io.bean.protocol.impl.XE0_RequestLogUploadCmd;
import com.tgx.queen.io.bean.protocol.impl.XE1_LogUploadCmd;
import com.tgx.queen.io.bean.protocol.impl.XE2_ConfirmLogUploadCmdArrived;
import com.tgx.queen.io.bean.protocol.impl.XE4_LogUploadAction;
import com.tgx.queen.io.bean.protocol.impl.XE5_LogUploadResult;
import com.tgx.queen.io.bean.websocket.WSControl;
import com.tgx.queen.io.filter.websocket.WSControlFilter;
import com.tgx.queen.socket.aio.impl.AioFilterChain;
import com.tgx.queen.socket.aio.impl.AioSession;
import com.tgx.queen.socket.aio.websocket.WSContext;
import com.tgx.queen.socket.aio.websocket.WSFrame;


public class WSCommandFilter
        extends
        AioFilterChain
        implements
        ICommandFactory
{
	
	public WSCommandFilter(AioFilterChain filter) {
		super(name);
		linkAfter(filter);
		new WSControlFilter(this);
		
	}
	
	public final static String name = "ws-command-filter";
	
	@Override
	public ResultType preEncode(AioSession session, Object content) {
		if (content == null) return ResultType.NOT_OK;
		if (content instanceof TgxCommand) return ResultType.HANDLED;
		if (content instanceof WSControl) return ResultType.IGNORE;
		return ResultType.NOT_OK;
	}
	
	@Override
	public Object encode(AioSession session, Object content, ResultType preResult) throws Exception {
		if (ResultType.HANDLED.equals(preResult))
		{
			TgxCommand toEncode = (TgxCommand) content;
			WSFrame wsFrame = new WSFrame();
			wsFrame.payload = toEncode.encode((WSContext) session.getContext());
			wsFrame.payload_length = wsFrame.payload == null ? 0 : wsFrame.payload.length;
			if (toEncode.isTypeBin()) wsFrame.setTypeBin();
			else if (toEncode.isTypeTxt()) wsFrame.setTypeTxt();
			return wsFrame;
		}
		return null;
	}
	
	@Override
	public ResultType preDecode(AioSession session, Object content) {
		if (content != null && content instanceof WSFrame)
		{
			WSFrame wsFrame = (WSFrame) content;
			return wsFrame.isNoCtrl() ? ResultType.HANDLED : ResultType.OK;
		}
		return ResultType.NOT_OK;
	}
	
	@Override
	public Object decode(AioSession session, Object content, ResultType preResult) throws Exception {
		WSFrame wsFrame = (WSFrame) content;
		byte[] payload = wsFrame.payload;
		if (wsFrame.mask != null) for (int i = 0; i < payload.length; i++)
			payload[i] = wsFrame.getMaskData(payload[i], i);
		if (ResultType.HANDLED.equals(preResult))
		{
			WSContext context = (WSContext) session.getContext();
			int cmd = payload[1] & 0xFF;
			TgxCommand command = (TgxCommand) makeCommand(cmd);
			if (command != null)
			{
				command.decode(payload, context);
				return command;
			}
			else
			{
				for (ICommandFactory factory : dynamicFactories)
				{
					command = (TgxCommand) factory.makeCommand(cmd);
					if (command != null)
					{
						command.decode(payload, context);
						return command;
					}
				}
			}
		}
		return content;
	}
	
	@Override
	public ICommand makeCommand(int command) {
		switch (command) {
			case X00_EncrytRequest.COMMAND:
				return new X00_EncrytRequest();
			case X01_AsymmetricPub.COMMAND:
				return new X01_AsymmetricPub();
			case X02_EncryptionRc4.COMMAND:
				return new X02_EncryptionRc4();
			case X03_ResponseEncrypt.COMMAND:
				return new X03_ResponseEncrypt();
			case X04_EncryptStart.COMMAND:
				return new X04_EncryptStart();
			case X05_Redirect.COMMAND:
				return new X05_Redirect();
			case X06_RequestClientID.COMMAND:
				return new X06_RequestClientID();
			case X07_ResponseClientID.COMMAND:
				return new X07_ResponseClientID();
			case X10_Login.COMMAND:
				return new X10_Login();
			case X11_LoginResult.COMMAND:
				return new X11_LoginResult();
			case X12_Logout.COMMAND:
				return new X12_Logout();
			case X20_SendClientMsg.COMMAND:
				return new X20_SendClientMsg();
			case X21_VerifyMsgArrived.COMMAND:
				return new X21_VerifyMsgArrived();
			case X22_SendChatMsg.COMMAND:
				return new X22_SendChatMsg();
			case X23_RouteMsg.COMMAND:
				return new X23_RouteMsg();
			case X24_ConfirmRouteArrived.COMMAND:
				return new X24_ConfirmRouteArrived();
			case X25_VerfiyRouteConfirm.COMMAND:
				return new X25_VerfiyRouteConfirm();
			case X26_SyncMsgStatus.COMMAND:
				return new X26_SyncMsgStatus();
			case X27_RSyncMsgStatus.COMMAND:
				return new X27_RSyncMsgStatus();
			case X31_PushMsg.COMMAND:
				return new X31_PushMsg();
			case X32_ConfirmPushArrived.COMMAND:
				return new X32_ConfirmPushArrived();
			case X33_VerfiyPushConfirm.COMMAND:
				return new X33_VerfiyPushConfirm();
			case X34_BroadCastAll.COMMAND:
				return new X34_BroadCastAll();
			case X35_VerifyBroadCast.COMMAND:
				return new X35_VerifyBroadCast();
			case X40_ExchangeMsg.COMMAND:
				return new X40_ExchangeMsg();
			case X41_VerifyExchangeMsgArrived.COMMAND:
				return new X41_VerifyExchangeMsgArrived();
			case X42_PaxosMsg.COMMAND:
				return new X42_PaxosMsg();
			case X43_PaxosMsgConfirm.COMMAND:
				return new X43_PaxosMsgConfirm();
			case X46_ProposalMsg.COMMAND:
				return new X46_ProposalMsg();
			case X47_NotifyObserver.COMMAND:
				return new X47_NotifyObserver();
			case X48_ConfirmNotify.COMMAND:
				return new X48_ConfirmNotify();
			case X50_CM_Login.COMMAND:
				return new X50_CM_Login();
			case X52_CM_Logout.COMMAND:
				return new X52_CM_Logout();
			case X54_SendClientMsg.COMMAND:
				return new X54_SendClientMsg();
			case X55_CmRouteMsg.COMMAND:
				return new X55_CmRouteMsg();
				//			case X56
			case X60_IM_Login.COMMAND:
				return new X60_IM_Login();
			case X62_IM_Logout.COMMAND:
				return new X62_IM_Logout();
			case XE0_RequestLogUploadCmd.COMMAND:
				return new XE0_RequestLogUploadCmd();
			case XE1_LogUploadCmd.COMMAND:
				return new XE1_LogUploadCmd();
			case XE2_ConfirmLogUploadCmdArrived.COMMAND:
				return new XE2_ConfirmLogUploadCmdArrived();
			case XE4_LogUploadAction.COMMAND:
				return new XE4_LogUploadAction();
			case XE5_LogUploadResult.COMMAND:
				return new XE5_LogUploadResult();
		}
		return null;
	}
	
	private List<ICommandFactory> dynamicFactories = new LinkedList<>();
	
	public void regOther(ICommandFactory factory) {
		dynamicFactories.add(factory);
	}
	
	@Override
	public void dispose() {
		dynamicFactories.clear();
		dynamicFactories = null;
		super.dispose();
	}
	
}
