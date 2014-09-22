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
package com.tgx.queen.im;

import java.util.LinkedList;
import java.util.List;

import com.tgx.queen.base.message.MsgSequenceManager;
import com.tgx.queen.db.impl.im.ImUsrCache4Router;
import com.tgx.queen.im.bean.Message;
import com.tgx.queen.im.bean.TgxImUsr;
import com.tgx.queen.im.bean.TgxImUsr.ClientState;
import com.tgx.queen.im.inf.IMsgRouter;
import com.tgx.queen.im.inf.Routeable;


public class IMRouter
        implements
        IMsgRouter
{
	@Override
	public List<Message> dipatchMsg(List<Message> list, Routeable routeMsg) {
		TgxImUsr oUsr = usrCache.getUsr(routeMsg.originUsr());
		TgxImUsr tUsr = usrCache.getUsr(routeMsg.targetUsr());
		Message message = routeMsg.mkTarMsg();
		// 将Message加入到全局消息管理器中 跟踪消息状态
		
		if (oUsr == null)
		{
			// x20 消息不应该出现着这里的调用，
			// x26 消息只进行目标投递不需要进行终端消息同步。所以在这里出现回调没有关系
			// TODO 向MQ提交、离线清单
		}
		if (tUsr == null)
		{
			// TODO 目标不可达，这消息发得有问题，查查查
		}
		if (oUsr != null || tUsr != null)
		{
			if (list == null) list = new LinkedList<>();
			if (tUsr != null)
			{
				if (tUsr.isGroupUsr()) message.setThread(tUsr.getUsrIndex());
				for (ClientState client : tUsr.getClients())
					if (client.getClientIndex() > 0) list.add((Message) message.duplicate().setSequence(sfactory).setClient(client.getClientIndex()));
			}
			if (oUsr != null)
			{
				for (ClientState client : oUsr.getClients())
					if (client.getClientIndex() > 0) list.add((Message) message.duplicate().setSequence(sfactory).setClient(client.getClientIndex()));
			}
			return list;
		}
		list.clear();
		return list;
	}
	
	private MsgSequenceManager sfactory = MsgSequenceManager.getInstance();
	private ImUsrCache4Router  usrCache = ImUsrCache4Router.getInstance();
	
	public static class VirtualThread
	{
		long     send_client_index;
		long[]   sender_client_cluster;
		
		long[][] receiver_account_cluster; // c0:accounts c1:client-index
	}
	
	public static class Translater
	{
		long[] client_index;
		long   account_index;
	}
	
}
