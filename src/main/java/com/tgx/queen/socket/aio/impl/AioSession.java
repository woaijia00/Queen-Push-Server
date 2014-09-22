/**
 * - Copyright (c) 2013 Zhang Zhuo All rights reserved. Redistribution and use
 * in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 1. Redistributions of source
 * code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. 2. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific written permission. THIS SOFTWARE
 * IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
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
package com.tgx.queen.socket.aio.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.WritePendingException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tgx.queen.base.inf.IDisposable;
import com.tgx.queen.base.inf.IReset;
import com.tgx.queen.base.util.SkipSet;
import com.tgx.queen.io.inf.IQoS;
import com.tgx.queen.io.inf.IWriteable;
import com.tgx.queen.socket.aio.inf.ISessionManager;
import com.tgx.queen.socket.aio.websocket.WSContext;


public class AioSession
        implements
        IDisposable,
        IReset,
        Closeable,
        Comparable<AioSession>,
        IWriteable<AioWriteHandler>
{
	public final static int                 SO_PP_BUF  = 1 << 12;
	public final static int                 SO_TCP_MTU = 1500 - 40;
	
	private final AsynchronousSocketChannel asyncSocketChannel;
	private String                          remote;
	private String                          decribe;
	private final AtomicBoolean             closed     = new AtomicBoolean(false);       //此变量一直处于单线程写的状态，可以不用 Atomic 变量操作
	private final AioContext                ctx        = new WSContext();
	private final int                       hashCode   = hashCode();
	private Map<String, Object>             attributes = null;
	private long                            _index     = -1;
	private final ISessionManager           myManager;
	private final AioFilterChain            inFilter, outFilter;
	private final SkipSet<IQoS>             sendQueue  = new SkipSet<>();
	public final ByteBuffer                 sending    = ByteBuffer.allocate(SO_PP_BUF);
	public final ByteBuffer                 recvBuf    = ByteBuffer.allocate(SO_TCP_MTU);
	private long                            writeSequence;
	private int                             searchKey;
	private boolean                         wrFinish;
	
	public final void setSearchKey(int key) {
		searchKey = key;
	}
	
	public final void setIndex(long index) {
		this._index = index;
	}
	
	public final long getIndex() {
		return _index;
	}
	
	public final String getRemoteUrl() {
		return remote;
	}
	
	public final AsynchronousSocketChannel getChannel() {
		return asyncSocketChannel;
	}
	
	@Override
	public final void dispose() {
		ctx.dispose();
		sendQueue.clear();
		if (attributes != null) attributes.clear();
		attributes = null;
		remote = null;
		decribe = null;
		_index = -1;
	}
	
	@Override
	public boolean isDisposable() {
		return true;
	}
	
	@Override
	public void reset() {
		_index = -1;
		searchKey = 0;
	}
	
	@Override
	public final void close() throws IOException {
		if (isClosed()) return;
		boolean closed = false;
		for (;;)
		{
			closed = this.closed.get();
			if (closed || this.closed.compareAndSet(false, true)) break;
		}
		asyncSocketChannel.close();
	}
	
	public final boolean isClosed() {
		return closed.get();
	}
	
	public final void readNext(AioReadHandler readHandler) {
		asyncSocketChannel.read(recvBuf, 15, TimeUnit.MINUTES, this, readHandler);
	}
	
	public final Object read() throws Exception {
		Object result = isClosed() ? null : inFilter.filterChainDecode(this, recvBuf.flip());
		recvBuf.clear();
		return result;
	}
	
	int i = 0;
	
	public final void writeNext(AioWriteHandler writeHandler) throws WritePendingException, NotYetConnectedException, Exception {
		wrFinish = false;
		if (!isClosed() && sending.hasRemaining()) writeChannel(sending, writeHandler);
		else if (!isClosed())
		{
			IQoS tick = sendQueue.removeFirst();
			if (tick != null) writeChannel(tick, writeHandler);
		}
		else sendQueue.clear();
	}
	
	@Override
	public final void write(IQoS content, AioWriteHandler writeHandler) throws WritePendingException, NotYetConnectedException, Exception {
		if (isClosed()) return;
		content.setSequence(writeSequence++);//TODO 在服务器长时间服务条件下需要关注当前链接的 Sequence 是否会出现越界问题。
		if (sendQueue.size() > 64) System.err.println("Session: " + toString() + " send Buf length > 64!");
		else if (sendQueue.isEmpty() && !wrFinish) writeChannel(content, writeHandler);
		else sendQueue.add(content);
	}
	
	private final void writeChannel(IQoS content, AioWriteHandler writeHandler) throws WritePendingException, NotYetConnectedException, Exception {
		writeChannel(outFilter.filterChainEncode(this, content), writeHandler);
	}
	
	private final void writeChannel(ByteBuffer buf, AioWriteHandler writeHandler) throws WritePendingException, NotYetConnectedException, Exception {
		wrFinish = true;
		asyncSocketChannel.write(sending, 30, TimeUnit.SECONDS, this, writeHandler);
	}
	
	public AioSession(final AsynchronousSocketChannel channel, ISessionManager manager, AioFilterChain filter) throws IOException {
		asyncSocketChannel = channel;
		remote = channel.getRemoteAddress().toString();
		decribe = channel.getLocalAddress().toString();
		myManager = manager;
		outFilter = filter.getChainTail();
		inFilter = filter.getChainHead();
		sending.limit(0);
		channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
		channel.setOption(StandardSocketOptions.SO_RCVBUF, SO_TCP_MTU);
		channel.setOption(StandardSocketOptions.SO_SNDBUF, SO_PP_BUF);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("AioSession@").append(hashCode);
		//formatter:off
		sb.append(" remote: ").append(remote)
		.append(" sQueue-count: ").append(sendQueue.size())
		.append(" wait-send: ").append(sending.remaining())
		.append(" closed: " ).append(isClosed());
		//formatter:on
		return sb.toString();
	}
	
	public final ISessionManager getMyManager() {
		return myManager;
	}
	
	public final AioContext getContext() {
		return ctx;
	}
	
	public final Object getAttr(Class<?> clazz) {
		return attributes == null ? null : attributes.get(clazz.getName());
	}
	
	public final Object setAttr(Object obj) {
		return attributes == null ? null : attributes.put(obj.getClass().getName(), obj);
	}
	
	public final Object getAttr(String attrName) {
		return attributes == null ? null : attributes.get(attrName);
	}
	
	public final Object setAttr(String name, Object obj) {
		return attributes == null ? null : attributes.put(name, obj);
	}
	
	public final String localDec() {
		return decribe;
	}
	
	@Override
	public final int compareTo(AioSession o) {
		int keyOff = searchKey - o.searchKey;
		return keyOff == 0 ? hashCode - o.hashCode : keyOff;
	}
	
}
