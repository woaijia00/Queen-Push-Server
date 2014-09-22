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
package com.tgx.queen.db.impl.push;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.tgx.queen.db.TgxBaseDao;
import com.tgx.queen.db.TgxClientMapper;
import com.tgx.queen.db.TgxTagMapper;
import com.tgx.queen.db.jdbc.JDBCUtils;
import com.tgx.queen.db.jdbc.TgxClientDBExecutor;
import com.tgx.queen.db.jdbc.TgxTagDBExecutor;
import com.tgx.queen.push.bean.TgxClient;
import com.tgx.queen.push.bean.TgxTag;


public class TgxTagDao
        extends
        TgxBaseDao
{
	
	public List<TgxTag> getClientTags(long clientIndex) {
		if (sqlSessionFactory == null) return null;
		SqlSession session = sqlSessionFactory.openSession();
		TgxTagMapper mapper = session.getMapper(TgxTagMapper.class);
		TgxClientMapper cMapper = session.getMapper(TgxClientMapper.class);
		try
		{
			TgxClient client = null;
			List<TgxTag> list = null;
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxClientDBExecutor tce = new TgxClientDBExecutor();
				TgxTagDBExecutor tte = new TgxTagDBExecutor();
				client = tce.getClientByIndex(clientIndex);
				list = tte.getClientTags(client.getTags());
			}
			else
			{
				client = cMapper.getClientByIndex(clientIndex);
				list = mapper.getClientTags(client.getTags());
				session.commit();
			}
			
			return list;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return null;
	}
	
	public List<TgxTag> getTagDefine() {
		if (sqlSessionFactory == null) return null;
		SqlSession session = sqlSessionFactory.openSession();
		TgxTagMapper mapper = session.getMapper(TgxTagMapper.class);
		try
		{
			
			List<TgxTag> list = null;
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxTagDBExecutor executor = new TgxTagDBExecutor();
				list = executor.getTagDefine();
			}
			else
			{
				list = mapper.getTagDefine();
				session.commit();
			}
			
			return list;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return null;
	}
	
	private static TgxTagDao _instance;
	
	public final static TgxTagDao getInstance() {
		return _instance == null ? _instance = new TgxTagDao() : _instance;
	}
	
	private TgxTagDao() {
		super();
		createTable();
	}
	
	private void createTable() {
		if (sqlSessionFactory == null) return;
		SqlSession session = sqlSessionFactory.openSession();
		TgxTagMapper mapper = session.getMapper(TgxTagMapper.class);
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxTagDBExecutor executor = new TgxTagDBExecutor();
				executor.createTagSchema();
				executor.createTagDefineTable();
			}
			else
			{
				mapper.createTagSchema();
				mapper.createTagDefineTable();
				session.commit();
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
	}
	
}
