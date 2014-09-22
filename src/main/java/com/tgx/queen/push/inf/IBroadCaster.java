package com.tgx.queen.push.inf;

import java.util.List;

import com.tgx.queen.push.bean.Message;


public interface IBroadCaster
{
	public List<Message> dipatchMsg(List<Message> list, BroadCastable broadCastMsg);
}
