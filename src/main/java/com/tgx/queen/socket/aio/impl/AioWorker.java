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
package com.tgx.queen.socket.aio.impl;

import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.io.inf.IQoS;


/**
 * @author Zhangzhuo
 */
public class AioWorker
        extends
        Thread
{
	final RingBuffer<Event> ringBuffer;
	
	public AioWorker(Runnable r, final RingBuffer<Event> ringBuffer) {
		super(r);
		this.ringBuffer = ringBuffer;
	}
	
	public void publish(AioSession session, EventOp op) {
		publish(session, null, op, null, EventOp.Error.NO_ERROR);
	}
	
	public void publish(AioSession session, EventOp errOp, EventOp.Error error) {
		publish(session, null, errOp, null, error);
	}
	
	public void publish(AioSession session, IQoS content, EventOp op, Throwable ex, EventOp.Error error) {
		long sequence = -1;
		
		try
		{
			sequence = ringBuffer.tryNext();
			try
			{
				Event event = ringBuffer.get(sequence);
				event.produce(op, error, session, content, ex);
			}
			finally
			{
				ringBuffer.publish(sequence);
			}
		}
		catch (InsufficientCapacityException e)
		{
			e.printStackTrace();
			System.out.println("InsufficientCapacity --: " + ringBuffer);
		}
	}
	
	public void publish(IQoS content, EventOp errOp, Throwable ex, EventOp.Error error) {
		publish(null, content, errOp, ex, error);
	}
	
	public void publish(Object errAttach, EventOp errOp, Throwable ex, EventOp.Error error) {
		long sequence = -1;
		try
		{
			sequence = ringBuffer.tryNext();
			try
			{
				Event event = ringBuffer.get(sequence);
				event.produce(errOp, error, ex, errAttach);
			}
			finally
			{
				ringBuffer.publish(sequence);
			}
		}
		catch (InsufficientCapacityException e)
		{
			e.printStackTrace();
			System.out.println("InsufficientCapacity: " + ringBuffer);
		}
	}
}
