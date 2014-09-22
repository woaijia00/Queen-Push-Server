/*******************************************************************************
 * Copyright 2013 Zhang Zhuo(william@TinyGameX.com).
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.tgx.queen.base.classic.task;

import com.tgx.queen.base.classic.task.inf.ITaskListener;


public abstract class AbstractListener
        implements
        ITaskListener
{
	private int bindSerial;
	
	@Override
	public void setBindSerial(int bindSerial) {
		if (this.bindSerial == 0) this.bindSerial = bindSerial;
	}
	
	@Override
	public int getBindSerial() {
		return bindSerial;
	}
	
	@Override
	public boolean isEnable() {
		return enabled;
	}
	
	protected boolean enabled;
	
	public AbstractListener() {
		enabled = true;
		setBindSerial(hashCode());
	}
}
