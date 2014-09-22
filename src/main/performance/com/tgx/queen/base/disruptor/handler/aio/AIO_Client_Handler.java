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
import com.tgx.queen.io.bean.protocol.impl.X00_EncrytRequest;
import com.tgx.queen.io.bean.protocol.impl.X01_AsymmetricPub;
import com.tgx.queen.io.bean.protocol.impl.X02_EncryptionRc4;
import com.tgx.queen.io.bean.protocol.impl.X03_ResponseEncrypt;
import com.tgx.queen.io.bean.protocol.impl.X04_EncryptStart;
import com.tgx.queen.io.bean.protocol.impl.X100_HandShake;
import com.tgx.queen.io.bean.protocol.impl.X101_Close;
import com.tgx.queen.io.bean.protocol.impl.X102_Ping;
import com.tgx.queen.io.bean.protocol.impl.X103_Pong;
import com.tgx.queen.io.bean.protocol.impl.X14_Disconnet;


/**
 * @author Zhangzhuo
 */
public class AIO_Client_Handler
        extends
        AbstractPipeHandler<Event>
{
	public AIO_Client_Handler(final RingBuffer<TgxEvent> ccBuffer, final RingBuffer<Event> wBuffer) {
		connectOrCloseBuffer = ccBuffer;
		wroteBuffer = wBuffer;
	}
	
	private final RingBuffer<TgxEvent> connectOrCloseBuffer;
	private final RingBuffer<Event>    wroteBuffer;
	private RingBuffer<TgxEvent>       logicEventBuffer;
	private RingBuffer<TgxEvent>[]     encryptEventBuffers;
	
	public AIO_Client_Handler setLogicBuffer(RingBuffer<TgxEvent> buffer) {
		logicEventBuffer = buffer;
		return this;
	}
	
	public AIO_Client_Handler setEncryptBuffer(RingBuffer<TgxEvent>[] buffers) {
		encryptEventBuffers = buffers;
		if (Integer.bitCount(buffers.length) != 1) throw new IllegalArgumentException("bufferSize must be a power of 2");
		eIndexMask = buffers.length - 1;
		return this;
	}
	
	private int eIndex, eIndexMask;
	int         i = 0, j = 0;
	
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
					if (rOp != null) publish(connectOrCloseBuffer, rOp, event.attach, event.session);
					break;
				case EventOp.AIO_CM_CLIENT_READ_SERIAL:
					rOp = op.hasError() ? op.errOp(event) : op.op(event);
					if (rOp != null)
					{
						if (op.getSerialNum() == EventOp.AIO_CLOSE_SERIAL) publish(connectOrCloseBuffer, rOp, event.attach, event.session);
						else switch (event.attach.getSerialNum()) {
							case X00_EncrytRequest.COMMAND:
							case X01_AsymmetricPub.COMMAND:
							case X02_EncryptionRc4.COMMAND:
							case X03_ResponseEncrypt.COMMAND:
							case X04_EncryptStart.COMMAND:
								if (event.attach.getSerialNum() == X04_EncryptStart.COMMAND) System.out.println("Client - Crypt HandShake OK " + (++i));
								publish(encryptEventBuffers[eIndexMask & eIndex++], rOp, event.attach, event.session);
								break;
							case X14_Disconnet.COMMAND:
								publish(logicEventBuffer, rOp, event.attach, event.session);
								break;
							case X100_HandShake.SerialNum:
								publish(encryptEventBuffers[eIndexMask & eIndex++], rOp, event.attach, event.session);
								break;
							case X101_Close.SerialNum:
								publish(logicEventBuffer, rOp, event.attach, event.session);
								break;
							case X102_Ping.SerialNum:
							case X103_Pong.SerialNum:
								publish(encryptEventBuffers[eIndexMask & eIndex++], rOp, event.attach, event.session);
								break;
							default:
								publish(logicEventBuffer, rOp, event.attach, event.session);
								break;
						}
					}
					break;
				case EventOp.AIO_WROTE_SERIAL:
					rOp = op.hasError() ? op.errOp(event) : op.op(event);
					if (rOp != null) publish(wroteBuffer, rOp, event.attach, event.session);
					break;
				default:
					System.out.println("No operation report!");
					break;
			}
		}
		else System.err.println("-AIO EVENT-No Op");
		event.reset();
	}
}
