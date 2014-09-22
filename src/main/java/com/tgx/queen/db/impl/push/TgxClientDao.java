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
import org.postgresql.util.PSQLException;

import com.tgx.queen.db.TgxBaseDao;
import com.tgx.queen.db.TgxClientMapper;
import com.tgx.queen.db.bean.TgxSharder;
import com.tgx.queen.db.jdbc.JDBCUtils;
import com.tgx.queen.db.jdbc.TgxClientDBExecutor;
import com.tgx.queen.push.bean.TgxClient;


public class TgxClientDao
        extends
        TgxBaseDao
{
	private static TgxClientDao _instance;
	
	public final static TgxClientDao getInstance() {
		return _instance == null ? _instance = new TgxClientDao() : _instance;
	}
	
	private TgxClientDao() {
		super();
		int table_count = countTable();
		if (table_count == 0)
		{
			if (!createTable()) System.err.println("create table error!");
			// TODO 日志输出 建表失败
			for (int i = 0; i < 10; i++)
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
			long nextSerial = countClientID();
			TgxSharder sharder = new TgxSharder(nextSerial);
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
	
	private long countClientID() {
		if (sqlSessionFactory == null) return 0;
		SqlSession session = sqlSessionFactory.openSession();
		TgxClientMapper mapper = session.getMapper(TgxClientMapper.class);
		try
		{
			long count = 0;
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxClientDBExecutor executor = new TgxClientDBExecutor();
				count = executor.countClientId();
			}
			else
			{
				count = mapper.countClientId();
				session.commit();
			}
			
			return count;
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
		TgxClientMapper mapper = session.getMapper(TgxClientMapper.class);
		try
		{
			int count = 0;
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxClientDBExecutor executor = new TgxClientDBExecutor();
				count = executor.countClientShardSubTable();
			}
			else
			{
				count = mapper.countClientShardSubTable();
				session.commit();
			}
			return count;
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
	
	private void createTrigger() {
		if (sqlSessionFactory == null) return;
		SqlSession session = sqlSessionFactory.openSession();
		TgxClientMapper mapper = session.getMapper(TgxClientMapper.class);
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxClientDBExecutor executor = new TgxClientDBExecutor();
				executor.createClientShardInsertTrigger();
			}
			else
			{
				mapper.createClientShardInsertTrigger();
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
	
	private boolean createTable() {
		if (sqlSessionFactory == null) return false;
		SqlSession session = sqlSessionFactory.openSession();
		TgxClientMapper mapper = session.getMapper(TgxClientMapper.class);
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxClientDBExecutor executor = new TgxClientDBExecutor();
				executor.createClientSchema();
				executor.createClientHashTable();
				executor.createClientTable();
				executor.createClientShardInsertTriggerFun(new TgxSharder());
			}
			else
			{
				mapper.createClientSchema();
				mapper.createClientHashTable();
				mapper.createClientTable();
				mapper.createClientShardInsertTriggerFun(new TgxSharder());
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
	
	public boolean createSubTable(long serial) {
		if (sqlSessionFactory == null) return false;
		SqlSession session = sqlSessionFactory.openSession();
		TgxClientMapper mapper = session.getMapper(TgxClientMapper.class);
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxClientDBExecutor executor = new TgxClientDBExecutor();
				executor.createClientSubShardTable(new TgxSharder(serial));
			}
			else
			{
				mapper.createClientSubShardTable(new TgxSharder(serial));
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
	
	public TgxClient registerOne() {
		return registerOne(null, null, null);
	}
	
	public TgxClient registerOne(byte[] hash_info, String thirdparty_push_token, String client_type) {
		if (sqlSessionFactory == null) return null;
		SqlSession session = sqlSessionFactory.openSession();
		TgxClientMapper mapper = null;
		TgxClient client = null;
		try
		{
			mapper = session.getMapper(TgxClientMapper.class);
			client = new TgxClient().setClientId(crypt, hash_info);
			client.setThirdparty_push_token(thirdparty_push_token);
			client.setClientType(client_type);
			TgxClient client_ex = mapper.checkClientHash(client.getClientIdHex());// 当x06处于异常调用时,此处将成为性能瓶颈
			if (client_ex == null)
			{
				
				if (JDBCUtils.getInstance().jdbc_use)
				{
					TgxClientDBExecutor executor = new TgxClientDBExecutor();
					executor.insertClient(client);
				}
				else
				{
					mapper.insertClient(client);
					session.commit();
				}
				return client;
			}
			else return client_ex;
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
			session.close();
		}
		return null;
	}
	
	// mark
	public void updateClient(TgxClient client) {
		if (client == null) return;
		SqlSession session = sqlSessionFactory.openSession();
		TgxClientMapper mapper = session.getMapper(TgxClientMapper.class);
		try
		{
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxClientDBExecutor executor = new TgxClientDBExecutor();
				executor.updateClient(client);
			}
			else
			{
				mapper.updateClient(client);
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
	
	// mark
	public TgxClient getClient(long clientIndex) {
		if (clientIndex < 0) return null;
		
		SqlSession session = sqlSessionFactory.openSession();
		TgxClientMapper mapper = session.getMapper(TgxClientMapper.class);
		try
		{
			TgxClient client = null;
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxClientDBExecutor executor = new TgxClientDBExecutor();
				client = executor.getClientByIndex(clientIndex);
			}
			else
			{
				client = mapper.getClientByIndex(clientIndex);
				session.commit();
			}
			
			return client;
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
	
	// mark
	public List<TgxClient> getAllClientsFrom(long clientIndex) {
		SqlSession session = sqlSessionFactory.openSession();
		TgxClientMapper mapper = session.getMapper(TgxClientMapper.class);
		try
		{
			List<TgxClient> clients = null;
			if (JDBCUtils.getInstance().jdbc_use)
			{
				TgxClientDBExecutor executor = new TgxClientDBExecutor();
				clients = executor.getAllClients(clientIndex);
			}
			else
			{
				clients = mapper.getAllClients(clientIndex);
				session.commit();
			}
			
			return clients;
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
	
	public static void main(String[] agrs) {
		TgxClientDao tcd = TgxClientDao.getInstance();
		TgxClient client = tcd.registerOne();
		System.out.println(client.getClientIndex());
		System.out.println(tcd.countClientID());
		System.out.println(tcd.countTable());
		System.out.println(tcd.getClient(1).getId());
		List<TgxClient> list = tcd.getAllClientsFrom(0);
		for (TgxClient c : list)
		{
			System.out.println(c.getId());
		}
		TgxClient cc = new TgxClient();
		cc.setId(1);
		cc.setClient_id("9950AB42F9B76723D1C1041029698467F9D17E1560F98A96C3600136162FB54E");
		Integer[] tags = {
		        1,
		        2,
		        3
		};
		cc.setTags(tags);
		tcd.updateClient(cc);
	}
}
