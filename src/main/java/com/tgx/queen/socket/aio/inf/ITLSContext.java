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
package com.tgx.queen.socket.aio.inf;

import com.tgx.queen.base.encrpyt.Rc4;


public interface ITLSContext
{
	public boolean needUpdateKeyIn();
	
	public boolean needUpdateKeyOut();
	
	public void updateKeyIn();
	
	public void updateKeyOut();
	
	public int getRc4KeyId();
	
	public void setRc4KeyId(int rc4KeyId);
	
	public byte[] getRc4KeyIn();
	
	public byte[] getRc4KeyOut();
	
	public byte[] getReRollKey();
	
	public void reRollKey(byte[] key);
	
	public void swapKeyIn(byte[] key);
	
	public void swapKeyOut(byte[] key);
	
	public Rc4 getEncryptRc4();
	
	public Rc4 getDecryptRc4();
	
	public static enum EncryptState
	{
		PLAIN,
		ENCRYPTED
	}
	
	public EncryptState inState();
	
	public EncryptState outState();
	
	public void cryptIn();
	
	public void cryptOut();
	
}
