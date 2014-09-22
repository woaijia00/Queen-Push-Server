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
package com.tgx.queen.im;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tgx.queen.base.disruptor.AbstractCore;
import com.tgx.queen.base.disruptor.IM_3XTCore;
import com.tgx.queen.socket.aio.impl.AioProcessor;
import com.tgx.queen.socket.aio.impl.AioSession;


/**
 * @author Zhangzhuo
 */
public class ImServerLogic
        extends
        AioProcessor
{
	
	/**
	 * @param core
	 * @param sessionMap
	 * @throws IOException
	 */
	public ImServerLogic(final AbstractCore core, final Map<Long, AioSession> sessionMap) throws IOException {
		super(core, sessionMap);
	}
	
	private final AtomicBoolean imClusterStable = new AtomicBoolean(false);
	
	public final boolean isClusterStable() {
		return imClusterStable.get();
	}
	
	public final void clusterVote() {
		for (boolean isStable;;)
		{
			isStable = imClusterStable.get();
			if (!isStable || imClusterStable.compareAndSet(true, false)) break;
		}
	}
	
	public final void clusterStable() {
		for (boolean isStable;;)
		{
			isStable = imClusterStable.get();
			if (isStable || imClusterStable.compareAndSet(false, true)) break;
		}
	}
	
	public static void main(String[] args) throws IOException {
		ImServerLogic im = new ImServerLogic(new IM_3XTCore(3), new ConcurrentHashMap<Long, AioSession>(1 << 27));
		
		im.startServer();
	}
}
