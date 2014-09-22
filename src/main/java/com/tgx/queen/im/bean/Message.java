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
package com.tgx.queen.im.bean;

import com.tgx.queen.base.bean.BaseMessage;
import com.tgx.queen.base.message.MsgSequenceManager;


public class Message
        extends
        BaseMessage

{
	private long origin   = -1; // usr-Index;
	private long target   = -1; // account-Index;
	private long thread   = -1; // thread-Index;
	private long sequence = -1;
	
	public Message() {
		this(null);
	}
	
	private Message(Message oMsg) {
		super(oMsg);
		if (oMsg != null)
		{
			origin = oMsg.origin;
			target = oMsg.target;
			thread = oMsg.thread;
		}
	}
	
	public final Message duplicate() {
		return new Message(this);
	}
	
	public final long getOrigin() {
		return origin;
	}
	
	public final Message setOrigin(long origin) {
		this.origin = origin;
		return this;
	}
	
	public final long getTarget() {
		return target;
	}
	
	public final Message setTarget(long target) {
		this.target = target;
		return this;
	}
	
	public final long getThread() {
		return thread;
	}
	
	public final Message setThread(long thread) {
		this.thread = thread;
		return this;
	}
	
	public final long getSequence() {
		return sequence;
	}
	
	public final Message setSequence(long sequence) {
		this.sequence = sequence;
		return this;
	}
	
	public final Message setGuid(MsgSequenceManager sFactory) {
		sFactory.createGUid(this);
		return this;
	}
	
	public final Message setSequence(MsgSequenceManager sFactory) {
		sFactory.setSequence(this);
		return this;
	}
	
}
