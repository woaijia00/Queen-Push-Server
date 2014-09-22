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

import java.util.List;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.ibatis.session.SqlSession;
import org.postgresql.util.PSQLException;

import com.tgx.queen.db.TgxBaseDao;
import com.tgx.queen.db.TgxUsrMapper;
import com.tgx.queen.db.bean.TgxSharder;
import com.tgx.queen.db.jdbc.JDBCUtils;
import com.tgx.queen.db.jdbc.TgxUsrDBExecutor;
import com.tgx.queen.im.bean.TgxUsr;


public class TgxUsrDao
        extends
        TgxBaseDao
{
	private static TgxUsrDao _instance;
	
	public final static TgxUsrDao getInstance() {
		return _instance == null ? _instance = new TgxUsrDao() : _instance;
	}
	
	private TgxUsrDao() {
		super();
		int table_count = countTable();
		if (table_count == 0)
		{
			if (!createTable()) System.err.println("create table error!");// TODO
			                                                              // 日志输出
			                                                              // 建表失败
			for (int i = 0; i < 32; i++)
			{
				if (!createSubTable(i * TgxSharder.perTableSize()))
				{
					System.err.println("create sub table failed!");
					// TODO 写日志
				}
			}
		}
		else
		{
			long curr_serial = countUsrID();
			TgxSharder sharder = new TgxSharder(curr_serial);
			if (sharder.getShard() + 2 > table_count)
			{
				for (int i = 0, j = table_count - 1; i < 10; i++)
				{
					if (!createSubTable((j + i) * TgxSharder.perTableSize()))
					{
						System.err.println("create sub table failed!");
						// TODO 写日志
					}
				}
			}
		}
	}
	
	private long countUsrID() {
		if (sqlSessionFactory == null) return 0;
		SqlSession session = sqlSessionFactory.openSession();
		TgxUsrMapper mapper = null;
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxUsrDBExecutor executor = new TgxUsrDBExecutor();
				return executor.countUsrId();
			}
			else
			{
				mapper = session.getMapper(TgxUsrMapper.class);
				return mapper.countUsrId();
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
		return 0;
		
	}
	
	private int countTable() {
		if (sqlSessionFactory == null) return 0;
		SqlSession session = sqlSessionFactory.openSession();
		TgxUsrMapper mapper = null;
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxUsrDBExecutor executor = new TgxUsrDBExecutor();
				return executor.countUsrShardSubTable();
			}
			else
			{
				mapper = session.getMapper(TgxUsrMapper.class);
				return mapper.countUsrShardSubTable();
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
		return 0;
		
	}
	
	private boolean createTable() {
		if (sqlSessionFactory == null) return false;
		SqlSession session = sqlSessionFactory.openSession();
		TgxUsrMapper mapper = null;
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxUsrDBExecutor executor = new TgxUsrDBExecutor();
				executor.createUsrSchema();
				executor.createUsrTable();
				executor.createUsrShardInsertTriggerFun(new TgxSharder());
			}
			else
			{
				mapper = session.getMapper(TgxUsrMapper.class);
				mapper.createUsrSchema();
				mapper.createUsrTable();
				mapper.createUsrShardInsertTriggerFun(new TgxSharder());
				session.commit();
			}
			createTrigger();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return false;
	}
	
	private void createTrigger() {
		if (sqlSessionFactory == null) return;
		SqlSession session = sqlSessionFactory.openSession();
		TgxUsrMapper mapper = null;
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxUsrDBExecutor executor = new TgxUsrDBExecutor();
				executor.createUsrShardInsertTrigger();
			}
			else
			{
				mapper = session.getMapper(TgxUsrMapper.class);
				mapper.createUsrShardInsertTrigger();
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
	
	public boolean createSubTable(long serial) {
		if (sqlSessionFactory == null) return false;
		SqlSession session = sqlSessionFactory.openSession();
		TgxUsrMapper mapper = null;
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxUsrDBExecutor executor = new TgxUsrDBExecutor();
				executor.createUsrSubShardTable(new TgxSharder(serial));
			}
			else
			{
				mapper = session.getMapper(TgxUsrMapper.class);
				mapper.createUsrSubShardTable(new TgxSharder(serial));
				session.commit();
			}
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			session.close();
		}
		return false;
	}
	
	public TgxUsr registerOne() {
		return registerOne(null);
	}
	
	public TgxUsr registerOne(byte[] hash_info) {
		if (sqlSessionFactory == null) return null;
		SqlSession session = null;
		TgxUsrMapper mapper = null;
		TgxUsr usr = null;
		try
		{
			
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxUsrDBExecutor executor = new TgxUsrDBExecutor();
				usr = new TgxUsr(crypt, hash_info);
				executor.insertUsr(usr);
			}
			else
			{
				session = sqlSessionFactory.openSession();
				mapper = session.getMapper(TgxUsrMapper.class);
				usr = new TgxUsr(crypt, hash_info);
				mapper.insertUsr(usr);
				session.commit();
			}
			return usr;
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
	
	private LRUMap<Long, TgxUsr> usrCache = new LRUMap<>(1 << 16);
	
	// mark
	public TgxUsr getImUsr(long usrIndex) {
		
		TgxUsr usr = null;
		
		usr = usrCache.get(usrIndex);
		if (usr != null) return usr;
		if (usrIndex < 0 || sqlSessionFactory == null) return null;
		
		SqlSession session = sqlSessionFactory.openSession();
		TgxUsrMapper mapper = session.getMapper(TgxUsrMapper.class);
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxUsrDBExecutor executor = new TgxUsrDBExecutor();
				usr = executor.getImUsrByIndex(usrIndex);
			}
			else
			{
				usr = mapper.getImUsrByIndex(usrIndex);
				session.commit();
			}
			
			return usr;
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
						break;
				}
			}
		}
		finally
		{
			session.close();
		}
		return null;
	}
	
	public List<TgxUsr> getImUsrAll(long usrIndexOffset) {
		if (sqlSessionFactory == null) return null;
		SqlSession session = sqlSessionFactory.openSession();
		TgxUsrMapper mapper = session.getMapper(TgxUsrMapper.class);
		try
		{
			List<TgxUsr> usrs = null;
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxUsrDBExecutor executor = new TgxUsrDBExecutor();
				usrs = executor.getImUsrAll(usrIndexOffset);
			}
			else
			{
				usrs = mapper.getImUsrAll(usrIndexOffset);
				session.commit();
			}
			return usrs;
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
						break;
				}
			}
		}
		finally
		{
			session.close();
		}
		return null;
	}
	
	public void updateImUsr(TgxUsr usr) {
		SqlSession session = sqlSessionFactory.openSession();
		TgxUsrMapper mapper = session.getMapper(TgxUsrMapper.class);
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxUsrDBExecutor executor = new TgxUsrDBExecutor();
				executor.updateUsr(usr);
			}
			else
			{
				mapper.updateUsr(usr);
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
					case "42P01": // table not exists
						break;
					case "42P07": // table exists
						break;
					case "23505": // insert failed duplicate primaryKey or
						          // unique column
					default:
						break;
				}
			}
		}
		finally
		{
			session.close();
		}
	}
	
	public static void main(String[] agrs) {
		try
		{
			TgxUsrDao instance = TgxUsrDao.getInstance();
			int count = 1000;
			long startMili = System.currentTimeMillis();
			for (int i = 1; i <= count; i++)
			{
				instance.getImUsr(i);
			}
			long endMili = System.currentTimeMillis();
			System.out.println("总耗时为：" + (endMili - startMili) + "毫秒");
			System.out.println("单个耗时: " + (endMili - startMili) * 1.0 / count + "毫秒");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
