package com.tgx.queen.io.bean.protocol.impl;

import com.tgx.queen.io.bean.inf.ICommand;
import com.tgx.queen.io.bean.inf.ICommandFactory;


public class TestFactory
        implements
        ICommandFactory
{
	
	@Override
	public ICommand makeCommand(int command) {
		switch (command) {
			case XF0_CommitStatus.COMMAND:
				return new XF0_CommitStatus();
			case XF1_ResponseClient.COMMAND:
				return new XF1_ResponseClient();
			case XF3_StartConnect.COMMAND:
				return new XF3_StartConnect();
			case XF4_ConfirmServer.COMMAND:
				return new XF4_ConfirmServer();
			case XF5_StartLogin.COMMAND:
				return new XF5_StartLogin();
			case XF7_RandomMsg.COMMAND:
				return new XF7_RandomMsg();
			case XFF_ResponseConfirm.COMMAND:
				return new XFF_ResponseConfirm();
		}
		return null;
	}
	
}
