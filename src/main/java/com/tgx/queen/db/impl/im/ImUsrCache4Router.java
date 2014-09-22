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
package com.tgx.queen.db.impl.im;

import org.apache.commons.collections4.map.LRUMap;

import com.tgx.queen.im.bean.TgxImUsr;
import com.tgx.queen.im.bean.TgxUsr;


public class ImUsrCache4Router
{
	private LRUMap<Long, TgxImUsr> usrCache      = new LRUMap<>(1 << 24);
	private TgxUsrDao              tgxUsrDao     = TgxUsrDao.getInstance();
	private TgxMessageDao          tgxMessageDao = new TgxMessageDao();
	
	public TgxImUsr getUsr(long usrIndex) {
		if (usrIndex < 0) return null;
		TgxImUsr usr = usrCache.get(usrIndex);
		if (usr == null)
		{
			TgxUsr tUsr = tgxUsrDao.getImUsr(usrIndex);
			if (tUsr != null)
			{
				usr = new TgxImUsr(tUsr);
				TgxImUsr rm = usrCache.put(usrIndex, usr);
				if (rm != null)
				{
					String hexGuid = null;
					do
					{
						hexGuid = rm.rmHis();
						if (hexGuid != null) tgxMessageDao.backCache(hexGuid);
					}
					while (hexGuid != null);
				}
			}
		}
		return usr;
	}
	
	private static ImUsrCache4Router _instance;
	
	public static ImUsrCache4Router getInstance() {
		return _instance == null ? _instance = new ImUsrCache4Router() : _instance;
	}
}
