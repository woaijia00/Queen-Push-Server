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

import java.util.List;

import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.im.bean.Message;
import com.tgx.queen.im.inf.IMsgRouter;
import com.tgx.queen.im.inf.Routeable;
import com.tgx.queen.io.bean.protocol.impl.X07_ResponseClientID;
import com.tgx.queen.io.bean.protocol.impl.X20_SendClientMsg;
import com.tgx.queen.io.bean.protocol.impl.X21_VerifyMsgArrived;
import com.tgx.queen.io.bean.protocol.impl.X24_ConfirmRouteArrived;
import com.tgx.queen.io.bean.protocol.impl.X25_VerfiyRouteConfirm;
import com.tgx.queen.io.bean.protocol.impl.X26_SyncMsgStatus;


/**
 * @author Zhangzhuo
 */
public enum TgxOpCmLogic
        implements
        EventOp
{
	INSTANCE;
	
	private IMsgRouter[] routers;
	
	@Override
	public void onTranslate(final Event event) {
		
	}
	
	@Override
	public EventOp op(final Event event) {
		//此处不以 close 状态为基准，由于关闭前可能已经发送了相关数据必须对其进行转发
		switch (event.attach.getSerialNum()) {
			case X07_ResponseClientID.COMMAND:
			case X20_SendClientMsg.COMMAND:
				X20_SendClientMsg x20 = (X20_SendClientMsg) event.attach;
				X21_VerifyMsgArrived x21 = new X21_VerifyMsgArrived();
				x21.local_id = x20.localId;
				route((TgxEvent) event);
				event.attach = x21;
				break;
			case X24_ConfirmRouteArrived.COMMAND:
				X24_ConfirmRouteArrived x24 = (X24_ConfirmRouteArrived) event.attach;
				X25_VerfiyRouteConfirm x25 = new X25_VerfiyRouteConfirm();
				x25.local_id = x24.local_id;
				route((TgxEvent) event);
				event.attach = x25;
			case X26_SyncMsgStatus.COMMAND:
				route((TgxEvent) event);
				break;
		}
		return TgxOpCmWrite.INSTANCE;
	}
	
	@SuppressWarnings ("unchecked")
	private void route(TgxEvent event) {
		List<Message> list = (List<Message>) event.msgList;
		Routeable rmgs = (Routeable) event.attach;
		for (IMsgRouter router : routers)
			list = router.dipatchMsg(list, rmgs);
		event.msgList = list;
	}
	
	public void setRouter(IMsgRouter... iMsgRouters) {
		routers = iMsgRouters;
	}
	
	@Override
	public EventOp errOp(final Event event) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean hasError() {
		return false;
	}
	
	@Override
	public int getSerialNum() {
		return TGX_CM_LOGIC_SERIAL;
	}
	
}
