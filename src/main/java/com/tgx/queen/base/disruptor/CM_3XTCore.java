/**
 * - Copyright (c) 2013 Zhang Zhuo All rights reserved. Redistribution and use
 * in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 1. Redistributions of source
 * code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. 2. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific written permission. THIS SOFTWARE
 * IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tgx.queen.base.disruptor;

import static java.lang.System.arraycopy;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.aio.AIO_CM_Handler;
import com.tgx.queen.base.disruptor.handler.aio.WriteHandler;
import com.tgx.queen.base.disruptor.handler.cm.CM_ExchangeHandler;
import com.tgx.queen.base.disruptor.handler.cm.CM_LogicHandler;
import com.tgx.queen.base.disruptor.handler.cm.CM_SenderHandler;
import com.tgx.queen.base.disruptor.handler.cm.EncryptHandler;
import com.tgx.queen.base.disruptor.handler.cm.WaitWriteHandler;
import com.tgx.queen.base.disruptor.processor.MultiBufferBatchEventProcessor;


/**
 * @author william
 */
public class CM_3XTCore
        extends
        AbstractCore
{
	private MultiBufferBatchEventProcessor<Event>    accept_read_Processor;
	private MultiBufferBatchEventProcessor<Event>    write_next_Processor;
	private MultiBufferBatchEventProcessor<TgxEvent> cmLogicProcessor;
	private MultiBufferBatchEventProcessor<TgxEvent> waitWrProcessor;
	private BatchEventProcessor<?>[]                 encryptProcessors;
	private BatchEventProcessor<TgxEvent>            sendProcessor;
	private BatchEventProcessor<TgxEvent>            exchangeProcessor;
	private final RingBuffer<TgxEvent>               exchangerBuffer;
	private final RingBuffer<TgxEvent>               sendBuffer;
	private final RingBuffer<Event>                  writeBuffer;
	private final RingBuffer<Event>                  wroteBuffer;
	
	public CM_3XTCore(final int producerCount) {
		super(producerCount, Event.EVENT_FACTORY);
		sendBuffer = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 16);
		exchangerBuffer = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 18);
		wroteBuffer = RingBuffer.createSingleProducer(Event.EVENT_FACTORY, 1 << 17, new YieldingWaitStrategy());
		writeBuffer = RingBuffer.createSingleProducer(Event.EVENT_FACTORY, 1 << 18, new YieldingWaitStrategy());
	}
	
	@SuppressWarnings ({
	        "unchecked",
	        "rawtypes"
	})
	public void initialize() {
		RingBuffer<TgxEvent> connectOrCloseBuffer = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 17, new YieldingWaitStrategy());
		RingBuffer<TgxEvent> imServerBuffer = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 17, new YieldingWaitStrategy());
		RingBuffer<TgxEvent>[] encryptBuffers = new RingBuffer[EncryptHandler.encryptCount];
		for (int i = 0; i < EncryptHandler.encryptCount; i++)
			encryptBuffers[i] = RingBuffer.createSingleProducer(TgxEvent.EVENT_FACTORY, 1 << 18);
		encryptProcessors = new BatchEventProcessor[EncryptHandler.encryptCount];
		Sequence[] encryptSequences = new Sequence[EncryptHandler.encryptCount];
		for (int i = 0; i < EncryptHandler.encryptCount; i++)
		{
			encryptProcessors[i] = new BatchEventProcessor<>(encryptBuffers[i], encryptBuffers[i].newBarrier(), new EncryptHandler(i, true));
			encryptSequences[i] = encryptProcessors[i].getSequence();
		}
		//formatter:off
		accept_read_Processor = new MultiBufferBatchEventProcessor<>(pBuffers, pBarriers, 
								new AIO_CM_Handler(connectOrCloseBuffer, wroteBuffer)
                					.setLoginBuffer(imServerBuffer)
                					.setExchageBuffer(exchangerBuffer)
                					.setEncryptBuffer(encryptBuffers));
		//formatter:on
		/****/
		RingBuffer<TgxEvent>[] logicHandlerBuffers = new RingBuffer[] {
		        imServerBuffer,
		        connectOrCloseBuffer
		};
		
		SequenceBarrier[] logicHandleBarriers = new SequenceBarrier[] {
		        imServerBuffer.newBarrier(),
		        connectOrCloseBuffer.newBarrier()
		};
		
		cmLogicProcessor = new MultiBufferBatchEventProcessor<>(logicHandlerBuffers, logicHandleBarriers, new CM_LogicHandler());//单线程处理业务
		sendProcessor = new BatchEventProcessor<TgxEvent>(sendBuffer, sendBuffer.newBarrier(), new CM_SenderHandler());
		exchangeProcessor = new BatchEventProcessor<TgxEvent>(exchangerBuffer, exchangerBuffer.newBarrier(), new CM_ExchangeHandler());
		final int encryptBufIndex = 3;
		final int waitWrBufCount = encryptBufIndex + EncryptHandler.encryptCount;
		
		RingBuffer<TgxEvent>[] waitWrBuffers = new RingBuffer[waitWrBufCount];
		waitWrBuffers[0] = sendBuffer;
		waitWrBuffers[1] = imServerBuffer;
		waitWrBuffers[2] = exchangerBuffer;
		arraycopy(encryptBuffers, 0, waitWrBuffers, encryptBufIndex, EncryptHandler.encryptCount);
		SequenceBarrier[] waitWrBarriers = new SequenceBarrier[waitWrBufCount];
		waitWrBarriers[0] = sendBuffer.newBarrier(sendProcessor.getSequence());
		waitWrBarriers[1] = imServerBuffer.newBarrier(cmLogicProcessor.getSequences()[0]);
		waitWrBarriers[2] = exchangerBuffer.newBarrier(exchangeProcessor.getSequence());
		for (int i = encryptBufIndex, j = 0; j < EncryptHandler.encryptCount; i++, j++)
			waitWrBarriers[i] = encryptBuffers[j].newBarrier(encryptSequences[j]);
		waitWrProcessor = new MultiBufferBatchEventProcessor<TgxEvent>(waitWrBuffers, waitWrBarriers, new WaitWriteHandler(writeBuffer));
		
		final int writeHandlerCount = 2;
		RingBuffer[] writeHandleBuffers = new RingBuffer[writeHandlerCount];
		writeHandleBuffers[0] = writeBuffer;
		writeHandleBuffers[1] = wroteBuffer;
		SequenceBarrier[] writeHandleBarriers = new SequenceBarrier[writeHandlerCount];
		writeHandleBarriers[0] = writeBuffer.newBarrier();
		writeHandleBarriers[1] = wroteBuffer.newBarrier();
		write_next_Processor = new MultiBufferBatchEventProcessor<>(writeHandleBuffers, writeHandleBarriers, new WriteHandler());
		/** Add Gating **/
		for (int i = 0, size = getProducerCount(); i < size; i++)
			pBuffers[i].addGatingSequences(accept_read_Processor.getSequences()[i]);
		connectOrCloseBuffer.addGatingSequences(cmLogicProcessor.getSequences()[1]);
		imServerBuffer.addGatingSequences(waitWrProcessor.getSequences()[1]);
		exchangerBuffer.addGatingSequences(waitWrProcessor.getSequences()[2]);
		for (int i = encryptBufIndex, j = 0; j < EncryptHandler.encryptCount; i++, j++)
			encryptBuffers[j].addGatingSequences(waitWrProcessor.getSequences()[i]);
		sendBuffer.addGatingSequences(waitWrProcessor.getSequences()[0]);
		
		writeBuffer.addGatingSequences(write_next_Processor.getSequences()[0]);
		wroteBuffer.addGatingSequences(write_next_Processor.getSequences()[1]);
		executor.submit(accept_read_Processor);
		executor.submit(cmLogicProcessor);
		executor.submit(exchangeProcessor);
		executor.submit(sendProcessor);
		for (int i = 0; i < EncryptHandler.encryptCount; i++)
			executor.submit(encryptProcessors[i]);
		executor.submit(waitWrProcessor);
		executor.submit(write_next_Processor);
	}
	
	@Override
	public int getCpuCount() {
		return 6 + EncryptHandler.encryptCount;
	}
	
	public final RingBuffer<TgxEvent> getSendBuffer() {
		return sendBuffer;
	}
	
}
