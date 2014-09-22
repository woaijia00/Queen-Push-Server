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
import com.tgx.queen.io.bean.protocol.impl.X32_ConfirmPushArrived;
import com.tgx.queen.io.bean.protocol.impl.X33_VerfiyPushConfirm;
import com.tgx.queen.io.bean.protocol.impl.X34_BroadCastAll;
import com.tgx.queen.io.bean.protocol.impl.XE0_RequestLogUploadCmd;
import com.tgx.queen.io.bean.protocol.impl.XE4_LogUploadAction;
import com.tgx.queen.io.bean.protocol.impl.XE5_LogUploadResult;
import com.tgx.queen.push.bean.Message;
import com.tgx.queen.push.inf.BroadCastable;
import com.tgx.queen.push.inf.IBroadCaster;


/**
 * @author Zhangzhuo
 */
public enum TgxOpCmPush
        implements
        EventOp
{
	INSTANCE;
	
	@Override
	public void onTranslate(final Event event) {
		
	}
	
	@Override
	public EventOp op(final Event event) {
		switch (event.attach.getSerialNum()) {
			case X32_ConfirmPushArrived.COMMAND:
				X32_ConfirmPushArrived x32 = (X32_ConfirmPushArrived) event.attach;
				X33_VerfiyPushConfirm x33 = new X33_VerfiyPushConfirm();
				x33.local_id = x32.local_id;
				x33.setGUid(x32.g_msg_uid);
				event.attach = x33;
				
				//				/*
				//				 * report message reciver
				//				 */
				//				String guid = IoUtil.bin2Hex(x32.origin_g_msg_id);
				//				System.out.println("--------------guid" + guid);
				break;
			case X34_BroadCastAll.COMMAND:
				route((TgxEvent) event);
				break;
			case XE0_RequestLogUploadCmd.COMMAND:
				route((TgxEvent) event);
				break;
			case XE4_LogUploadAction.COMMAND:
				XE4_LogUploadAction xe4 = (XE4_LogUploadAction) event.attach;
				//				System.out.println("Upload log -------------:" + new String(xe4.log_msg));
				//TODO 日志投递到 Hadoop
				XE5_LogUploadResult xe5 = new XE5_LogUploadResult();
				xe5.setGUid(xe4.g_msg_uid);
				event.attach = xe5;
				break;
		}
		return TgxOpCmWrite.INSTANCE;
	}
	
	@SuppressWarnings ("unchecked")
	private void route(TgxEvent event) {
		List<Message> list = (List<Message>) event.msgList;
		BroadCastable bmgs = (BroadCastable) event.attach;
		for (IBroadCaster broadCaster : broadCasters)
			list = broadCaster.dipatchMsg(list, bmgs);
		event.msgList = list;
		event.attach = null;
	}
	
	public void setBroadCaster(IBroadCaster... iBroadCasters) {
		broadCasters = iBroadCasters;
	}
	
	private IBroadCaster[] broadCasters;
	
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
		return TGX_CM_PUSH_SERIAL;
	}
	
}
