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
package com.tgx.queen.db;

import java.util.List;

import com.tgx.queen.db.bean.TgxSharder;
import com.tgx.queen.im.bean.TgxUsr;


public interface TgxUsrMapper
{
	public TgxUsr getImUsrByIndex(long usr_index);
	
	public List<TgxUsr> getImUsrAll(long offset);
	
	public int insertUsr(TgxUsr usr);
	
	public int deleteUsr(TgxUsr usr);
	
	public int updateUsr(TgxUsr usr);
	
	public void createUsrSchema();
	
	public void createUsrTable();
	
	public void createUsrSubShardTable(TgxSharder sharder);
	
	public void createUsrShardInsertTriggerFun(TgxSharder sharder);
	
	public void createUsrShardInsertTrigger();
	
	public int countUsrShardSubTable();
	
	public long countUsrId();
}
