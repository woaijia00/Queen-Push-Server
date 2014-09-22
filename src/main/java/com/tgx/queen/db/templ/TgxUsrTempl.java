package com.tgx.queen.db.templ;

import com.tgx.queen.db.jdbc.JDBCUtils;


public class TgxUsrTempl
{
	
	private String             schema_im;
	private String             tName_im_master;
	
	private static TgxUsrTempl _instance;
	
	final String               AllUsrColumns = "id,im_usr_id,extract(epoch from c_time) as \"c_time\"," + "extract(epoch from m_time) as \"m_time\",c_bind,bind_type";
	
	public TgxUsrTempl() {
		this.schema_im = JDBCUtils.getInstance().schema_im;
		this.tName_im_master = JDBCUtils.getInstance().tName_im_master;
	}
	
	public static TgxUsrTempl getInstance() {
		return _instance == null ? _instance = new TgxUsrTempl() : _instance;
	}
	
	public String getImUsrByIndex(long usr_index) {
		return "select " + this.AllUsrColumns + " from " + this.schema_im + "." + this.tName_im_master + " where " + "id = " + usr_index + "::bigint";
	}
	
	public String getImUsrAll(long offset) {
		String query;
		long limit = offset + 1024;
		if (offset > 0)
		{
			query = "select " + this.AllUsrColumns + " from " + this.schema_im + "." + this.tName_im_master + " where " + "id >= " + offset + "::bigint" + " and id < " + limit + "::bigint";
		}
		else
		{
			query = "select " + this.AllUsrColumns + " from " + this.schema_im + "." + this.tName_im_master + " where id > 0 and id <= 1024::bigint";
		}
		return query;
	}
	
	public String insertUsr(String im_usr_id) {
		return "insert into " + this.schema_im + "." + this.tName_im_master + " (im_usr_id) values('" + im_usr_id + "')";
	}
	
	public String deleteUsr(long usr_index) {
		return "delete from " + this.schema_im + "." + this.tName_im_master + " where id= " + usr_index + "::bigint";
	}
	
	public String updateUsr(long id, long[] c_bind) {
		String bindSequence = "{";
		for (int i = 0; i < c_bind.length - 1; i++)
			bindSequence += c_bind[i] + ",";
		bindSequence += c_bind[c_bind.length - 1] + "}";
		String query = "update " + this.schema_im + "." + this.tName_im_master + " set c_bind='" + bindSequence + "', m_time=now() " + "where id='" + id + "'::bigint";
		return query;
	}
	
	public String createUsrSchema() {
		return "create schema if not exists " + this.schema_im;
	}
	
	public String createUsrTable() {
		return "create table if not exists " + this.schema_im + "." + this.tName_im_master + "(" + "id			bigserial 	primary key," + "im_usr_id 	char(64) 	not null," + "c_time 		timestamp 	not null default now()," + "m_time 		timestamp 	not null default now()," + "c_bind		bigint[] 	not null default '{-1}'," + "bind_type	smallint	not null default 0" + ");";
	}
	
	public String createUsrSubShardTable(long share_id, long low, long high) {
		return "create table if not exists " + this.schema_im + "." + this.tName_im_master + "_" + share_id + "(" + "check ( id >= " + low + "::bigint and id < " + high + "::bigint )" + ") " + "inherits" + " ( " + this.schema_im + "." + this.tName_im_master + " );";
	}
	
	public String createUsrShardInsertTriggerFun(long shard_size) {
		return "create or replace function\n " + this.schema_im + "." + "\"" + this.tName_im_master + "_insert_trigger\"()\n " + "returns trigger as $body$\n " + "declare shard_id bigint;\n " + "sub_table text;\n " + "begin\n " + "select new.id / " + shard_size + " into shard_id;\n " + "sub_table:='" + this.tName_im_master + "_'||shard_id;\n " + "execute 'insert into " + this.schema_im + ".' || quote_ident(sub_table) || ' values (($1).*)' using new;\n " + "return null;\n " + "end;\n " + "$body$\n " + "language plpgsql;";
	}
	
	public String createUsrShardInsertTrigger() {
		return "create trigger insert_" + this.tName_im_master + "_trigger before insert on " + this.schema_im + "." + this.tName_im_master + " for each row execute procedure " + this.schema_im + "." + "\"" + this.tName_im_master + "_insert_trigger\"();";
	}
	
	public String countUsrShardSubTable() {
		return "select " + "count(*) " + "from   " + "pg_catalog.pg_class c " + "join   " + "pg_catalog.pg_namespace n on n.oid = c.relnamespace " + "where  " + "c.relkind = 'r' " + "and    " + "c.relname like '%" + this.tName_im_master + "%' " + "and    " + "n.nspname = '" + this.schema_im + "';";
	}
	
	public String countUsrId() {
		return "select " + "last_value " + "from " + this.schema_im + "." + this.tName_im_master + "_id_seq;";
	}
	
	public String selectKey() {
		return "select currval('" + this.schema_im + "." + this.tName_im_master + "_id_seq')";
	}
}
