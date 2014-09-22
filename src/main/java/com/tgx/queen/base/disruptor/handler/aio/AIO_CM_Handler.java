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
import com.tgx.queen.base.disruptor.handler.cm.EncryptHandler;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.io.bean.protocol.impl.X00_EncrytRequest;
import com.tgx.queen.io.bean.protocol.impl.X02_EncryptionRc4;
import com.tgx.queen.io.bean.protocol.impl.X04_EncryptStart;
import com.tgx.queen.io.bean.protocol.impl.X06_RequestClientID;
import com.tgx.queen.io.bean.protocol.impl.X100_HandShake;
import com.tgx.queen.io.bean.protocol.impl.X101_Close;
import com.tgx.queen.io.bean.protocol.impl.X102_Ping;
import com.tgx.queen.io.bean.protocol.impl.X103_Pong;
import com.tgx.queen.io.bean.protocol.impl.X10_Login;
import com.tgx.queen.io.bean.protocol.impl.X12_Logout;
import com.tgx.queen.io.bean.protocol.impl.X20_SendClientMsg;
import com.tgx.queen.io.bean.protocol.impl.X24_ConfirmRouteArrived;
import com.tgx.queen.io.bean.protocol.impl.X26_SyncMsgStatus;
import com.tgx.queen.io.bean.protocol.impl.X32_ConfirmPushArrived;
import com.tgx.queen.io.bean.protocol.impl.X34_BroadCastAll;


/**
 * @author Zhangzhuo
 */
public class AIO_CM_Handler
        extends
        AbstractPipeHandler<Event>
{
	public AIO_CM_Handler(final RingBuffer<TgxEvent> ccBuffer, final RingBuffer<Event> wBuffer) {
		connectOrCloseBuffer = ccBuffer;
		wroteBuffer = wBuffer;
	}
	
	private final RingBuffer<TgxEvent> connectOrCloseBuffer;
	private final RingBuffer<Event>    wroteBuffer;
	private RingBuffer<TgxEvent>       loginEventBuffer;
	private RingBuffer<TgxEvent>       exchangeEventBuffer;
	private RingBuffer<TgxEvent>[]     encryptEventBuffers;
	
	public AIO_CM_Handler setExchageBuffer(RingBuffer<TgxEvent> buffer) {
		exchangeEventBuffer = buffer;
		return this;
	}
	
	public AIO_CM_Handler setLoginBuffer(RingBuffer<TgxEvent> buffer) {
		loginEventBuffer = buffer;
		return this;
	}
	
	public AIO_CM_Handler setEncryptBuffer(RingBuffer<TgxEvent>[] buffers) {
		encryptEventBuffers = buffers;
		if (Integer.bitCount(buffers.length) != 1) throw new IllegalArgumentException("bufferSize must be a power of 2");
		eIndexMask = buffers.length - 1;
		return this;
	}
	
	private int eIndex, eIndexMask;
	private int i = 0, j = 0;
	
	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
		EventOp op = event.getOperator();
		if (op != null)
		{
			EventOp rOp;
			switch (op.getSerialNum()) {
				case EventOp.AIO_ACCEPT_SERIAL://AioOpAccept
				case EventOp.AIO_CONNECT_SERIAL://AioOpConnect
				case EventOp.AIO_CLOSE_SERIAL://AioOpClosed
					//read -1/faild/timeout
					rOp = op.hasError() ? op.errOp(event) : op.op(event);
					if (rOp != null) publish(connectOrCloseBuffer, rOp, event.attach, event.session);
					break;
				case EventOp.AIO_CM_SERVER_READ_SERIAL:
					rOp = op.hasError() ? op.errOp(event) : op.op(event);
					if (rOp != null)
					{
						switch (event.attach.getSerialNum()) {
							case X10_Login.COMMAND:
							case X12_Logout.COMMAND:
								publish(loginEventBuffer, rOp, event.attach, event.session);
								break;
							case X20_SendClientMsg.COMMAND:
							case X24_ConfirmRouteArrived.COMMAND:
							case X26_SyncMsgStatus.COMMAND:
							case X32_ConfirmPushArrived.COMMAND:
							case X34_BroadCastAll.COMMAND:
								publish(exchangeEventBuffer, rOp, event.attach, event.session);
								break;
							case X00_EncrytRequest.COMMAND:
								X00_EncrytRequest x00 = (X00_EncrytRequest) event.attach;
								int forkIndex = EncryptHandler.isPubKeyAvailable(x00.pubKey_id) ? EncryptHandler.getForkIndex(x00.pubKey_id) : eIndexMask & eIndex++;
								publish(encryptEventBuffers[forkIndex], rOp, event.attach, event.session);
								break;
							case X02_EncryptionRc4.COMMAND:
								X02_EncryptionRc4 x02 = (X02_EncryptionRc4) event.attach;
								forkIndex = EncryptHandler.getForkIndex(x02.pubKey_id);
								publish(encryptEventBuffers[forkIndex], rOp, event.attach, event.session);
								break;
							case X04_EncryptStart.COMMAND:
								if (event.attach.getSerialNum() == X04_EncryptStart.COMMAND) System.out.println("Server - Crypt HandShake OK " + (++i));
							case X06_RequestClientID.COMMAND:
							case X100_HandShake.SerialNum:
								if (event.attach.getSerialNum() == X100_HandShake.SerialNum) System.out.println("Server - HandShake OK " + (++j));
								publish(encryptEventBuffers[eIndexMask & eIndex++], rOp, event.attach, event.session);
								break;
							case X101_Close.SerialNum:
								/*
								 * 如果返回为 TgxOpWrite将在 Connect/Login 线程中不进行操作
								 * 透过 SequenceBarrier 进入 Write 处理线程
								 * --
								 * 返回 TgxOpClose 则在 Connect/Login 线程中操作
								 */
								publish(connectOrCloseBuffer, rOp, event.attach, event.session);
								break;
							case X102_Ping.SerialNum:
							case X103_Pong.SerialNum:
								publish(encryptEventBuffers[eIndexMask & eIndex++], rOp, event.attach, event.session);
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
