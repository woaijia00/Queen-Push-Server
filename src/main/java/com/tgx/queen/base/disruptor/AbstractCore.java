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
package com.tgx.queen.base.disruptor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;


/**
 * @author william
 */
public abstract class AbstractCore
{
	protected final RingBuffer<Event>[]                    pBuffers;
	protected final SequenceBarrier[]                      pBarriers;
	protected final ExecutorService                        executor;
	private final int                                      producerCount;
	private final int                                      producerBufferSize = 1 << 17;
	private final ConcurrentLinkedQueue<RingBuffer<Event>> availableBufferQueue;
	
	@SuppressWarnings ("unchecked")
	public AbstractCore(int producerCount, EventFactory<Event> pFactory) {
		executor = Executors.newFixedThreadPool(getCpuCount());
		this.producerCount = producerCount;
		pBuffers = new RingBuffer[producerCount];
		pBarriers = new SequenceBarrier[producerCount];
		availableBufferQueue = new ConcurrentLinkedQueue<RingBuffer<Event>>();
		for (int i = 0; i < producerCount; i++)
		{
			pBuffers[i] = RingBuffer.createSingleProducer(pFactory, producerBufferSize, new YieldingWaitStrategy());
			availableBufferQueue.offer(pBuffers[i]);
			pBarriers[i] = pBuffers[i].newBarrier();
		}
		
	}
	
	public final RingBuffer<Event> getProducerBuffer() {
		if (availableBufferQueue.isEmpty()) throw new IllegalAccessError("check produce count~");
		return availableBufferQueue.poll();
	}
	
	public final void reAvailable(final RingBuffer<Event> buf) {
		availableBufferQueue.offer(buf);
	}
	
	public abstract int getCpuCount();
	
	public abstract void initialize();
	
	public final int getProducerCount() {
		return producerCount;
	}
	
	public abstract RingBuffer<TgxEvent> getSendBuffer();
	
}
