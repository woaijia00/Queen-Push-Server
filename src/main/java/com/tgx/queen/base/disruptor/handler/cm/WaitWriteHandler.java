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
package com.tgx.queen.base.disruptor.handler.cm;

import com.lmax.disruptor.RingBuffer;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.bean.TgxEvent;
import com.tgx.queen.base.disruptor.handler.AbstractPipeHandler;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpCmWrite;
import com.tgx.queen.base.disruptor.handler.logic.operations.TgxOpImWrite;
import com.tgx.queen.io.inf.IQoS;
import com.tgx.queen.socket.aio.impl.AioSession;


/**
 * @author Zhangzhuo
 */
public class WaitWriteHandler
        extends
        AbstractPipeHandler<TgxEvent>
{
	
	private final RingBuffer<Event> writeBuffer;
	
	public WaitWriteHandler(final RingBuffer<Event> writeBuffer) {
		this.writeBuffer = writeBuffer;
	}
	
	@Override
	public void onEvent(TgxEvent event, long sequence, boolean endOfBatch) throws Exception {
		EventOp op = event.getOperator();
		if (op != null)
		{
			switch (op.getSerialNum()) {
				case EventOp.AIO_WRITE_SERIAL://TgxOpWrite
					EventOp rOp = op.hasError() ? op.errOp(event) : op.op(event);
					if (rOp != null) publish(writeBuffer, rOp, event.attach, event.session);//此处应该返回 TgxOpToWrite
					break;
				case EventOp.AIO_CM_WRITE_SERIAL://TgxOpCmWrite
					TgxOpCmWrite topcw = (TgxOpCmWrite) op;
					do
					{
						rOp = topcw.op(event);
						IQoS rQoS = topcw.getOpResult();
						AioSession session = topcw.getOpSession();
						if (rOp == null || rQoS == null || session == null) continue;
						publish(writeBuffer, rOp, rQoS, session);
					}
					while (topcw.moreWrite());
					break;
				case EventOp.AIO_IM_WRITE_SERIAL://TgxOpImWrite
					TgxOpImWrite topiw = (TgxOpImWrite) op;
					do
					{
						rOp = topiw.op(event);
						IQoS rQoS = topiw.getOpResult();
						AioSession session = topiw.getOpSession();
						if (rOp == null || rQoS == null || session == null) continue;
						publish(writeBuffer, rOp, rQoS, session);
					}
					while (topiw.moreWrite());
					break;
			}
			event.reset();// TgxEvent 完成自身使命
		}
		else System.err.println(getClass().getSimpleName() + " NILL  Operator! ");
	}
}
