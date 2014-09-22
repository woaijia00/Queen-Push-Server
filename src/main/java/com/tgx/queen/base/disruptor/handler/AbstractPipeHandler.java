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
package com.tgx.queen.base.disruptor.handler;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.io.inf.IQoS;
import com.tgx.queen.socket.aio.impl.AioSession;


public abstract class AbstractPipeHandler<T extends Event>
        implements
        EventHandler<T>
{
	public <E extends Event> boolean publish(RingBuffer<E> publisher, EventOp op, IQoS attach, AioSession session) {
		return publish(publisher, op, attach, session, null, EventOp.Error.NO_ERROR);
	}
	
	public <E extends Event> boolean publish(RingBuffer<E> publisher, EventOp op, IQoS attach, AioSession session, Throwable exc, EventOp.Error errType) {
		long sequence = -1;
		try
		{
			sequence = publisher.tryNext();
			try
			{
				E event = publisher.get(sequence);
				event.produce(op, errType, session, attach, exc);
				return true;
			}
			finally
			{
				publisher.publish(sequence);
			}
		}
		catch (InsufficientCapacityException e)
		{
			System.err.println(getClass().getSimpleName() + "drop event op: " + op + " s: " + session);
		}
		return false;
	}
}
