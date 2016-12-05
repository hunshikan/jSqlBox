/**
* Copyright (C) 2016 Yong Zhu.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.drinkjava2.jsqlbox.jpa;

import java.sql.Connection;

import javax.sql.DataSource;

import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyJdbc;

/**
 * Define a table ID generator, implements of JPA TableGenerator
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class TableGenerator implements IdGenerator {
	/**
	 * (Required) A unique generator name that can be referenced by one or more classes to be the generator
	 */
	private String name = "";

	/**
	 * Name of table that stores the generated id values. Defaults to a name chosen by persistence provider.
	 */
	private String table = "";

	/**
	 * Name of the primary key column in the table. Defaults to a provider-chosen name.
	 */
	private String pkColumnName = "";

	/**
	 * Name of the column that stores the last value generated. Defaults to a provider-chosen name.
	 */
	private String valueColumnName = "";

	/**
	 * The primary key value in the generator table that distinguishes this set of generated values from others that may
	 * be stored in the table.
	 */
	private String pkColumnValue = "";

	/**
	 * The initial value to be used to initialize the column that stores the last value generated.
	 */
	private int initialValue = 0;

	/**
	 * The amount to increment by when allocating id numbers from the generator.
	 */
	private int allocationSize = 50;

	/**
	 * Last ID Value
	 */
	private int lastValue = -1;

	public TableGenerator(String name, String table, String pkColumnName, String pkColumnValue, String valueColumnName,
			int initialValue, int allocationSize) {
		this.name = name;
		this.table = table;
		this.pkColumnName = pkColumnName;
		this.valueColumnName = valueColumnName;
		this.pkColumnValue = pkColumnValue;
		this.initialValue = initialValue;
		this.allocationSize = allocationSize;
	}

	public boolean ifEqual(String name, String table, String pkColumnName, String pkColumnValue, String valueColumnName,
			int initialValue, int allocationSize) {
		return this.name.equals(name) && this.table.equals(table) && this.pkColumnName.equals(pkColumnName)// NOSONAR
				&& this.valueColumnName.equals(valueColumnName) && this.pkColumnValue.equals(pkColumnValue)
				&& this.initialValue == initialValue && this.allocationSize == allocationSize;
	}

	/**
	 * Get the next Table Generator ID
	 */
	@Override
	public Object getNextID(DataSource ds) {
		if (lastValue == -1) {
			int countOfRec = TinyJdbc.queryForInteger(ds, Connection.TRANSACTION_READ_COMMITTED,
					"select count(*) from " + table + " where " + pkColumnName + "=?", pkColumnValue);// NOSONAR
			if (countOfRec == 0) {
				TinyJdbc.execute(ds, Connection.TRANSACTION_READ_COMMITTED,
						"insert into " + table + "( " + pkColumnName + "," + valueColumnName + " )  values(?,?)",
						pkColumnValue, initialValue);
				lastValue = initialValue;
				return lastValue;
			} else {
				int last = TinyJdbc.queryForInteger(ds, 2,
						"select " + valueColumnName + " from " + table + " where " + pkColumnName + "=?",
						pkColumnValue);// 70 or 99 or 100 or 101
				last = calculateBucketFirstID(last, allocationSize);// 101 or 101 or 101 or 151
				TinyJdbc.execute(ds, 2,
						"update " + table + " set " + valueColumnName + "=? where " + pkColumnName + " =?",
						calculateBucketFirstID(last + 1, allocationSize), pkColumnValue);// 151, 151, 151, 201
				lastValue = last;
				return lastValue;
			}
		} else {
			int last = lastValue;
			int nextBucketFirstID = calculateBucketFirstID(last, allocationSize);
			if (last + 1 >= nextBucketFirstID)
				TinyJdbc.execute(ds, 2,
						"update " + table + " set " + valueColumnName + "=? where " + pkColumnName + " =?",
						calculateBucketFirstID(last + 1, allocationSize), pkColumnValue);
			lastValue = last + 1;
			return lastValue;
		}
	}

	private static int calculateBucketFirstID(int currentValue, int allocationSize) {
		return ((currentValue + allocationSize - 1) / allocationSize) * allocationSize + 1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getPkColumnName() {
		return pkColumnName;
	}

	public void setPkColumnName(String pkColumnName) {
		this.pkColumnName = pkColumnName;
	}

	public String getPkColumnValue() {
		return pkColumnValue;
	}

	public void setPkColumnValue(String pkColumnValue) {
		this.pkColumnValue = pkColumnValue;
	}

	public String getValueColumnName() {
		return valueColumnName;
	}

	public void setValueColumnName(String valueColumnName) {
		this.valueColumnName = valueColumnName;
	}

	public int getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(int initialValue) {
		this.initialValue = initialValue;
	}

	public int getAllocationSize() {
		return allocationSize;
	}

	public void setAllocationSize(int allocationSize) {
		this.allocationSize = allocationSize;
	}

	public int getLastValue() {
		return lastValue;
	}

	public void setLastValue(int lastValue) {
		this.lastValue = lastValue;
	}

}