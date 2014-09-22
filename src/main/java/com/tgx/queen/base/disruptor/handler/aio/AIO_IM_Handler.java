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
package com.tgx.queen.base.disruptor.handler.aio;

import com.lmax.disruptor.RingBuffer;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.AbstractPipeHandler;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.io.bean.inf.IClientIndex;
import com.tgx.queen.io.bean.inf.INodeIndex;
import com.tgx.queen.io.bean.inf.IRouteTargetIndex;
import com.tgx.queen.io.bean.protocol.impl.X101_Close;
import com.tgx.queen.io.bean.protocol.impl.X102_Ping;
import com.tgx.queen.io.bean.protocol.impl.X103_Pong;
import com.tgx.queen.io.bean.protocol.impl.X20_SendClientMsg;
import com.tgx.queen.io.bean.protocol.impl.X24_ConfirmRouteArrived;
import com.tgx.queen.io.bean.protocol.impl.X26_SyncMsgStatus;
import com.tgx.queen.io.bean.protocol.impl.X40_ExchangeMsg;
import com.tgx.queen.io.bean.protocol.impl.X42_PaxosMsg;
import com.tgx.queen.io.bean.protocol.impl.X46_ProposalMsg;
import com.tgx.queen.io.bean.protocol.impl.X50_CM_Login;
import com.tgx.queen.io.bean.protocol.impl.X52_CM_Logout;


/**
 * @author Zhangzhuo
 */
public class AIO_IM_Handler
        extends
        AbstractPipeHandler<Event>
{
	public AIO_IM_Handler(final RingBuffer<TgxEvent> ccBuffer, final RingBuffer<Event> wBuffer) {
		cmUpOrDownBuffer = ccBuffer;
		wroteBuffer = wBuffer;
	}
	
	private final RingBuffer<TgxEvent> cmUpOrDownBuffer;
	private final RingBuffer<Event>    wroteBuffer;
	private RingBuffer<TgxEvent>[]     loginEventBuffers;
	private RingBuffer<TgxEvent>[]     routeEventBuffers;
	private RingBuffer<TgxEvent>[]     clusterEventBuffers;
	
	@SuppressWarnings ("unchecked")
	public AIO_IM_Handler setClusterBuffers(RingBuffer<TgxEvent>... buffers) {
		if (Integer.bitCount(buffers.length) != 1) throw new IllegalArgumentException("bufferSize must be a power of 2");
		clusterEventBuffers = buffers;
		cIndexMask = buffers.length - 1;
		return this;
	}
	
	@SuppressWarnings ("unchecked")
	public AIO_IM_Handler setRouterBuffers(RingBuffer<TgxEvent>... buffers) {
		if (Integer.bitCount(buffers.length) != 1) throw new IllegalArgumentException("bufferSize must be a power of 2");
		routeEventBuffers = buffers;
		rIndexMask = buffers.length - 1;
		return this;
	}
	
	@SuppressWarnings ("unchecked")
	public AIO_IM_Handler setLoginDbBuffers(RingBuffer<TgxEvent>... buffers) {
		if (Integer.bitCount(buffers.length) != 1) throw new IllegalArgumentException("bufferSize must be a power of 2");
		loginEventBuffers = buffers;
		lIndexMask = buffers.length - 1;
		return this;
	}
	
	private int lIndexMask, rIndexMask, cIndexMask;
	
	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
		EventOp op = event.getOperator();
		if (op != null)
		{
			EventOp rOp;
			switch (op.getSerialNum()) {
				case EventOp.AIO_ACCEPT_SERIAL:
				case EventOp.AIO_CONNECT_SERIAL:
				case EventOp.AIO_CLOSE_SERIAL:
					rOp = op.hasError() ? op.errOp(event) : op.op(event);
					if (rOp != null) publish(cmUpOrDownBuffer, rOp, event.attach, event.session);
					break;
				case EventOp.AIO_IM_CLIENT_READ_SERIAL:
					rOp = op.hasError() ? op.errOp(event) : op.op(event);
					if (rOp != null)
					{
						switch (event.attach.getSerialNum()) {
							case X20_SendClientMsg.COMMAND:
							case X24_ConfirmRouteArrived.COMMAND:
							case X26_SyncMsgStatus.COMMAND:
								IRouteTargetIndex iRouterTargetIndex = (IRouteTargetIndex) event.attach;
								publish(routeEventBuffers[rIndexMask & iRouterTargetIndex.getTargetIndex()], rOp, event.attach, event.session);
								break;
							case X40_ExchangeMsg.COMMAND:
							case X42_PaxosMsg.COMMAND:
							case X46_ProposalMsg.COMMAND:
								INodeIndex iNodeIndex = (INodeIndex) event.attach;
								publish(clusterEventBuffers[cIndexMask & iNodeIndex.getNodeIndex()], rOp, event.attach, event.session);
								break;
							case X50_CM_Login.COMMAND:
							case X52_CM_Logout.COMMAND:
								IClientIndex iClientIndex = (IClientIndex) event.attach;
								publish(loginEventBuffers[lIndexMask & (int) iClientIndex.getClientIndex()], rOp, event.attach, event.session);
								break;
							case X101_Close.SerialNum:
								publish(cmUpOrDownBuffer, rOp, event.attach, event.session);
								break;
							case X102_Ping.SerialNum:
							case X103_Pong.SerialNum:
								break;
						}
					}
					
					break;
				case EventOp.AIO_WROTE_SERIAL://AioOpFinishWrote
					rOp = op.hasError() ? op.errOp(event) : op.op(event);
					if (rOp != null) publish(wroteBuffer, rOp, event.attach, event.session);
					break;
				default:
					System.out.println("No operation report!");
					break;
			}
		}
		else System.err.println(getClass().getSimpleName() + " NILL  Operator!");
		event.reset();
	}
}
