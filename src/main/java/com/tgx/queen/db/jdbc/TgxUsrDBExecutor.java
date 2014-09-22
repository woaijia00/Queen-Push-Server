package com.tgx.queen.db.jdbc;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tgx.queen.db.bean.TgxSharder;
import com.tgx.queen.db.templ.TgxUsrTempl;
import com.tgx.queen.im.bean.TgxUsr;


public class TgxUsrDBExecutor
{
	
	private Connection connection;
	
	public TgxUsrDBExecutor() {
		this.connection = JDBCUtils.getInstance().getConnection();
	}
	
	private TgxUsr parseUsr(ResultSet resultSet) throws SQLException {
		long id = resultSet.getLong(1);
		String im_usr_id = resultSet.getString(2);
		double c_time = resultSet.getDouble(3);
		double m_time = resultSet.getDouble(4);
		Array bind_array = resultSet.getArray(5);
		byte bind_type = resultSet.getByte(6);
		ResultSet bind_set = bind_array.getResultSet();
		List<Long> list = new ArrayList<Long>();
		while (bind_set.next())
		{
			list.add(bind_set.getLong(2));
		}
		Long[] c_bind = new Long[list.size()];
		list.toArray(c_bind);
		
		TgxUsr usr = new TgxUsr(id);
		usr.resetInstance(id, im_usr_id, c_time, m_time, c_bind, bind_type);
		return usr;
	}
	
	public TgxUsr getImUsrByIndex(long usr_index) throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxUsrTempl.getInstance().getImUsrByIndex(usr_index);
		ResultSet resultSet = statement.executeQuery(query);
		
		TgxUsr usr = null;
		if (resultSet.next())
		{
			usr = parseUsr(resultSet);
		}
		statement.close();
		return usr;
	}
	
	public List<TgxUsr> getImUsrAll(long offset) throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxUsrTempl.getInstance().getImUsrAll(offset);
		ResultSet resultSet = statement.executeQuery(query);
		
		List<TgxUsr> list = new ArrayList<TgxUsr>();
		while (resultSet.next())
		{
			TgxUsr usr = this.parseUsr(resultSet);
			list.add(usr);
		}
		statement.close();
		return list;
	}
	
	public int insertUsr(TgxUsr usr) throws SQLException {
		Statement statement = this.connection.createStatement();
		
		String query = TgxUsrTempl.getInstance().insertUsr(usr.getImUsrId());
		int re = statement.executeUpdate(query);
		String selectKey = TgxUsrTempl.getInstance().selectKey();
		ResultSet resultSet = statement.executeQuery(selectKey);
		if (resultSet.next())
		{
			long id = resultSet.getLong(1);
			usr.setId(id);
		}
		statement.close();
		return re;
	}
	
	public int deleteUsr(TgxUsr usr) throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxUsrTempl.getInstance().deleteUsr(usr.getUsrIndex());
		int re = statement.executeUpdate(query);
		statement.close();
		return re;
	}
	
	public int updateUsr(TgxUsr usr) throws SQLException {
		Statement statement = this.connection.createStatement();
		long id = usr.getUsrIndex();
		long[] c_bind = usr.getC_bind();
		String query = TgxUsrTempl.getInstance().updateUsr(id, c_bind);
		int re = statement.executeUpdate(query);
		statement.close();
		return re;
	}
	
	public void createUsrSchema() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxUsrTempl.getInstance().createUsrSchema();
		statement.execute(query);
		statement.close();
	}
	
	public void createUsrTable() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxUsrTempl.getInstance().createUsrTable();
		statement.execute(query);
		statement.close();
	}
	
	public void createUsrSubShardTable(TgxSharder sharder) throws SQLException {
		Statement statement = this.connection.createStatement();
		long share_id = sharder.getShard();
		long low = sharder.getLow();
		long high = sharder.getHigh();
		String query = TgxUsrTempl.getInstance().createUsrSubShardTable(share_id, low, high);
		statement.execute(query);
		statement.close();
	}
	
	public void createUsrShardInsertTriggerFun(TgxSharder sharder) throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxUsrTempl.getInstance().createUsrShardInsertTriggerFun(TgxSharder.perTableSize());
		statement.execute(query);
		statement.close();
	}
	
	// TODO
	public void createUsrShardInsertTrigger() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxUsrTempl.getInstance().createUsrShardInsertTrigger();
		
		statement.execute(query);
		statement.close();
	}
	
	public int countUsrShardSubTable() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxUsrTempl.getInstance().countUsrShardSubTable();
		
		ResultSet resultSet = statement.executeQuery(query);
		int re = 0;
		if (resultSet.next())
		{
			re = resultSet.getInt(1);
		}
		statement.close();
		return re;
	}
	
	public long countUsrId() throws SQLException {
		Statement statement = this.connection.createStatement();
		long re = 0;
		String query = TgxUsrTempl.getInstance().countUsrId();
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next())
		{
			re = resultSet.getLong(1);
		}
		statement.close();
		return re;
	}
	
	public static void main(String[] agrs) {
		TgxUsrDBExecutor tue = new TgxUsrDBExecutor();
		try
		{
			TgxUsr usr = tue.getImUsrAll(0).get(10);
			// Long[] bind = {(long)4,(long)5,(long) 9};
			// usr.setC_bind(bind);
			System.out.println(usr.getCreateTime());
			// System.out.println(tue.updateUsr(usr));
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
}
