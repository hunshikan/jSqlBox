/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdbpro.handler;

import org.apache.commons.dbutils.QueryRunner;

/**
 * AroundSqlExecute should have a handleSql method to translate SQL and a
 * handleResult method to translate Result
 * 
 * @since 1.7.0.2
 */
@SuppressWarnings({ "all" })
public interface AroundSqlExecute {

	/**
	 * handleSql method used to change SQL string before execute, to add extra
	 * features like pagination, logging, entityNet SQl explanation...
	 * 
	 * @param query
	 *            The QueryRunner instance
	 * @param sql
	 *            The original SQL
	 * @param params
	 *            Parameters if have
	 * @return The changed SQL String
	 */
	public String handleSql(QueryRunner query, String sql, Object... params);

	/**
	 * After a SQL be executed, handleResult method be called to change the result
	 * 
	 * @param result
	 *            The original result Object
	 * @return The changed result Object
	 */
	public Object handleResult(QueryRunner query, Object result);
}