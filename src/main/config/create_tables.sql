drop table if exists t_cm_hash_global cascade;
create table if not exists t_cm_hash_global(
	id 		    bigserial,
	hash		char(64) not null primary key
);
SET constraint_exclusion = on;
drop table if exists t_cm_hash_global_0 cascade;
create table if not exists t_cm_hash_global_0 ( check (id >= 0 and id< 10000::bigint) ) inherits (t_cm_hash_global);

drop rule if exists cm_hash_insert_0 on t_cm_hash_global;
create rule cm_hash_insert_0 as on insert to t_cm_hash_global where (id >= 0 and id < 1000000::bigint) do instead insert into t_cm_hash_global_0 values (new.id,new.hash); 


explain select count(*) from t_cm_hash_global;