/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Net is an entity net created by queryNet method
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNet {
	private SqlBoxContext ctx; 
	 
	private List<SqlBox> configBoxes = new ArrayList<SqlBox>();

	/** The net */
	private Map<Class<?>, List<Object>> net; 

 

	public EntityNet(SqlBoxContext ctx, Class<?>... entityClasses) {
		this.ctx = ctx;
	}
	
	public EntityNet(SqlBoxContext ctx, SqlBox... sqlBoxes) {
		this.ctx = ctx;
	}

}
