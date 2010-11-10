//
// Groovy Liquibase ChangeLog
//
// Copyright (C) 2010 Tim Berglund
// http://augusttechgroup.com
// Littleton, CO
//
// Licensed under the GNU Lesser General Public License v2.1
//

package com.augusttechgroup.liquibase.delegate

import liquibase.change.core.AddColumnChange
import liquibase.change.core.RenameColumnChange
import liquibase.change.core.DropColumnChange
import liquibase.change.core.AlterSequenceChange
import liquibase.change.core.CreateTableChange
import liquibase.change.core.RenameTableChange
import liquibase.change.core.DropTableChange
import liquibase.change.core.CreateViewChange
import liquibase.change.core.RenameViewChange
import liquibase.change.core.DropViewChange
import liquibase.change.core.MergeColumnChange
import liquibase.change.core.CreateProcedureChange
import liquibase.change.core.AddLookupTableChange
import liquibase.change.core.AddNotNullConstraintChange
import liquibase.change.core.DropNotNullConstraintChange
import liquibase.change.core.AddUniqueConstraintChange
import liquibase.change.core.DropUniqueConstraintChange
import liquibase.change.core.CreateSequenceChange
import liquibase.change.core.DropSequenceChange
import liquibase.change.core.AddAutoIncrementChange
import liquibase.change.core.AddDefaultValueChange
import liquibase.change.core.DropDefaultValueChange
import liquibase.change.core.AddForeignKeyConstraintChange
import liquibase.change.core.DropForeignKeyConstraintChange
import liquibase.change.core.AddPrimaryKeyChange
import liquibase.change.core.DropPrimaryKeyChange
import liquibase.change.core.InsertDataChange
import liquibase.change.core.LoadDataColumnConfig
import liquibase.change.core.LoadDataChange
import liquibase.change.core.LoadUpdateDataChange
import liquibase.change.core.UpdateDataChange
import liquibase.change.core.TagDatabaseChange
import liquibase.change.core.StopChange
import liquibase.change.core.CreateIndexChange
import liquibase.change.core.DropIndexChange
import liquibase.change.core.RawSQLChange
import liquibase.change.core.SQLFileChange
import liquibase.change.core.ExecuteShellCommandChange
import liquibase.change.custom.CustomChangeWrapper


class ChangeSetDelegate {
  def changeSet


  void comment(String text) {
    changeSet.comments = text
  }


  void preConditions(Closure closure) {

  }


  //TODO Verify that this works. Don't fully understand addValidCheckSum() yet...
  void validCheckSum(String checksum) {
    println "ADDING ${checksum} to ${changeSet}"
    changeSet.addValidCheckSum(checksum)
  }


  void rollback(String sql) {
    changeSet.addRollBackSQL(sql)
  }


  void rollback(Closure closure) {
    changeSet.addRollBackSQL(closure.call().toString())
  }

  
  void rollback(Map params) {
    //TODO implement after changeSet processing is substantially implemented (testing requires it)
  }


  void addColumn(Map params, Closure closure) {
    def change = makeColumnarChangeFromMap(AddColumnChange, closure, params, ['schemaName', 'tableName'])
    changeSet.addChange(change)
  }


  void renameColumn(Map params) {
    addMapBasedChange(RenameColumnChange, params, ['schemaName', 'tableName', 'oldColumnName', 'newColumnName', 'columnDataType'])
  }


  void modifyColumn(Map params, Closure closure) {
    //TODO Figure out how the heck modifyColumn works.
  }


  void dropColumn(Map params) {
    addMapBasedChange(DropColumnChange, params, ['schemaName', 'tableName', 'columnName'])
  }


  void alterSequence(Map params) {
    addMapBasedChange(AlterSequenceChange, params, ['sequenceName', 'incrementBy'])
  }


  void createTable(Map params, Closure closure) {
    def change = makeColumnarChangeFromMap(CreateTableChange, closure, params, ['schemaName', 'tablespace', 'tableName', 'remarks'])
    changeSet.addChange(change)
  }


  void renameTable(Map params) {
    addMapBasedChange(RenameTableChange, params, ['schemaName', 'oldTableName', 'newTableName'])
  }


  void dropTable(Map params) {
    addMapBasedChange(DropTableChange, params, ['schemaName', 'tableName'])
  }
  

  void createView(Map params, Closure closure) {
    def change = makeChangeFromMap(CreateViewChange, params, ['schemaName', 'viewName', 'replaceIfExists'])
    change.selectQuery = closure.call()
    changeSet.addChange(change)
  }


