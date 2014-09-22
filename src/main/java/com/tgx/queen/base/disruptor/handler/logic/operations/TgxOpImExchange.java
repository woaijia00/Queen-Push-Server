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

import org.apache.commons.collections4.map.HashedMap;

import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.im.bean.Message;
import com.tgx.queen.im.inf.IMsgRouter;
import com.tgx.queen.im.inf.Routeable;
import com.tgx.queen.io.bean.protocol.impl.X24_ConfirmRouteArrived;
import com.tgx.queen.io.bean.protocol.impl.X25_VerfiyRouteConfirm;
import com.tgx.queen.io.bean.protocol.impl.X26_SyncMsgStatus;
import com.tgx.queen.io.bean.protocol.impl.X40_ExchangeMsg;
import com.tgx.queen.io.bean.protocol.impl.X41_VerifyExchangeMsgArrived;


/**
 * @author Zhangzhuo
 */
public enum TgxOpImExchange
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
		switch (event.attach.getSerialNum()) {
			case X40_ExchangeMsg.COMMAND:
				X40_ExchangeMsg x40 = (X40_ExchangeMsg) event.attach;
				X41_VerifyExchangeMsgArrived x41 = new X41_VerifyExchangeMsgArrived();
				x41.local_id = x40.local_id;
				route((TgxEvent) event);
				event.attach = x41;
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
		return TgxOpImWrite.INSTANCE;
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
	
	private HashedMap<Long, long[]> clientBindMap = new HashedMap<>(1 << 20);
	
	private void addBindC2Usr(long client_index, long usr_index) {
		long[] usrs = clientBindMap.get(client_index);
		if (usrs == null) usrs = new long[] {
		        -1,
		        -1,
		        -1,
		        -1,
		        -1
		};
		int i = 0;
		for (; i < usrs.length; i++)
			if (usrs[i] < 0)
			{
				usrs[i] = usr_index;
				break;
			}
		if (i == usrs.length)
		{
			long[] t = new long[usrs.length << 1];
			System.arraycopy(usrs, 0, t, 0, usrs.length);
			t[i++] = usr_index;
			for (; i < t.length; i++)
				t[i] = -1;
			usrs = t;
		}
		clientBindMap.put(client_index, usrs);
	}
	
	private long[] rmBindC2Usr(long client_index) {
		return clientBindMap.remove(client_index);
	}
	
	@Override
	public int getSerialNum() {
		return TGX_NODE_EXCHANGE_SERIAL;
	}
	
}
