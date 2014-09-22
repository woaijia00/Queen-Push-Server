create table t_message (
	id 			bigserial primary key,
	g_uid 		char(64) not null unique,
	payload		varchar(4096) not null,
	create_t	bigint default 0,
	deliver_t 	bigint default 0,
	arrive_t 	bigint default 0,
	receive_t	bigint default 0,
	read_t		bigint default 0,
	origin		bigint default -1,
	target		bigint default -1,	
);

create table t_message_client_info(
	id			bigserial primary key,
	m_id		bigint foreign key references t_message(id),
	client		bigint default -1,
	deliver_t	bigint default 0,
	receive_t 	bigint default 0,
	read_t		bigint default 0,
);