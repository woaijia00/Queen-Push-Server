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
package com.tgx.queen.base.disruptor.bean;

import com.lmax.disruptor.EventFactory;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.inf.IReset;
import com.tgx.queen.io.inf.IQoS;
import com.tgx.queen.socket.aio.impl.AioSession;


/**
 * eventOp >> translate >> publish
 * 
 * @author Zhangzhuo
 */

public class Event
        implements
        IReset
{
	
	@Override
	public void reset() {
		session = null;
		attach = null;
		ex = null;
		eventOp = null;
	}
	
	public AioSession     session;
	public IQoS           attach;
	public Throwable      ex;
	public Object         errAttach;
	
	private EventOp       eventOp;
	private EventOp.Error error;
	
	@Override
	public String toString() {
		return "session:" + session + "|EventOp:" + eventOp + " | " + error;
	}
	
	public void eventOp(final EventOp op, final EventOp.Error error) {
		eventOp = op;
		this.error = error;
	}
	
	public EventOp getTranslater() {
		return eventOp;
	}
	
	public EventOp.Error getErrorType() {
		return error;
	}
	
	public EventOp getOperator() {
		return eventOp;
	}
	
	private void translate(AioSession oSession, IQoS content, EventOp op, Throwable exc, Object callbackArg) {
		session = oSession;
		attach = content;
		errAttach = callbackArg;
		ex = exc;
		if (op != null) op.onTranslate(this);
	}
	
	public final void produce(final EventOp op, final EventOp.Error error, final AioSession oSession, final IQoS content, final Throwable exc, final Object callbackArg) {
		reset();
		eventOp(op, error);
		translate(oSession, content, op, exc, callbackArg);
	}
	
	public final void produce(final EventOp op, final EventOp.Error error, final AioSession oSession, IQoS content, final Throwable exc) {
		produce(op, error, oSession, content, exc, null);
	}
	
	public final void produce(final EventOp op, final EventOp.Error error, IQoS content, final Throwable exc) {
		produce(op, error, null, content, exc, null);
	}
	
	public final void produce(final EventOp op, final EventOp.Error error, final Throwable exc, final Object callbackArg) {
		produce(op, error, null, null, exc, callbackArg);
	}
	
	public final void produce(final EventOp op, final AioSession session, final IQoS content) {
		produce(op, EventOp.Error.NO_ERROR, session, content, null, null);
	}
	
	public static final EventFactory<Event> EVENT_FACTORY = new TheEventFactory();
	
	public final static class TheEventFactory
	        implements
	        EventFactory<Event>
	{
		@Override
		public Event newInstance() {
			return new Event();
		}
	}
}
