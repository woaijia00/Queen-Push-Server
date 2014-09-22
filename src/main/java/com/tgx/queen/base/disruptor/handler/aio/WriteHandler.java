/**
 * - Copyright (c) 2013 Zhang Zhuo All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification,
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or
 * other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific written permission.
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES,
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
package com.tgx.queen.base.disruptor.handler.aio;

import com.lmax.disruptor.EventHandler;
import com.tgx.queen.base.disruptor.bean.Event;
import com.tgx.queen.base.disruptor.handler.inf.EventOp;


/**
 * 负荷出现在 FilterChain 的 Encode 上，以及 ISessionManager.findSession(ClientIndex);
 * WriteHandler 管理所有的 RB 所以在这个 Handler 的末尾处理所有的 Event.reset()
 * 
 * @author Zhangzhuo
 */

public class WriteHandler
        implements
        EventHandler<Event>
{
	
	@Override
	public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
		EventOp op = event.getOperator();
		if (op != null)
		{
			switch (op.getSerialNum()) {
				case EventOp.AIO_TO_WRITE_SERIAL://TgxOpToWrite
				case EventOp.AIO_WROTE_SERIAL://AioOpFinishWrote
					if (op.hasError()) op.errOp(event);
					else op.op(event);
					break;
			}
		}
		else System.err.println(getClass().getSimpleName() + " NILL  Operator!");
		event.reset();// Event 完成自身使命
	}
}
