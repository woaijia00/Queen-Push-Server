package com.tgx.queen.push;

import java.util.List;

import com.tgx.queen.push.bean.Message;
import com.tgx.queen.push.inf.BroadCastable;
import com.tgx.queen.push.inf.IBroadCaster;


public class PushRouter
        implements
        IBroadCaster
{
	@Override
	public List<Message> dipatchMsg(List<Message> list, BroadCastable broadCastMsg) {
		return broadCastMsg.mkTarMsg(list);
	}
}
