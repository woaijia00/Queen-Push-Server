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

import java.lang.ref.SoftReference;
import java.util.Iterator;

import com.tgx.queen.base.bean.BaseMessage;
import com.tgx.queen.base.bean.BaseMessage.MsgStatus;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.im.bean.Message;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.io.bean.protocol.impl.X23_RouteMsg;
import com.tgx.queen.io.bean.protocol.impl.X27_RSyncMsgStatus;
import com.tgx.queen.io.bean.protocol.impl.X31_PushMsg;
import com.tgx.queen.io.inf.IQoS;
import com.tgx.queen.socket.aio.impl.AioSession;
import com.tgx.queen.socket.aio.inf.ISessionManager;
import com.tgx.queen.socket.aio.inf.ISessionPlugin;


/**
 * @author Zhangzhuo
 */
public enum TgxOpCmWrite
        implements
        EventOp,
        ISessionPlugin
{
	INSTANCE;
	
	@Override
	public void onTranslate(final Event event) {
		
	}
	
	private ISessionManager sm;
	
	@Override
	public void regSessionManager(ISessionManager sm) {
		this.sm = sm;
	}
	
	private Iterator<? extends BaseMessage> itMsg;
	private SoftReference<IQoS>             perOpResult;
	private SoftReference<AioSession>       perOpSession;
	
	@Override
	public EventOp op(final Event event) {
		AioSession session = event.session;
		if (session != null) try
		{
			if (event.attach != null)
			{
				perOpResult = new SoftReference<IQoS>(event.attach);
				perOpSession = new SoftReference<AioSession>(session);
				event.attach = null;
				return TgxOpToWrite.INSTANCE;
			}
			TgxEvent tgxEvent = (TgxEvent) event;
			long myClientIndex = session.getIndex();
			if (itMsg == null) itMsg = tgxEvent.msgList.iterator();
			if (itMsg.hasNext())
			{
				BaseMessage message = itMsg.next();
				itMsg.remove();
				if (message.getClient() == myClientIndex) return null;
				sendMsg(session.getMyManager(), message);
				return TgxOpToWrite.INSTANCE;
			}
		}
		catch (Exception e)
		{
			System.out.println(session);
			e.printStackTrace();
		}
		else try
		{
			TgxEvent tgxEvent = (TgxEvent) event;
			if (itMsg == null) itMsg = tgxEvent.msgList.iterator();
			if (itMsg.hasNext())
			{
				BaseMessage message = itMsg.next();
				itMsg.remove();
				sendMsg(sm, message);
				return TgxOpToWrite.INSTANCE;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public EventOp errOp(final Event event) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean hasError() {
		return false;
	}
	
	private void sendMsg(ISessionManager sm, BaseMessage bMessage) {
		AioSession session = null;
		try
		{
			session = sm.findSessionByIndex(bMessage.getClient());
			if (session != null && !session.isClosed())
			{
				TgxCommand cmd = null;
				switch (bMessage.parcel) {
					case X27_RSyncMsgStatus.COMMAND:
						X27_RSyncMsgStatus x27 = new X27_RSyncMsgStatus();
						x27.setStatus(bMessage.getStatus());
						x27.origin_g_msg_uid = IoUtil.hex2bin(bMessage.getHexGuid());
						x27.origin = ((Message) bMessage).getOrigin();
						x27.target = ((Message) bMessage).getTarget();
						cmd = x27;
						break;
					case X23_RouteMsg.COMMAND:
						X23_RouteMsg x23 = new X23_RouteMsg();
						x23.setCharset_Serial(bMessage.getFixAttr());
						x23.charset = bMessage.getCharset();
						x23.msg = bMessage.getPayload();
						x23.setGUid(bMessage.getHexGuid());
						Message message = (Message) bMessage;
						x23.origin = message.getOrigin();
						x23.target = message.getTarget();
						x23.thread = message.getThread();
						x23.stamp = bMessage.setStatus(MsgStatus.STATUS_DELIVER).getTimeStamp(MsgStatus.STATUS_DELIVER);
						cmd = x23;
						break;
					case X31_PushMsg.COMMAND:
						X31_PushMsg x31 = new X31_PushMsg();
						x31.setCharset_Serial(bMessage.getFixAttr());
						x31.charset = bMessage.getCharset();
						x31.msg = bMessage.getPayload();
						x31.setGUid(bMessage.getHexGuid());
						x31.stamp = bMessage.setStatus(MsgStatus.STATUS_DELIVER).getTimeStamp(MsgStatus.STATUS_DELIVER);
						cmd = x31;
						break;
				
				}
				perOpSession = new SoftReference<AioSession>(session);
				perOpResult = new SoftReference<IQoS>(cmd);
			}
			else
			{
				if (perOpSession != null) perOpSession.clear();
				if (perOpResult != null) perOpResult.clear();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public int getSerialNum() {
		return AIO_CM_WRITE_SERIAL;
	}
	
	public boolean moreWrite() {
		boolean x = itMsg != null && itMsg.hasNext();
		if (!x)
		{
			itMsg = null;
			if (perOpResult != null) perOpResult.clear();
			if (perOpSession != null) perOpSession.clear();
		}
		return x;
	}
	
	public IQoS getOpResult() {
		return perOpResult != null ? perOpResult.get() : null;
	}
	
	public AioSession getOpSession() {
		return perOpSession != null ? perOpSession.get() : null;
	}
}
