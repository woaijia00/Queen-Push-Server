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
package com.tgx.queen.base.classic.task.inf;

import com.tgx.queen.base.classic.task.TaskService;


public interface ITaskListener
{
	/**
	 * @param taskOrResult
	 * @param service
	 * @return true:将停止继续分发
	 */
	public boolean handleResult(ITaskResult taskOrResult, TaskService service);
	
	/**
	 * task 将携带错误信息，task内含的资源需要在此处进行手工释放
	 * 
	 * @param task
	 * @param service
	 * @return true:将停止继续分发
	 */
	public boolean exCaught(ITaskResult task, TaskService service);
	
	/**
	 * 当前Listener是否处于可用状态,此状态的由具体实现决定
	 * 
	 * @return
	 */
	public boolean isEnable();
	
	/**
	 * 设置当前Listener的串号,为进行分发确定唯一标识
	 * 
	 * @param bindSerial
	 */
	public void setBindSerial(int bindSerial);
	
	public int getBindSerial();
}