  void renameView(Map params) {
    addMapBasedChange(RenameViewChange, params, ['schemaName', 'oldViewName', 'newViewName'])
  }


  void dropView(Map params) {
    addMapBasedChange(DropViewChange, params, ['schemaName', 'viewName'])
  }


  void mergeColumns(Map params) {
    addMapBasedChange(MergeColumnChange, params, ['schemaName', 'tableName', 'column1Name', 'column2Name', 'finalColumnName', 'finalColumnType', 'joinString'])
  }


  void createStoredProcedure(String storedProc) {
    def change = new CreateProcedureChange()
    change.procedureBody = storedProc
    changeSet.addChange(change)
  }

  
  void addLookupTable(Map params) {
    addMapBasedChange(AddLookupTableChange, params, ['existingTableName', 'existingTableSchemaName', 'existingColumnName', 'newTableName', 'newTableSchemaName', 'newColumnName', 'newColumnDataType', 'constraintName'])
  }


  void addNotNullConstraint(Map params) {
    addMapBasedChange(AddNotNullConstraintChange, params, ['schemaName', 'tableName', 'columnName', 'defaultNullValue', 'columnDataType'])
  }

  
  void dropNotNullConstraint(Map params) {
    addMapBasedChange(DropNotNullConstraintChange, params, ['schemaName', 'tableName', 'columnName', 'columnDataType'])
  }


  void addUniqueConstraint(Map params) {
    addMapBasedChange(AddUniqueConstraintChange, params, ['tablespace', 'schemaName', 'tableName', 'columnNames', 'constraintName'])
  }

  
  void dropUniqueConstraint(Map params) {
    addMapBasedChange(DropUniqueConstraintChange, params, ['tableName', 'schemaName', 'constraintName'])
  }


  void createSequence(Map params) {
    addMapBasedChange(CreateSequenceChange, params, ['sequenceName', 'schemaName', 'incrementBy', 'minValue', 'maxValue', 'ordered', 'startValue'])
  }


  void dropSequence(Map params) {
    addMapBasedChange(DropSequenceChange, params, ['sequenceName'])
  }
  

  void addAutoIncrement(Map params) {
    addMapBasedChange(AddAutoIncrementChange, params, ['tableName', 'columnName', 'columnDataType'])
  }


  void addDefaultValue(Map params) {
    addMapBasedChange(AddDefaultValueChange, params, ['tableName', 'schemaName', 'columnName', 'defaultValue', 'defaultValueNumeric', 'defaultValueBoolean', 'defaultValueDate'])
  }


  void dropDefaultValue(Map params) {
    addMapBasedChange(DropDefaultValueChange, params, ['tableName', 'schemaName', 'columnName'])
  }

  
  void addForeignKeyConstraint(Map params) {
    addMapBasedChange(AddForeignKeyConstraintChange, params, ['constraintName', 'baseTableName', 'baseTableSchemaName', 'baseColumnNames', 'referencedTableName', 'referencedTableSchemaName', 'referencedColumnNames', 'deferrable', 'initiallyDeferred', 'deleteCascade', 'onDelete', 'onUpdate'])
  }


  void dropForeignKeyConstraint(Map params) {
    addMapBasedChange(DropForeignKeyConstraintChange, params, ['constraintName', 'baseTableName', 'baseTableSchemaName'])
  }

  
  void addPrimaryKey(Map params) {
    addMapBasedChange(AddPrimaryKeyChange, params, ['tableName', 'schemaName', 'columnNames', 'constraintName', 'tablespace'])  
  }

  
  void dropPrimaryKey(Map params) {
    addMapBasedChange(DropPrimaryKeyChange, params, ['tableName', 'schemaName', 'constraintName']) 
  }

  
  void insert(Map params, Closure closure) {
    def change = makeColumnarChangeFromMap(InsertDataChange, closure, params, ['schemaName', 'tableName'])
    changeSet.addChange(change)
  }
  
  
  void loadData(Map params, Closure closure) {
    if(params.file instanceof File) {
      params.file = params.file.canonicalPath
    }

    def change = makeLoadDataColumnarChangeFromMap(LoadDataChange, closure, params, ['schemaName', 'tableName', 'file', 'encoding'])
    changeSet.addChange(change)
  }

  
  void loadUpdateData(Map params, Closure closure) {
    if(params.file instanceof File) {
      params.file = params.file.canonicalPath
    }

    def change = makeLoadDataColumnarChangeFromMap(LoadUpdateDataChange, closure, params, ['schemaName', 'tableName', 'file', 'encoding', 'primaryKey'])
    changeSet.addChange(change)
  }


