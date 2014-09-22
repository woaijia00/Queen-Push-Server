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

import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.postgresql.util.PSQLException;

import com.tgx.queen.db.TgxBaseDao;
import com.tgx.queen.db.TgxBindMapper;
import com.tgx.queen.db.jdbc.JDBCUtils;
import com.tgx.queen.db.jdbc.TgxBindDBExecutor;
import com.tgx.queen.im.bean.TgxBind;
import com.tgx.queen.im.bean.TgxUsr;


public class TgxBindDao
        extends
        TgxBaseDao
{
	public final static TgxBindDao getInstance() {
		return _instance == null ? _instance = new TgxBindDao() : _instance;
	}
	
	private static TgxBindDao _instance;
	
	private TgxBindDao() {
		super();
		createTable();
	}
	
	public boolean bind(TgxBind binder) {
		if (sqlSessionFactory == null) return false;
		SqlSession session = null;
		try
		{
			
			List<TgxBind> binds = null;
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxBindDBExecutor executor = new TgxBindDBExecutor();
				executor.insertBind(binder);
				binds = executor.getBindsByUsrIndex(binder.getImUsrIndex());
			}
			else
			{
				session = sqlSessionFactory.openSession();
				TgxBindMapper mapper = session.getMapper(TgxBindMapper.class);
				mapper.insertBind(binder);
				binds = mapper.getBindsByUsrIndex(binder.getImUsrIndex());
				session.commit();
			}
			
			TgxUsr usr = new TgxUsr(binder.getImUsrIndex());
			long[] lBinds = new long[binds.size()];
			usr.setNativeC_bind(lBinds);
			int i = 0;
			for (TgxBind b : binds)
				lBinds[i++] = b.getBindIndex();
			Arrays.sort(lBinds);
			TgxUsrDao.getInstance().updateImUsr(usr);
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			Throwable throwable = e.getCause();
			if (throwable instanceof PSQLException)
			{
				PSQLException psqle = (PSQLException) throwable;
				switch (psqle.getSQLState()) {
					case "42P01": // table not exists
						break;
					case "42P07": // table exists
						break;
					case "23505": // insert failed duplicate primaryKey or
						          // unique column
					default:
						return false;
				}
			}
		}
		finally
		{
			if (session != null) session.close();
		}
		return false;
	}
	
	public List<TgxBind> getBindUsrs(long clientIndex) {
		if (sqlSessionFactory == null) return null;
		SqlSession session = null;
		try
		{
			
			List<TgxBind> binds = null;
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxBindDBExecutor executor = new TgxBindDBExecutor();
				binds = executor.getBindUsrByBindIndex(clientIndex);
			}
			else
			{
				session = sqlSessionFactory.openSession();
				TgxBindMapper mapper = session.getMapper(TgxBindMapper.class);
				binds = mapper.getBindUsrByBindIndex(clientIndex);
				session.commit();
			}
			
			return binds;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			Throwable throwable = e.getCause();
			if (throwable instanceof PSQLException)
			{
				PSQLException psqle = (PSQLException) throwable;
				switch (psqle.getSQLState()) {
					case "42P01": // table not exists
						createTable();
						break;
					default:
						return null;
				}
			}
		}
		finally
		{
			if (session != null) session.close();
		}
		return null;
	}
	
	public void createTable() {
		if (sqlSessionFactory == null) return;
		SqlSession session = null;
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxBindDBExecutor executor = new TgxBindDBExecutor();
				executor.createBindSchema();
				executor.createBindTable();
			}
			else
			{
				session = sqlSessionFactory.openSession();
				TgxBindMapper mapper = session.getMapper(TgxBindMapper.class);
				mapper.createBindSchema();
				mapper.createBindTable();
				session.commit();
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			Throwable throwable = e.getCause();
			if (throwable instanceof PSQLException)
			{
				PSQLException psqle = (PSQLException) throwable;
				switch (psqle.getSQLState()) {
					case "42P07": // table exists
						break;
					default:
						break;
				}
			}
		}
		finally
		{
			if (session != null) session.close();
		}
	}
	
	public static void main(String[] agrs) {
		TgxBindDao bind = TgxBindDao.getInstance();
		TgxBind binder = new TgxBind();
		binder.setBindIndex(3);
		binder.setImUsrIndex(2);
		binder.setApp_id("97738E1690FA90A1F2943E5499B7B725787F0FC2FDB99C818960B6DDC4B13F3E");
		bind.bind(binder);
		
		System.out.println(bind.getBindUsrs(1).get(0).getBindIndex());
	}
}
