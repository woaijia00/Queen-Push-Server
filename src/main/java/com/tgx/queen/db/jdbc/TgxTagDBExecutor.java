package com.tgx.queen.db.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.tgx.queen.db.templ.TgxTagTempl;
import com.tgx.queen.push.bean.TgxTag;


public class TgxTagDBExecutor
{
	
	private Connection connection;
	
	public TgxTagDBExecutor() {
		this.connection = JDBCUtils.getInstance().getConnection();
	}
	
	private TgxTag parseTgxTag(ResultSet resultSet) throws SQLException {
		TgxTag tag = new TgxTag();
		tag.setId(resultSet.getLong(1));
		tag.setTag(resultSet.getString(2));
		tag.setAppId(resultSet.getString(3));
		tag.setDomian(resultSet.getString(4));
		return tag;
	}
	
	public List<TgxTag> getClientTags(int[] tagIds) throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxTagTempl.getInstance().getClientTags(tagIds);
		
		List<TgxTag> list = new ArrayList<TgxTag>();
		ResultSet resultSet = statement.executeQuery(query);
		while (resultSet.next())
		{
			TgxTag tag = parseTgxTag(resultSet);
			list.add(tag);
		}
		
		statement.close();
		return list;
	}
	
	public void createTagSchema() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxTagTempl.getInstance().createTagSchema();
		statement.execute(query);
		
		statement.close();
	}
	
	public void createTagDefineTable() throws SQLException {
		Statement statement = this.connection.createStatement();
		String query = TgxTagTempl.getInstance().createTagDefineTable();
		
		statement.execute(query);
		statement.close();
	}
	
	public void insertTagDefine(TgxTag tag) throws SQLException {
		Statement statement = this.connection.createStatement();
		String tags = tag.getTag();
		String app_id = tag.getAppId();
		String domain = tag.getDomian();
		String query = TgxTagTempl.getInstance().insertTagDefine(tags, app_id, domain);
		statement.executeUpdate(query);
		
		statement.close();
	}
	
	public List<TgxTag> getTagDefine() throws SQLException {
		Statement statement = this.connection.createStatement();
		List<TgxTag> list = new ArrayList<TgxTag>();
		
		String query = TgxTagTempl.getInstance().getTagDefine();
		ResultSet resultSet = statement.executeQuery(query);
		while (resultSet.next())
		{
			TgxTag tag = parseTgxTag(resultSet);
			list.add(tag);
		}
		
		statement.close();
		return list;
	}
	
	public static void main(String[] agrs) {
		TgxTag tag = new TgxTag();
		tag.setAppId("app_id2");
		tag.setDomian("domain2");
		tag.setTag("tag2");
		
		TgxTagDBExecutor tte = new TgxTagDBExecutor();
		try
		{
			tte.insertTagDefine(tag);
			tte.createTagSchema();
			tte.createTagDefineTable();
			
			int[] ts = {
			        1,
			        2
			};
			List<TgxTag> tags = tte.getClientTags(ts);
			if (tags != null) for (TgxTag tt : tags)
			{
				System.out.println(tt.getAppId());
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
