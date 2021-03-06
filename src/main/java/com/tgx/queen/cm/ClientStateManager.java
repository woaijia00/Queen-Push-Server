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
package com.tgx.queen.cm;

import static java.util.Arrays.fill;

import java.util.HashMap;
import java.util.Map;


public class ClientStateManager
{
	private Map<Long, long[]> usrLoginMap = new HashMap<>();
	
	public final static ClientStateManager getInstance() {
		return _instance == null ? _instance = new ClientStateManager() : _instance;
	}
	
	private static ClientStateManager _instance;
	
	public long[] getUsrOnSession(long clientIndex) {
		return usrLoginMap.get(clientIndex);
	}
	
	public void bindUsr(long clientIndex, long usrIndex) {
		long[] usrIndexes = usrLoginMap.get(clientIndex);
		if (usrIndexes == null)
		{
			usrIndexes = new long[4];
			fill(usrIndexes, -1);
		}
		int i;
		for (i = 0; i < usrIndexes.length; i++)
		{
			if (usrIndexes[i] == -1)
			{
				usrIndexes[i] = usrIndex;
				return;
			}
		}
		long[] t = usrIndexes;
		usrIndexes = new long[t.length + 1];
		System.arraycopy(t, 0, usrIndexes, 0, t.length);
		usrIndexes[t.length] = usrIndex;
	}
}
