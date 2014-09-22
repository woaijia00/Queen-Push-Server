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
package com.tgx.queen.base.disruptor.handler.logic.operations;

import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.util.MixUtil;
import com.tgx.queen.db.impl.im.TgxUsrDao;
import com.tgx.queen.db.impl.push.TgxClientDao;
import com.tgx.queen.im.UsrStateManager;
import com.tgx.queen.im.bean.TgxUsr;
import com.tgx.queen.io.bean.protocol.impl.X10_Login;
import com.tgx.queen.io.bean.protocol.impl.X11_LoginResult;
import com.tgx.queen.push.bean.TgxClient;
import com.tgx.queen.socket.aio.inf.ISessionManager;


/**
 * @author Zhangzhuo
 */
public enum TgxOpImLogin
        implements
        EventOp
{
	INSTANCE;
	
	@Override
	public void onTranslate(final Event event) {
		
	}
	
	private final TgxClientDao    clientDao       = TgxClientDao.getInstance();
	private final UsrStateManager usrStateManager = UsrStateManager.getInstance();
	private final TgxUsrDao       tgxUsrDao       = TgxUsrDao.getInstance();
	
	@Override
	public EventOp op(final Event event) {
		ISessionManager sm = event.session.getMyManager();
		TgxClient client = null;
		switch (event.attach.getSerialNum()) {
			case X10_Login.COMMAND:
				X10_Login x10 = (X10_Login) event.attach;
				long clientIndex = MixUtil.mixIndex(x10.client_token);
				long usrIndex = MixUtil.mixIndex(x10.usr_token);
				if (event.session.getIndex() == -1 || event.session.getIndex() == clientIndex)
				{
					//TODO 透传到 IM 服务去集中处理登录事件 或者提交 Zookeeper 管理 CM 的登录状态 clientIndex 只允许登录一个设备。重复登录视为安全风险应禁止
					client = clientDao.getClient(clientIndex);
					if (client != null)
					{
						sm.mapSession(clientIndex, event.session);
						event.attach = onLogin(clientIndex, usrIndex);
					}
					else
					{
						event.attach = new X11_LoginResult().setLoginFailed(X11_LoginResult.NOT_FOUND);
					}
				}
				else
				{
					//Ignore login with different clientIndex 
					return null;
				}
				return TgxOpImWrite.INSTANCE;
			default:
				return null;
		}
	}
	
	@Override
	public EventOp errOp(final Event event) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean hasError() {
		return false;
	}
	
	private X11_LoginResult onLogin(long clientIndex, long usrIndex) {
		X11_LoginResult x11 = new X11_LoginResult().setLoginOk();
		if (usrIndex > 0)
		{
			if (tgxUsrDao != null && usrStateManager != null)
			{
				TgxUsr tgxUsr = tgxUsrDao.getImUsr(usrIndex);
				if (tgxUsr != null) usrStateManager.loginUsr(tgxUsr, clientIndex);
				else x11.setLoginFailed(X11_LoginResult.NOT_FOUND);
			}
			else
			{
				// TODO 向MQ进行投递，完成IM登录
			}
		}
		return x11;
	}
	
	@Override
	public int getSerialNum() {
		return TGX_IM_LOGIN_SERIAL;
	}
	
}
