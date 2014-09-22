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
package com.tgx.queen.im;

import java.util.Iterator;

import org.apache.commons.collections4.map.LRUMap;

import com.tgx.queen.im.bean.TgxImUsr;
import com.tgx.queen.im.bean.TgxUsr;


public class UsrStateManager
        implements
        Iterator<TgxImUsr>,
        Iterable<TgxImUsr>
{
	public final static UsrStateManager getInstance() {
		return _instance == null ? _instance = new UsrStateManager() : _instance;
	}
	
	private static UsrStateManager       _instance;
	private final LRUMap<Long, TgxImUsr> mHotMap;
	private final LRUMap<Long, TgxImUsr> mColdMap;
	
	private UsrStateManager() {
		this(1 << 23);
	}
	
	private UsrStateManager(int capacity) {
		int hotCap = capacity >>> 2;
		mHotMap = new LRUMap<>(hotCap);
		mColdMap = new LRUMap<>(capacity - hotCap);
	}
	
	public void loginUsr(TgxUsr dbUsr, long clientIndex) {
		long usrIndex = dbUsr.getUsrIndex();
		LRUMap<Long, TgxImUsr> map = isFocusUsr(usrIndex) ? mHotMap : mColdMap;
		TgxImUsr usr = map.get(usrIndex);
		if (usr == null && map == mHotMap) usr = mColdMap.get(usrIndex);
		if (usr == null) usr = new TgxImUsr(dbUsr);
		usr.clientOnLine(clientIndex);
		usr.online();
		TgxImUsr usrr = null;
		if (map == mHotMap && (usrr = map.put(usrIndex, usr)) != null && !usr.equals(usrr)) mColdMap.put(usrr.getUsrIndex(), usrr);
	}
	
	public void logoutUsr(long usrIndex) {
		LRUMap<Long, TgxImUsr> map = isFocusUsr(usrIndex) ? mHotMap : mColdMap;
		TgxImUsr usr = map.remove(usrIndex);
		if (usr == null && map == mHotMap) mColdMap.remove(usrIndex);
		if (usr != null) usr.offline();
	}
	
	public void logoutUsr(long usrIndex, long client) {
		LRUMap<Long, TgxImUsr> map = isFocusUsr(usrIndex) ? mHotMap : mColdMap;
		TgxImUsr usr = map.get(usrIndex);
		if (usr == null && map == mHotMap) usr = mColdMap.get(usrIndex);
		if (usr != null)
		{
			usr.clientOffLine(client);
			if (usr.isAllOffline())
			{
				usr.offline();
				TgxImUsr x = map.remove(usrIndex);
				if (map == mHotMap && x == null) mColdMap.remove(usrIndex);
			}
		}
	}
	
	private boolean isFocusUsr(long usrIndex) {
		// TODO HotLogic/ColdLogic
		return true;
	}
	
	@Override
	public boolean hasNext() {
		return mHotIterator.hasNext() || mColdIterator.hasNext();
	}
	
	@Override
	public TgxImUsr next() {
		nextMask = mHotIterator.hasNext() ? 1 : 0;
		if ((nextMask & 1) != 0) return mHotIterator.next();
		return mColdIterator.next();
	}
	
	@Override
	public void remove() {
		if ((nextMask & 0x80) != 0) throw new IllegalStateException();
		if ((nextMask & 1) != 0) mHotIterator.remove();
		else mColdIterator.remove();
		nextMask |= 0x80;
	}
	
	private int                nextMask = 0x80;
	private Iterator<TgxImUsr> mHotIterator;
	private Iterator<TgxImUsr> mColdIterator;
	
	@Override
	public Iterator<TgxImUsr> iterator() {
		mHotIterator = mHotMap.values().iterator();
		mColdIterator = mColdMap.values().iterator();
		return this;
	}
}
