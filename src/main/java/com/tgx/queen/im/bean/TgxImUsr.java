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

import java.io.Serializable;
import java.util.Iterator;

import com.tgx.queen.base.inf.IDisposable;
import com.tgx.queen.base.util.SkipSet;
import com.tgx.queen.base.util.TgxRingBuffer;
import com.tgx.queen.im.bean.TgxImUsr.ClientState;


/* 记录了ImUser的状态信息，本信息必须在单一线程中操作，由于含有多处定向优化 */
public class TgxImUsr
        implements
        IDisposable,
        Iterable<ClientState>,
        Comparable<TgxImUsr>,
        Serializable
{
	private static final long     serialVersionUID = 9174194101246733502L;
	
	private int                   limit;
	private TgxRingBuffer<String> history;                                // HexGuid
	private long                  index;
	private boolean               isOnLine;
	private SkipSet<ClientState>  clients          = new SkipSet<>();
	private byte                  usr_type;
	private final static byte     NORMAL_USR       = 0, GROUP_USR = 1, CIRCLE_USR = 2;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + limit;
		result = prime * result + +((history == null) ? 0 : history.hashCode());
		result = (int) (prime * result + +index);
		result = prime * result + +(isOnLine ? 1 : 0);
		result = prime * result + +((clients == null) ? 0 : clients.hashCode());
		result = prime * result + +usr_type;
		return result;
	}
	
	@Override
	public void dispose() {
		history.clear();
		history = null;
		clients.clear();
		clients = null;
	}
	
	public int getHisLimit() {
		return limit;
	}
	
	public TgxImUsr(TgxUsr usr) {
		this(7, usr);
	}
	
	public TgxImUsr(int limit_bit, TgxUsr... usrs) {
		history = new TgxRingBuffer<String>(limit_bit).init(new String[1 << limit_bit]);
		TgxUsr usr = usrs[0];
		if (usrs.length == 1)
		{
			index = usr.getUsrIndex();
			long[] c_binds = usr.getC_bind();
			usr_type = NORMAL_USR;
			for (long clientIndex : c_binds)
				clients.insert(new ClientState(clientIndex));
			
		}
		else
		{
			isOnLine = true;
			if (usr.isGroupBind()) setGroupUsr();
			else if (usr.isCircleBind()) setCircleUsr();
			for (int i = 1; i < usrs.length; i++)
			{
				usr = usrs[i];
				long[] c_binds = usr.getC_bind();
				for (long clientIndex : c_binds)
					clients.insert(new ClientState(clientIndex));
			}
		}
		// Arrays.sort(clients);//c_bind 本身就是有序排列
	}
	
	public boolean isNormalUsr() {
		return usr_type == NORMAL_USR;
	}
	
	public void setGroupUsr() {
		usr_type = GROUP_USR;
	}
	
	public void setCircleUsr() {
		usr_type = CIRCLE_USR;
	}
	
	public boolean isGroupUsr() {
		return usr_type == GROUP_USR;
	}
	
	public boolean isCircleUsr() {
		return usr_type == CIRCLE_USR;
	}
	
	public long getUsrIndex() {
		return index;
	}
	
	public String addHis(String msgSeq) {
		return history.lruAdd(msgSeq);
	}
	
	public String rmHis() {
		return history.remove();
	}
	
	public Iterable<ClientState> getClients() {
		return this;
	}
	
	public void updateClients(TgxUsr usr) {
		for (ClientState cs : clients)
			if (!usr.contain(cs.clientIndex)) cs.isOnLine = false;
	}
	
	public TgxImUsr clientOnLine(long clientIndex) {
		ClientState cs = findClient(clientIndex);
		if (cs != null) cs.isOnLine = true;
		return this;
	}
	
	public TgxImUsr clientOffLine(long clientIndex) {
		ClientState cs = findClient(clientIndex);
		if (cs != null) cs.isOnLine = false;
		return this;
	}
	
	private final static ClientState keyCS = new ClientState();
	
	private ClientState findClient(long clientIndex) {
		if (clientIndex < 0) throw new IllegalArgumentException();
		keyCS.setClientIndex(clientIndex);
		return clients.find(keyCS);
	}
	
	public TgxImUsr offline() {
		isOnLine = false;
		return this;
	}
	
	public TgxImUsr online() {
		isOnLine = true;
		return this;
	}
	
	public boolean isAllOffline() {
		boolean allOff = true;
		for (ClientState state : clients)
		{
			if (state.isOnLine) return false;
		}
		return allOff;
	}
	
	public boolean isOnLine() {
		return isOnLine;
	}
	
	@Override
	public boolean isDisposable() {
		return true;
	}
	
	public static class ClientState
	        implements
	        IDisposable,
	        Comparable<ClientState>
	{
		
		private long    clientIndex = -1;
		private String  clientType  = "default";
		private boolean isOnLine;
		
		public ClientState() {
		}
		
		public ClientState(long clientIndex) {
			this.clientIndex = clientIndex;
		}
		
		public long getClientIndex() {
			return clientIndex;
		}
		
		public ClientState setClientIndex(long index) {
			clientIndex = index;
			return this;
		}
		
		public String getClientType() {
			return clientType;
		}
		
		public void setClientType(String clientType) {
			this.clientType = clientType;
		}
		
		public boolean isOnLine() {
			return isOnLine;
		}
		
		@Override
		public void dispose() {
			clientType = null;
		}
		
		@Override
		public int compareTo(ClientState o) {
			return clientIndex > o.clientIndex ? -1 : clientIndex < o.clientIndex ? 1 : 0;
		}
		
		@Override
		public boolean isDisposable() {
			return true;
		}
	}
	
	@Override
	public int compareTo(TgxImUsr o) {
		return index < o.index ? -1 : index > o.index ? 1 : 0;
	}
	
	@Override
	public Iterator<ClientState> iterator() {
		return clients.iterator();
	}
	
}
