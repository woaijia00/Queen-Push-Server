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

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.aio.AIO_IM_Handler;
import com.tgx.queen.base.disruptor.handler.aio.WriteHandler;
import com.tgx.queen.base.disruptor.handler.im.IM_LoginDbHandler;
import com.tgx.queen.base.disruptor.handler.im.IM_LoginHandler;
import com.tgx.queen.base.disruptor.handler.im.MessageHandlerXT;
import com.tgx.queen.base.disruptor.processor.MultiBufferBatchEventProcessor;


/**
 * @author Zhangzhuo
 */
public class IM_3XTCore
        extends
        AbstractCore
{
	private MultiBufferBatchEventProcessor<Event>    accept_read_Processor;
	private MultiBufferBatchEventProcessor<Event>    writeProcessor;
	private MultiBufferBatchEventProcessor<TgxEvent> loginLogicProcessor;
	
	private BatchEventProcessor<TgxEvent>[]          loginDbProcessors;
	private BatchEventProcessor<TgxEvent>[]          routerProcessors;
	
	private final static int                         loginDbBufferCount = 1 << 2;
	private final static int                         routerBufferCount  = 1 << 2;
	
	private final RingBuffer<TgxEvent>               sendBuffer;
	private final RingBuffer<Event>                  wroteBuffer;
	
	public IM_3XTCore(final int producerCount) {
		super(producerCount, Event.EVENT_FACTORY);
		sendBuffer = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 18);
		wroteBuffer = RingBuffer.createSingleProducer(Event.EVENT_FACTORY, 1 << 17);
	}
	
	@SuppressWarnings ({
	        "unchecked",
	        "rawtypes"
	})
	public void initialize() {
		final int producerCount = getProducerCount();
		RingBuffer<TgxEvent> cmUpOrDownBuffer = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 8);
		RingBuffer<TgxEvent>[] loginDbHandleBuffers = new RingBuffer[loginDbBufferCount];
		loginDbProcessors = new BatchEventProcessor[loginDbBufferCount];
		for (int i = 0; i < loginDbBufferCount; i++)
		{
			loginDbHandleBuffers[i] = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 13);
			loginDbProcessors[i] = new BatchEventProcessor<>(loginDbHandleBuffers[i], loginDbHandleBuffers[i].newBarrier(), new IM_LoginDbHandler());
		}
		RingBuffer<TgxEvent> clusterLoginBuffer = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 10);
		/**
		 * loginEventBuffers{
		 * clusterHandleBuffer,
		 * loginDbBuffer[]
		 * cmUpOrDownBuffer
		 * }
		 */
		RingBuffer<TgxEvent>[] loginEventBuffers = new RingBuffer[2 + loginDbBufferCount];
		loginEventBuffers[0] = clusterLoginBuffer;
		System.arraycopy(loginDbHandleBuffers, 0, loginEventBuffers, 1, loginDbBufferCount);
		loginEventBuffers[loginDbBufferCount] = cmUpOrDownBuffer;
		
		SequenceBarrier[] loginEventBarriers = new SequenceBarrier[loginEventBuffers.length];
		loginEventBarriers[0] = clusterLoginBuffer.newBarrier();
		for (int i = 0; i < loginDbBufferCount; i++)
			loginEventBarriers[i] = loginDbHandleBuffers[i].newBarrier(loginDbProcessors[i].getSequence());
		loginEventBarriers[loginDbBufferCount] = cmUpOrDownBuffer.newBarrier();
		loginLogicProcessor = new MultiBufferBatchEventProcessor<>(loginEventBuffers, loginEventBarriers, new IM_LoginHandler());
		/*-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-*/
		RingBuffer<TgxEvent>[] routerHandleBuffers = new RingBuffer[routerBufferCount];
		for (int i = 0; i < routerBufferCount; i++)
			routerHandleBuffers[i] = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 16);
		SequenceBarrier[] routerHandlerBarriers = new SequenceBarrier[routerBufferCount];
		for (int i = 0; i < routerBufferCount; i++)
			routerHandlerBarriers[i] = routerHandleBuffers[i].newBarrier();
		//formatter:off
		accept_read_Processor = new MultiBufferBatchEventProcessor<>(pBuffers, pBarriers, 
				new AIO_IM_Handler(cmUpOrDownBuffer, wroteBuffer)
				.setLoginDbBuffers(loginDbHandleBuffers)
				.setRouterBuffers(routerHandleBuffers)
				.setClusterBuffers(clusterLoginBuffer));
		//formatter:on
		routerProcessors = new BatchEventProcessor[routerBufferCount];
		for (int i = 0; i < routerBufferCount; i++)
			routerProcessors[i] = new BatchEventProcessor<>(routerHandleBuffers[i], routerHandlerBarriers[i], new MessageHandlerXT());
		final int writeHandlerAioBufferIndex = loginEventBuffers.length + routerBufferCount + 1;
		RingBuffer[] writeHandlerBuffers = new RingBuffer[writeHandlerAioBufferIndex + producerCount];
		System.arraycopy(loginEventBuffers, 0, writeHandlerBuffers, 0, loginEventBuffers.length);
		System.arraycopy(routerHandleBuffers, 0, writeHandlerBuffers, loginEventBuffers.length, routerBufferCount);
		
		writeHandlerBuffers[writeHandlerAioBufferIndex - 1] = sendBuffer;
		System.arraycopy(pBuffers, 0, writeHandlerBuffers, writeHandlerAioBufferIndex, producerCount);
		SequenceBarrier[] writeBufferBarriers = new SequenceBarrier[writeHandlerBuffers.length];
		for (int i = 0, size = loginEventBuffers.length; i < size; i++)
			writeBufferBarriers[i] = loginEventBuffers[i].newBarrier(loginLogicProcessor.getSequences()[i]);
		for (int i = 0, j = loginEventBuffers.length; i < routerBufferCount; i++, j++)
			writeBufferBarriers[j] = routerHandleBuffers[i].newBarrier(routerProcessors[i].getSequence());
		writeBufferBarriers[writeHandlerAioBufferIndex - 1] = sendBuffer.newBarrier();
		System.arraycopy(pBarriers, 0, writeBufferBarriers, writeHandlerAioBufferIndex, producerCount);
		writeProcessor = new MultiBufferBatchEventProcessor<Event>(writeHandlerBuffers, writeBufferBarriers, new WriteHandler());
		for (int i = 0, size = loginEventBuffers.length; i < size; i++)
			loginEventBuffers[i].addGatingSequences(writeProcessor.getSequences()[i]);
		for (int i = 0, j = loginEventBuffers.length; i < routerBufferCount; i++, j++)
			routerHandleBuffers[i].addGatingSequences(writeProcessor.getSequences()[j]);
		sendBuffer.addGatingSequences(writeProcessor.getSequences()[writeHandlerAioBufferIndex - 1]);
		for (int i = writeHandlerAioBufferIndex, j = 0; j < producerCount; i++, j++)
			pBuffers[j].addGatingSequences(accept_read_Processor.getSequences()[j], writeProcessor.getSequences()[i]);
		executor.submit(accept_read_Processor);
		for (BatchEventProcessor lProcessor : loginDbProcessors)
			executor.submit(lProcessor);
		executor.submit(loginLogicProcessor);
		for (BatchEventProcessor rProcessor : routerProcessors)
			executor.submit(rProcessor);
		executor.submit(writeProcessor);
	}
	
	@Override
	public int getCpuCount() {
		return 3 + routerBufferCount + loginDbBufferCount;
	}
	
	@Override
	public final RingBuffer<TgxEvent> getSendBuffer() {
		return sendBuffer;
	}
}
