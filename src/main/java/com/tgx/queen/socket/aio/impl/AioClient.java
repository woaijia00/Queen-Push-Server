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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;

import com.tgx.queen.base.disruptor.AbstractCore;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.socket.aio.inf.IAioClient;


public class AioClient
        extends
        AioLogic
        implements
        IAioClient
{
	
	/**
	 * @param core
	 * @param sessionMap
	 * @throws IOException
	 */
	public AioClient(final AbstractCore core, final Map<Long, AioSession> sessionMap) {
		super(core, sessionMap);
	}
	
	public AioClient init() throws IOException {
		int poolsize = core.getProducerCount();
		channelGroup = AsynchronousChannelGroup.withThreadPool(new AioWorkerGroup(poolsize, this));
		return this;
	}
	
	@Override
	public AioSession createSession(AsynchronousSocketChannel channel, AioFilterChain filter) throws IOException {
		return new AioSession(channel, this, filter);
	}
	
	@Override
	public void newConnect(AioConnector connector) throws IOException {
		AsynchronousSocketChannel aSocketChannel = AsynchronousSocketChannel.open(channelGroup);
		String bindIp = connector.getLocalBindIp();
		int bindPort = connector.getLocalBindPort();
		if (bindIp != null && bindPort != 0) aSocketChannel.bind(new InetSocketAddress(bindIp, bindPort));
		String[] urlSplit = IoUtil.splitURL(connector.getRemoteUrl());
		aSocketChannel.connect(new InetSocketAddress(urlSplit[IoUtil.HOST], Integer.parseInt(urlSplit[IoUtil.PORT])), aSocketChannel, connector);
	}
}
