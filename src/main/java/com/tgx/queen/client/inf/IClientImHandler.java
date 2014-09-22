/**
 * -
 * Copyright (c) 2013 Zhang Zhuo
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote
 * products derived from this software without specific written permission.
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.tgx.queen.client.inf;

import com.tgx.queen.io.bean.protocol.impl.X21_VerifyMsgArrived;
import com.tgx.queen.io.bean.protocol.impl.X23_RouteMsg;
import com.tgx.queen.io.bean.protocol.impl.X24_ConfirmRouteArrived;
import com.tgx.queen.io.bean.protocol.impl.X25_VerfiyRouteConfirm;
import com.tgx.queen.io.bean.protocol.impl.X27_RSyncMsgStatus;


public interface IClientImHandler
        extends
        IClientCmdHandler
{
	public void x21Logic(X21_VerifyMsgArrived x21);
	
	public void x23Logic(X23_RouteMsg x23, X24_ConfirmRouteArrived x24);
	
	public void x25Logic(X25_VerfiyRouteConfirm x25);
	
	public void x27Logic(X27_RSyncMsgStatus x27);
}
