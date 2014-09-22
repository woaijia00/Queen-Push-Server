package com.tgx.queen.push.inf;

import java.util.List;

import com.tgx.queen.push.bean.Message;


public interface BroadCastable
{
	
	public long originUsr();
	
	public List<Message> mkTarMsg(List<Message> event_msg_list);
}
