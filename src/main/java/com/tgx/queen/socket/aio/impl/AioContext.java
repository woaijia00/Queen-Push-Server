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

import java.nio.ByteBuffer;

import com.tgx.queen.base.encrpyt.Rc4;
import com.tgx.queen.io.bean.TgxCommand;
import com.tgx.queen.socket.aio.inf.IAioContext;
import com.tgx.queen.socket.aio.inf.ITLSContext;


/**
 * @author Zhangzhuo
 */
public abstract class AioContext
        implements
        IAioContext,
        ITLSContext
{
	
	public int           version, decodingIndex, dataNeed;
	public ByteBuffer    recvBuf;
	public DecodeState   dState          = DecodeState.DECODING_FRAME;
	public ChannelState  cState          = ChannelState.CLOSED;
	
	public int           pubKey_id       = -2;
	private boolean      updateKey_in, updateKey_out;
	private EncryptState crypt_in_state  = EncryptState.PLAIN;
	private EncryptState crypt_out_state = EncryptState.PLAIN;
	private int          rc4Key_id;
	private byte[]       rc4Key_in, rc4Key_out, rc4Key_reroll;
	private Rc4          eRc4, dRc4;
	private boolean      handshakeStart;
	
	/**
     * 
     */
	public AioContext() {
		version = TgxCommand.version;
	}
	
	public final void noHandshake() {
		handshakeStart = false;
		dState = DecodeState.DECODING_FRAME;
	}
	
	public final void handshake() {
		handshakeStart = true;
		dState = DecodeState.DECODED_HANDSHANKE;
	}
	
	@Override
	public void reset() {
		dState = handshakeStart ? DecodeState.DECODED_HANDSHANKE : DecodeState.DECODING_FRAME;
		cState = ChannelState.CLOSED;
		crypt_in_state = crypt_out_state = EncryptState.PLAIN;
		recvBuf.clear();
	}
	
	@Override
	public void dispose() {
		recvBuf = null;
		dState = null;
		cState = null;
		crypt_in_state = crypt_out_state = null;
		rc4Key_in = rc4Key_out = rc4Key_reroll = null;
		if (eRc4 != null) eRc4.reset();
		if (dRc4 != null) dRc4.reset();
		eRc4 = dRc4 = null;
	}
	
	@Override
	public boolean isDisposable() {
		return true;
	}
	
	@Override
	public Rc4 getDecryptRc4() {
		return dRc4 == null ? dRc4 = new Rc4() : dRc4;
	}
	
	@Override
	public Rc4 getEncryptRc4() {
		return eRc4 == null ? eRc4 = new Rc4() : eRc4;
	}
	
	@Override
	public int getRc4KeyId() {
		return rc4Key_id;
	}
	
	@Override
	public void setRc4KeyId(int rc4KeyId) {
		rc4Key_id = rc4KeyId;
	}
	
	@Override
	public byte[] getRc4KeyIn() {
		return rc4Key_in;
	}
	
	@Override
	public byte[] getRc4KeyOut() {
		return rc4Key_out;
	}
	
	@Override
	public byte[] getReRollKey() {
		return rc4Key_reroll;
	}
	
	@Override
	public boolean needUpdateKeyIn() {
		if (updateKey_in)
		{
			updateKey_in = false;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean needUpdateKeyOut() {
		if (updateKey_out)
		{
			updateKey_out = false;
			return true;
		}
		return false;
	}
	
	@Override
	public void updateKeyIn() {
		updateKey_in = true;
	}
	
	@Override
	public void updateKeyOut() {
		updateKey_out = true;
	}
	
	@Override
	public EncryptState inState() {
		return crypt_in_state;
	}
	
	@Override
	public EncryptState outState() {
		return crypt_out_state;
	}
	
	@Override
	public void cryptIn() {
		crypt_in_state = EncryptState.ENCRYPTED;
	}
	
	@Override
	public void cryptOut() {
		crypt_out_state = EncryptState.ENCRYPTED;
	}
	
	@Override
	public void reRollKey(byte[] key) {
		rc4Key_reroll = key;
	}
	
	@Override
	public void swapKeyIn(byte[] key) {
		rc4Key_in = key;
	}
	
	@Override
	public void swapKeyOut(byte[] key) {
		rc4Key_out = key;
	}
}
