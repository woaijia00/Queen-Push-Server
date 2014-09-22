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
package com.tgx.queen.socket.aio.impl;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ThreadFactory;

import com.tgx.queen.base.disruptor.AbstractCore;
import com.tgx.queen.socket.aio.inf.ISessionManager;


public class AioLogic
        implements
        ISessionManager
{
	protected final AbstractCore          core;
	protected AsynchronousChannelGroup    channelGroup;
	protected final Map<Long, AioSession> clientIndex2sessionMap;
	protected final TreeSet<AioSession>   sessionsSet;
	final ThreadFactory                   workerThreadFactory = new ThreadFactory()
	                                                          {
		                                                          @Override
		                                                          public Thread newThread(Runnable r) {
			                                                          return new AioWorker(r, core.getProducerBuffer());
		                                                          }
	                                                          };
	
	public AioLogic(final AbstractCore core, final Map<Long, AioSession> sessionMap) {
		this.core = core;
		sessionsSet = new TreeSet<>();
		clientIndex2sessionMap = sessionMap;
		core.initialize();
	}
	
	@Override
	public void addSession(AioSession session) {
		sessionsSet.add(session);
	}
	
	@Override
	public final void rmSession(AioSession session) {
		boolean success = sessionsSet.remove(session);
		if (success) onClose(session);
		session.dispose();
	}
	
	@Override
	public void mapSession(long clientIndex, AioSession session) {
		if (clientIndex <= 0) throw new IllegalArgumentException("client index <= 0!");
		session.setIndex(clientIndex);
		AioSession oldSession = clientIndex2sessionMap.put(clientIndex, session);
		if (oldSession != null) oldSession.setIndex(-1);
	}
	
	@Override
	public final void clearSession(long clientIndex, AioSession session) {
		if (clientIndex <= 0 || clientIndex != session.getIndex()) return;// 未登录链接不在map管理范畴内，操作的clientIndex不一致也不行
		clientIndex2sessionMap.remove(clientIndex);
	}
	
	@Override
	public AioSession findSessionByIndex(long clientIndex) {
		return clientIndex2sessionMap.get(clientIndex);
	}
	
	@Override
	public Collection<Long> getOnlineClients() {
		return clientIndex2sessionMap.keySet();
	}
	
	public AioSession[] getSessions() {
		AioSession[] x = new AioSession[sessionsSet.size()];
		sessionsSet.toArray(x);
		return x;
	}
	
	public final int sessionCount() {
		return sessionsSet.size();
	}
	
	public AbstractCore getCore() {
		return core;
	}
	
	public void onClose(AioSession session) {
		
	}
	
}
