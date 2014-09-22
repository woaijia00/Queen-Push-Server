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

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.tgx.queen.base.disruptor.handler.aio.operations.AioOpConnect;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.inf.ISerialTick;
import com.tgx.queen.base.util.IoUtil;
import com.tgx.queen.socket.aio.inf.IAioClient;


public class AioConnector
        implements
        CompletionHandler<Void, AsynchronousSocketChannel>,
        ISerialTick
{
	public final static int SerialNum = -0x100003;
	
	@Override
	public int getSerialNum() {
		return SerialNum;
	}
	
	@Override
	public void completed(Void result, AsynchronousSocketChannel channel) {
		AioWorker worker = (AioWorker) Thread.currentThread();
		AioSession session = null;
		try
		{
			session = client.createSession(channel, filter);
			worker.publish(session, AioOpConnect.INSTANCE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			worker.publish(this, AioOpConnect.ERROR, e, EventOp.Error.AIO_CONNECT_ERROR);
			try
			{
				channel.close();
			}
			catch (Exception e1)
			{
				//Ignore
				e1.printStackTrace();
			}
		}
	}
	
	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel channel) {
		AioWorker worker = (AioWorker) Thread.currentThread();
		worker.publish(this, AioOpConnect.ERROR, exc, EventOp.Error.AIO_CONNECT_ERROR);
		try
		{
			channel.close();
		}
		catch (Exception e1)
		{
			//Ignore
			e1.printStackTrace();
		}
	}
	
	private final IAioClient     client;
	private final AioFilterChain filter;
	
	public AioConnector(IAioClient client, String remoteIp, int remotePort, String localIp, int localBindPort, AioFilterChain filter) {
		this.client = client;
		this.remoteIp = remoteIp;
		this.remotePort = remotePort;
		this.localIp = localIp;
		this.localBindPort = localBindPort;
		this.filter = filter;
	}
	
	public AioConnector(IAioClient client, String remoteUrl, String localIp, int localBindPort, AioFilterChain filter) {
		this.client = client;
		this.remoteIp = remoteUrl;
		this.localIp = localIp;
		this.localBindPort = localBindPort;
		this.filter = filter;
		String[] split = IoUtil.splitURL(remoteUrl);
		remoteIp = split[IoUtil.HOST];
		remotePort = Integer.parseInt(split[IoUtil.PORT]);
	}
	
	public AioConnector(IAioClient client, String remoteIp, int remotePort, AioFilterChain filter) {
		this(client, remoteIp, remotePort, null, 0, filter);
	}
	
	private String remoteUrl, remoteIp, localIp;
	private int    remotePort, localBindPort;
	
	public String getRemoteUrl() {
		remoteUrl = "socket://" + remoteIp + ":" + remotePort + "/";
		return remoteUrl;
	}
	
	public String getRemoteIp() {
		return remoteIp;
	}
	
	public void setLocalUri(String localIp, int localBindPort) {
		this.localIp = localIp;
		this.localBindPort = localBindPort;
	}
	
	public String getLocalBindUrl() {
		return "socket://" + localIp + ":" + localBindPort + "/";
	}
	
	public String getLocalBindIp() {
		return localIp;
	}
	
	public int getLocalBindPort() {
		return localBindPort;
	}
	
}
