package com.tgx.queen.db.templ;

import com.tgx.queen.db.jdbc.JDBCUtils;


public class TgxClientTempl
{
	
	private static TgxClientTempl _instance;
	private String                schema_cm;
	private String                tName_cm_master;
	private String                tName_cm_hash_global;
	
	final String                  AllClientColumns   = "id,client_id,extract(epoch from c_time) as \"c_time\"," + "client_type,thirdparty_push_token,tags";
	final String                  NoTagClientColumns = "id,client_id,extract(epoch from c_time) as \"c_time\"," + "client_type,thirdparty_push_token";
	
	public TgxClientTempl() {
		this.schema_cm = JDBCUtils.getInstance().schema_cm;
		this.tName_cm_master = JDBCUtils.getInstance().tName_cm_master;
		this.tName_cm_hash_global = JDBCUtils.getInstance().tName_cm_hash_global;
	}
	
	public static TgxClientTempl getInstance() {
		return _instance == null ? _instance = new TgxClientTempl() : _instance;
	}
	
	public String getClientByIndex(long client_index) {
		return "select " + AllClientColumns + " from " + this.schema_cm + "." + this.tName_cm_master + " where id = " + client_index + "::bigint";
	}
	
	public String getClientByIndexNoTag(long client_index) {
		return "select " + NoTagClientColumns + " from " + this.schema_cm + "." + this.tName_cm_master + " where id = " + client_index + "::bigint";
	}
	
	public String checkClientHash(String client_id) {
		return "select " + this.schema_cm + "." + this.tName_cm_hash_global + ".id," + this.schema_cm + "." + this.tName_cm_hash_global + ".hash as \"client_id\"" + " from " + this.schema_cm + "." + this.tName_cm_hash_global + " where " + "hash ='" + client_id + "'";
	}
	
	public String insertClient(String client_id, String thirdparty_push_token, String client_type) {
		return "insert into " + this.schema_cm + "." + this.tName_cm_master + " (client_id,thirdparty_push_token,client_type) " + "values" + "('" + client_id + "', '" + thirdparty_push_token + "', '" + client_type + "')";
	}
	
	public String selectKey() {
		return "select currval('" + this.schema_cm + "." + this.tName_cm_master + "_id_seq')";
	}
	
	public String updateClient(long id, int[] tags) {
		String tag_str = "{";
		for (int i = 0; i < tags.length - 1; i++)
		{
			tag_str += tags[i] + ",";
		}
		tag_str += tags[tags.length - 1] + "}";
		
		String query = "update " + this.schema_cm + "." + this.tName_cm_master + " set tags = '" + tag_str + "' where id=" + id + "::bigint";
		
		return query;
	}
	
	public String countClientShardSubTable() {
		return "select " + "count(*) from   pg_catalog.pg_class c " + "join   " + "pg_catalog.pg_namespace n on n.oid = c.relnamespace " + "where  " + "c.relkind = 'r' " + "and    " + "c.relname like '%" + this.tName_cm_master + "%' " + "and    " + "n.nspname = '" + this.schema_cm + "';";
	}
	
	public String countClientId() {
		return "select last_value from " + this.schema_cm + "." + this.tName_cm_master + "_id_seq";
	}
	
	public String dropClientSchema() {
		return "drop table if exists " + this.schema_cm + " cascade;";
	}
	
	public String createClientSchema() {
		return "create schema if not exists " + this.schema_cm + ";";
	}
	
	public String createClientHashTable() {
		return "create table if not exists " + this.schema_cm + "." + this.tName_cm_hash_global + "(" + "id	 		bigint unique," + "hash 		char(64) primary key" + ");";
	}
	
	public String createClientTable() {
		return "create table if not exists " + this.schema_cm + "." + this.tName_cm_master + "(" + "id 				bigserial primary key," + "client_id 		char(64) not null," + "c_time 			timestamp default now()," + "tags			int[] default '{-1}',			" + "client_type 	text," + "thirdparty_push_token char(128)" + ");";
	}
	
	public String createClientSubShardTable(long shard_id, long low, long high) {
		return "create table if not exists " + this.schema_cm + "." + this.tName_cm_master + "_" + shard_id + "(" + "check (id >= " + low + "::bigint and id < " + high + "::bigint)" + ") " + "inherits (" + this.schema_cm + "." + this.tName_cm_master + ");";
	}
	
	public String createClientShardInsertTriggerFun(long shard_size) {
		return "create or replace function\n" + this.schema_cm + ".\"" + this.tName_cm_master + "_insert_trigger\"()\n" + "returns trigger as $body$\n" + "declare shard_id bigint;\n" + "sub_table text;\n" + "begin\n" + "select new.id / " + shard_size + " into shard_id;\n" + "sub_table:='" + this.tName_cm_master + "_'||shard_id;\n" + "execute 'insert into " + this.schema_cm + ".' || quote_ident(sub_table) || ' values (($1).*)' using new;\n" + "execute 'insert into " + this.schema_cm + "." + this.tName_cm_hash_global + "(id,hash) values(($1).id,($1).client_id)' using new;\n" + "return null;\n" + "end;\n" + "$body$\n" + "language plpgsql;";
	}
	
	public String createClientShardInsertTrigger() {
		return "create trigger\n" + "insert_" + this.tName_cm_master + "_trigger\n" + "before insert on\n" + this.schema_cm + "." + this.tName_cm_master + "\n" + "for each row\n" + "execute procedure\n" + this.schema_cm + ".\"" + this.tName_cm_master + "_insert_trigger\"();";
	}
	
	public String getAllClients(long clientIndex) {
		long limit = clientIndex + 1024;
		String query = "";
		if (clientIndex > 0)
		{
			query = "select " + this.AllClientColumns + " from " + this.schema_cm + "." + this.tName_cm_master + " where " + "id >= " + clientIndex + "::bigint" + " and id < " + limit + "::bigint";
		}
		else
		{
			query = "select " + this.AllClientColumns + " from " + this.schema_cm + "." + this.tName_cm_master + " where id > 0 and id <= 1024::bigint";
		}
		
		return query;
	}
}
