package com.tgx.queen.db.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tgx.queen.db.bean.TgxSharder;
import com.tgx.queen.db.templ.TgxClientTempl;
import com.tgx.queen.push.bean.TgxClient;


public class TgxClientDBExecutor
{
	
	private Connection connection;
	
	public TgxClientDBExecutor() {
		this.connection = JDBCUtils.getInstance().getConnection();
	}
	
	private TgxClient parseClient(ResultSet resultSet) throws SQLException {
		TgxClient client = new TgxClient();
		
		client.setId(resultSet.getLong(1));
		client.setClient_id(resultSet.getString(2));
		client.setC_time(resultSet.getDouble(3));
		client.setClientType(resultSet.getString(4));
		client.setThirdparty_push_token(resultSet.getString(5));
		
		ResultSet tag_set = resultSet.getArray(6).getResultSet();
		List<Integer> list = new ArrayList<Integer>();
		while (tag_set.next())
		{
			list.add(tag_set.getInt(2));
		}
		Integer[] tags = new Integer[list.size()];
		list.toArray(tags);
		client.setTags(tags);
		
		return client;
	}
	
	public TgxClient getClientByIndex(long clientIndex) throws SQLException {
		Statement statement = this.connection.createStatement();
		TgxClient client = null;
		String query = TgxClientTempl.getInstance().getClientByIndex(clientIndex);
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next())
		{
			client = this.parseClient(resultSet);
		}
		
		statement.close();
		return client;
	}
	
	public TgxClient getClientByIndexNoTag(long client_index) throws SQLException {
		Statement statement = this.connection.createStatement();
		TgxClient client = null;
		String query = TgxClientTempl.getInstance().getClientByIndexNoTag(client_index);
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next())
		{
			client = new TgxClient();
			client.setId(resultSet.getLong(1));
			client.setClient_id(resultSet.getString(2));
			client.setC_time(resultSet.getDouble(3));
			client.setClientType(resultSet.getString(4));
			client.setThirdparty_push_token(resultSet.getString(5));
		}
		
		statement.close();
		return client;
	}
	
	public TgxClient checkClientHash(String client_id) throws SQLException {
		Statement statement = this.connection.createStatement();
		TgxClient client = null;
		
		String query = TgxClientTempl.getInstance().checkClientHash(client_id);
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next())
		{
			client = new TgxClient();
			client.setId(resultSet.getLong(1));
			client.setClient_id(resultSet.getString(2));
		}
		
		return client;
	}
	
	public long insertClient(TgxClient client) throws SQLException {
		Statement statement = this.connection.createStatement();
		String client_id = client.getClient_id();
		String thirdparty_push_token = client.getThirdparty_push_token();
		String client_type = client.getClient_type();
		
		String query = TgxClientTempl.getInstance().insertClient(client_id, thirdparty_push_token, client_type);
		
		int re = statement.executeUpdate(query);
		String selectKey = TgxClientTempl.getInstance().selectKey();
		ResultSet resultSet = statement.executeQuery(selectKey);
		if (resultSet.next())
		{
			long id = resultSet.getLong(1);
			client.setId(id);
		}
		statement.close();
		
		return re;
	}
	
	public void updateClient(TgxClient client) throws SQLException {
		Statement statement = this.connection.createStatement();
		long id = client.getId();
		int[] tags = client.getTags();
		
		String query = TgxClientTempl.getInstance().updateClient(id, tags);
		statement.executeUpdate(query);
		statement.close();
	}
	
	public int countClientShardSubTable() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxClientTempl.getInstance().countClientShardSubTable();
		ResultSet resultSet = statement.executeQuery(query);
		int re = 0;
		if (resultSet.next())
		{
			re = resultSet.getInt(1);
		}
		
		statement.close();
		return re;
	}
	
	public long countClientId() throws SQLException {
		Statement statement = this.connection.createStatement();
		
		String query = TgxClientTempl.getInstance().countClientId();
		ResultSet resultSet = statement.executeQuery(query);
		int re = 0;
		if (resultSet.next())
		{
			re = resultSet.getInt(1);
		}
		statement.close();
		return re;
	}
	
	public void dropClientSchema() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxClientTempl.getInstance().dropClientSchema();
		statement.execute(query);
		statement.close();
	}
	
	public void createClientSchema() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxClientTempl.getInstance().createClientSchema();
		statement.execute(query);
		statement.close();
	}
	
	public void createClientHashTable() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxClientTempl.getInstance().createClientHashTable();
		statement.execute(query);
		statement.close();
	}
	
	public void createClientTable() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxClientTempl.getInstance().createClientTable();
		statement.execute(query);
		statement.close();
	}
	
	public void createClientSubShardTable(TgxSharder sharder) throws SQLException {
		long shard_id = sharder.getShard();
		long low = sharder.getLow();
		long high = sharder.getHigh();
		
		Statement statement = this.connection.createStatement();
		String query = TgxClientTempl.getInstance().createClientSubShardTable(shard_id, low, high);
		statement.execute(query);
		statement.close();
	}
	
	public void createClientShardInsertTriggerFun(TgxSharder sharder) throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxClientTempl.getInstance().createClientShardInsertTriggerFun(TgxSharder.perTableSize());
		statement.execute(query);
		
		statement.close();
	}
	
	public void createClientShardInsertTrigger() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxClientTempl.getInstance().createClientShardInsertTrigger();
		statement.execute(query);
		
		statement.close();
	}
	
	public List<TgxClient> getAllClients(long clientIndex) throws SQLException {
		List<TgxClient> list = new ArrayList<TgxClient>();
		Statement statement = this.connection.createStatement();
		String query = TgxClientTempl.getInstance().getAllClients(clientIndex);
		
		ResultSet resultSet = statement.executeQuery(query);
		while (resultSet.next())
		{
			TgxClient client = this.parseClient(resultSet);
			list.add(client);
		}
		statement.close();
		return list;
	}
	
	public static void main(String[] agrs) {
		TgxClientDBExecutor tce = new TgxClientDBExecutor();
		try
		{
			// TgxClient client = tce.checkClientHash("1");
			// System.out.println(client.getId());
			// System.out.println(client.getClient_id());
			// System.out.println(client.getCreateTime());
			// System.out.println(client.getClient_type());
			// System.out.println(client.getThirdparty_push_token());
			// System.out.println(client.getTags());
			
			List<TgxClient> list = tce.getAllClients(0);
			for (TgxClient client : list)
			{
				System.out.println(client.getId());
			}
			
			System.out.println(tce.getClientByIndex(1).getId());
			
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