  void update(Map params, Closure closure) {
    def change = makeColumnarChangeFromMap(UpdateDataChange, closure, params, ['schemaName', 'tableName'])
    changeSet.addChange(change)
  }


  void delete(Map params, Closure closure) {
    def change = makeColumnarChangeFromMap(UpdateDataChange, closure, params, ['schemaName', 'tableName'])
    changeSet.addChange(change)
  }

  
  void tagDatabase(Map params) {
    addMapBasedChange(TagDatabaseChange, params, ['tag']) 
  }


  void stop(String message) {
    def change = new StopChange()
    change.message = message
    changeSet.addChange(change)
  }

  
  void createIndex(Map params, Closure closure) {
    def change = makeColumnarChangeFromMap(CreateIndexChange, closure, params, ['schemaName', 'tableName', 'tablespace', 'indexName', 'unique'])
    changeSet.addChange(change)
  }

  
  void dropIndex(Map params) {
    addMapBasedChange(DropIndexChange, params, ['tableName', 'indexName'])  
  }
  

  void sql(Map params = [:], Closure closure) {
    def change = makeChangeFromMap(RawSQLChange, params, ['stripComments', 'splitStatements', 'endDelimiter'])
    change.sql = closure.call()
    changeSet.addChange(change)
  }


  void sql(String sql) {
    def change = new RawSQLChange()
    change.sql = sql
    changeSet.addChange(change)
  }

  
  void sqlFile(Map params) {
    addMapBasedChange(SQLFileChange, params, ['path', 'stripComments', 'splitStatements', 'encoding', 'endDelimiter'])
  }


  void customChange(Map params, Closure closure = null) {
    def change = new CustomChangeWrapper()
    change.classLoader = this.class.classLoader
    change.className = params['class']

    if(closure) {
      def delegate = new KeyValueDelegate()
      closure.delegate = delegate
      closure.call()
      delegate.map.each { key, value ->
        change.setParam(key, value.toString())
      }
    }

    changeSet.addChange(change)
  }


  /**
   * A Groovy-specific extension that allows a closure to be provided,
   * implementing the change. The closure is passed the instance of
   * Database.
   */
  void customChange(Closure closure) {
    
  }

  
  void executeCommand(Map params) {
    addMapBasedChange(ExecuteShellCommandChange, params, ['executable', 'os'])
  }


  void executeCommand(Map params, Closure closure) {
    def change = makeChangeFromMap(ExecuteShellCommandChange, params, ['executable', 'os'])
    def delegate = new ArgumentDelegate()
    closure.delegate = delegate
    closure.call()
    delegate.args.each { arg ->
      change.addArg(arg)
    }

    changeSet.addChange(change)
  }


  def makeLoadDataColumnarChangeFromMap(Class klass, Closure closure, Map params, List paramNames) {
    def change = makeChangeFromMap(klass, params, paramNames)

    def columnDelegate = new ColumnDelegate(columnConfigClass: LoadDataColumnConfig)
    closure.delegate = columnDelegate
    closure.call()

    columnDelegate.columns.each { column ->
      change.addColumn(column)
    }

    return change
  }


  def makeColumnarChangeFromMap(Class klass, Closure closure, Map params, List paramNames) {
    def change = makeChangeFromMap(klass, params, paramNames)

    def columnDelegate = new ColumnDelegate()
    closure.delegate = columnDelegate
    closure.call()

    columnDelegate.columns.each { column ->
      change.addColumn(column)
    }

    // It is a bit sloppy to do this here from a coherence standpoint, but where clauses mostly
    // only get used when we are dealing with columns
    if(columnDelegate.whereClause != null) {
      change.whereClause = columnDelegate.whereClause
    }

    return change
  }

  
  private def makeChangeFromMap(Class klass, Map sourceMap, List paramNames) {
    def change = klass.newInstance()
    paramNames.each { name ->
      if(sourceMap[name] != null) {
        change[name] = sourceMap[name]
      }
    }

    return change
  }


  private void addMapBasedChange(Class klass, Map sourceMap, List paramNames) {
    changeSet.addChange(makeChangeFromMap(klass, sourceMap, paramNames))
  }


}
