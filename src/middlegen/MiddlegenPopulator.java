/*
 * Copyright (c) 2001, Aslak HellesÃ¸y, BEKK Consulting
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of BEKK Consulting nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package middlegen;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import middlegen.javax.Sql2Java;
import middlegen.plugins.iwallet.config.IWalletConfigException;
import middlegen.plugins.iwallet.util.DalUtil;

/**
 * @author <a href="mailto:aslak.hellesoy@bekk.no">Aslak Hellesøy</a>
 * @created 3. oktober 2001
 */
public class MiddlegenPopulator {

    /**
     * @todo-javadoc Describe the column
     */
    private Database                         _database;

    /**
     * @todo-javadoc Describe the column
     */
    private DatabaseMetaData                 _metaData;

    /**
     * @todo-javadoc Describe the column
     */
    private Middlegen                        _middlegen;

    /**
     * @todo-javadoc Describe the column
     */
    private String                           _schema;
    /**
     * @todo-javadoc Describe the column
     */
    private String                           _catalog;

    /**
     * @todo-javadoc Describe the column
     */
    private String[]                         _types           = null;

    /**
     * Maps Many2ManyElement.getOrderedNameWithoutJoinTable() to a Collection of
     * Many2ManyElement. The purpose is to group m:n relations that have the same
     * extremities, but different join tables.
     */
    private final Map                        _many2many;

    /**
     * @todo-javadoc Describe the field
     */
    private Connection                       _connection;

    /**
     * @todo-javadoc Describe the field
     */
    private String                           _sortColumns;

    /**
     * @todo-javadoc Describe the field
     */
    private final Collection                 EMPTY_COLLECTION = new ArrayList(0);

    /** Get static reference to Log4J Logger */
    private static org.apache.log4j.Category _log             = org.apache.log4j.Category
                                                                  .getInstance(MiddlegenPopulator.class
                                                                      .getName());

    /**
     * Describe what the SchemaFactory constructor does
     *
     * @todo-javadoc Write javadocs for exception
     * @param middlegen Describe what the parameter does
     * @param database Describe what the parameter does
     * @param schema Describe what the parameter does
     * @param catalog Describe what the parameter does
     * @param many2many Describe what the parameter does
     * @param sortColumns Sort the columns in the table. Coma separated list of
     *      criteria for sorting columns. Valid values are pk, fk, indexed,
     *      nullable, mandatory, unique, name (always with the lowset priory).
     *      Example: pk,indexed,name
     * @throws MiddlegenException Describe the exception
     */
    public MiddlegenPopulator(Middlegen middlegen, Database database, String schema,
                              String catalog, Map many2many, String sortColumns)
                                                                                throws MiddlegenException {
        _middlegen = middlegen;
        _database = database;
        _schema = Util.ensureNotNull(schema);
        _catalog = catalog;
        _many2many = many2many;
        _sortColumns = (sortColumns == null) ? null : sortColumns.toLowerCase();
        try {
            tune(getConnection().getMetaData());
        } catch (SQLException e) {
            throw new MiddlegenException("Couldn't tune database:" + e.getMessage());
        }
    }

    /**
     * Adds regular tables to middlegen's list of tables to process.
     *
     * @todo-javadoc Write javadocs for exception
     * @exception MiddlegenException Describe the exception
     */
    public void addRegularTableElements() throws MiddlegenException {
        ResultSet tableRs = null;
        try {
            tableRs = getMetaData().getTables(_catalog, _schema, null, _types);
            while (tableRs.next()) {
                String tableName = tableRs.getString("TABLE_NAME");
                String tableType = tableRs.getString("TABLE_TYPE");
                String schemaName = tableRs.getString("TABLE_SCHEM");
                String ownerSinonimo = null;
                if ("TABLE".equals(tableType)
                    || (_middlegen.getMiddlegenTask().isIncludeViews() && "VIEW".equals(tableType))
                    || ("SYNONYM".equals(tableType) && isOracle())) {
                    // it's a regular table or a synonym
                    _log.debug("schema:" + _schema + "," + schemaName);
                    _log.debug("table:" + tableName);
                    TableElement tableElement = new TableElement();
                    tableElement.setName(tableName);
                    if ("SYNONYM".equals(tableType) && isOracle()) {
                        ownerSinonimo = getSynonymOwner(tableName);
                        if (ownerSinonimo != null) {
                            tableElement.setOwnerSynonymName(ownerSinonimo);
                        }
                    }
                    _middlegen.addTableElement(tableElement);
                } else {
                    _log.debug("Ignoring table " + tableName + " of type " + tableType);
                }
            }
            if (_middlegen.getTableElements().isEmpty()) {
                String databaseStructure = getDatabaseStructure();
                throw new MiddlegenException(
                    "Middlegen successfully connected to the database, but "
                            + "couldn't find any tables. Perhaps the specified schema or catalog is wrong? -Or maybe "
                            + "there aren't any tables in the database at all?" + databaseStructure);
            }
        } catch (SQLException e) {
            // schemaRs and catalogRs are only used for error reporting if we get an exception
            String databaseStructure = getDatabaseStructure();
            _log.error(e.getMessage(), e);
            throw new MiddlegenException(
                "Couldn't get list of tables from database. Probably a JDBC driver problem."
                        + databaseStructure);
        } finally {
            try {
                tableRs.close();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Describe what the method does
     *
     * @todo-javadoc Write javadocs for method
     */
    public void closeConnection() {
        try {
            if (_connection != null) {
                _connection.close();
            }
        } catch (SQLException ignore) {
        }
    }

    /**
     * @todo figure out how to pick driver at runtime and use it. classloader
     *      problem. for now, must be on cp.
     * @todo do smarter primkey guessing. Have a way to mark columns/primkeys as
     *      "guessed"
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Write javadocs for return value
     * @todo-javadoc Write javadocs for exception
     * @param wantedTables Describe what the parameter does
     * @exception MiddlegenException Describe the exception
     */
    public void populate(Map wantedTables) throws MiddlegenException {
        try {
            addTables(wantedTables);

            for (Iterator tableIterator = _middlegen.getTables().iterator(); tableIterator
                .hasNext();) {
                DbTable table = (DbTable) tableIterator.next();
                addColumns(table);
            }

            for (Iterator tableIterator = _middlegen.getTables().iterator(); tableIterator
                .hasNext();) {
                DbTable table = (DbTable) tableIterator.next();
                addRelations(table, wantedTables);
            }

            // warn if there are no relations
            if (_middlegen.getRelations().size() == 0) {
                _log
                    .warn("WARNING: Middlegen couldn't find any relations between any tables. "
                          + "This may be intentional from the design of the database, but it may "
                          + "also be because you have incorrectly defined the relationships. "
                          + "It could also be because the JDBC driver you're using doesn't correctly implement DatabaseMetaData. "
                          + "See the samples (for an example on how to define relationships) "
                          + "and verify that your driver correctly implements DatabaseMetaData.");
            }

            markFksToUnwantedTables();
            addMany2ManyRelations();
            warnUnidentifiedM2ms();
            getConnection().close();

            if (_sortColumns != null) {
                Comparator comparator = new ColumnComparator(_sortColumns);
                for (Iterator tableIterator = _middlegen.getTables().iterator(); tableIterator
                    .hasNext();) {
                    DbTable table = (DbTable) tableIterator.next();
                    table.sortColumns(comparator);
                }
            }

        } catch (SQLException e) {
            _log.error(e.getMessage(), e);
            throw new MiddlegenException("Database problem:" + e.getMessage());
        }
    }

    /**
     * 
     * <p>add by zhaoxu 20060918</p>
     * 
     * @param wantedTables
     * @param tabs
     * @throws MiddlegenException
     */
    public void populate(Map wantedTables, String tabs) throws MiddlegenException {
        try {
            addTables(wantedTables);

            for (Iterator tableIterator = _middlegen.getTables().iterator(); tableIterator
                .hasNext();) {
                DbTable table = (DbTable) tableIterator.next();

                if (DalUtil.inTabs(table.getName())) {
                    addColumns(table);
                }
            }

            for (Iterator tableIterator = _middlegen.getTables().iterator(); tableIterator
                .hasNext();) {
                DbTable table = (DbTable) tableIterator.next();
                if (DalUtil.inTabs(table.getName())) {
                    addRelations(table, wantedTables);
                }
            }

            // warn if there are no relations
            if (_middlegen.getRelations().size() == 0) {
                _log
                    .warn("WARNING: Middlegen couldn't find any relations between any tables. "
                          + "This may be intentional from the design of the database, but it may "
                          + "also be because you have incorrectly defined the relationships. "
                          + "It could also be because the JDBC driver you're using doesn't correctly implement DatabaseMetaData. "
                          + "See the samples (for an example on how to define relationships) "
                          + "and verify that your driver correctly implements DatabaseMetaData.");
            }

            markFksToUnwantedTables();
            addMany2ManyRelations();
            warnUnidentifiedM2ms();
            getConnection().close();

            if (_sortColumns != null) {
                Comparator comparator = new ColumnComparator(_sortColumns);
                for (Iterator tableIterator = _middlegen.getTables().iterator(); tableIterator
                    .hasNext();) {
                    DbTable table = (DbTable) tableIterator.next();
                    table.sortColumns(comparator);
                }
            }

        } catch (SQLException e) {
            _log.error(e.getMessage(), e);
            throw new MiddlegenException("Database problem:" + e.getMessage());
        } catch (IWalletConfigException e) {
            _log.error(e.getMessage());
            throw new MiddlegenException(e.getMessage());
        }
    }

    /**
     * Establishes relations between tables. This is done by first quering what
     * tables are related (for the sake of speed), then by querying how they are
     * related. If there are more than one relation between two tables, the
     * relations must be given different names. In that case, the relation names
     * will be suffixed with "By(fk0)[And()fki]*" <pre>
     *
     *  pk       fk    relation
     * table    table   name
     *
     * (1)
     * +---+    +---+
     * | P |    | Q |
     * +---+    +---+
     * |*a |----| m | q
     * +---+    +---+
     *
     * (2)
     * +---+    +---+
     * | R |    | S |
     * +---+    +---+
     * |*a |----| m | sByM (Illegal in WLS. Relatiions must map to full pk)
     * |*b |----| n | sByN (Illegal in WLS. Relatiions must map to full pk)
     * +---+    +---+
     *
     * (3)
     * +---+    +---+
     * | T |    | U |
     * +---+    +---+
     * |   | ___| m | uByM
     * |*b |<___| n | uByN
     * +---+    +---+
     *
     * (4)
     * +---+    +---+
     * | V |    | W |
     * +---+    +---+
     * |*a |\__/| m | w
     * |*b |/  \| n |
     * +---+    +---+
     *
     * (5)
     * +---+    +---+
     * | X |    | Y |
     * +---+    +---+
     * |*a |\__/| m | yByMAndN
     * |*b |/| \| n |
     * |   | L_/| o | yByOAndP
     * |   |   \| p |
     * +---+    +---+
     * </pre>
     *
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Write javadocs for exception
     * @todo-javadoc Write javadocs for exception
     * @param pkTable Describe the method parameter
     * @param wantedTables a Collection of TableElements representing the names
     *      of the tables for which we want to establish relationships.
     * @exception MiddlegenException if Middlegen can't determine relationships
     *      because of a logical error
     * @exception SQLException if a database exception occurs
     */
    protected void addRelations(DbTable pkTable, Map wantedTables) throws MiddlegenException,
                                                                  SQLException {
        // Maps table to a new Map. That map maps fk name to collection of ColumnMap
        Map fkTables = new HashMap();

        //		Map fkNameToColumnMapsMap = new HashMap();
        short bogusFkName = 0;

        // first get all the relationships dictated by the database schema

        ResultSet exportedKeyRs = null;
        if (pkTable.getTableElement().getOwnerSynonymName() != null) {
            exportedKeyRs = getMetaData().getExportedKeys(_catalog,
                pkTable.getTableElement().getOwnerSynonymName(), pkTable.getSqlName());
        } else {
            exportedKeyRs = getMetaData().getExportedKeys(_catalog, _schema, pkTable.getSqlName());
        }
        while (exportedKeyRs.next()) {
            String fkTableName = exportedKeyRs.getString("FKTABLE_NAME");
            String fkColumnName = exportedKeyRs.getString("FKCOLUMN_NAME");

            // Let's see if that is one of the tables we're supposed to generate for
            if (isWantedTable(wantedTables, fkTableName)) {

                String pkColumnName = exportedKeyRs.getString("PKCOLUMN_NAME");
                String fkName = exportedKeyRs.getString("FK_NAME");
                short keySeq = exportedKeyRs.getShort("KEY_SEQ");
                // Warn if there is a relation to a column which is not a pk

                if (keySeq == 0) {
                    bogusFkName++;
                }
                if (fkName == null) {
                    fkName = String.valueOf(bogusFkName);
                }

                addCrossref(pkTable, pkColumnName, fkTableName, fkColumnName, fkName, fkTables);
            } else {
                _log.info("Found a relation between " + pkTable.getSqlName() + " and "
                          + fkTableName + ". Skipping it since " + fkTableName
                          + " isn't among the specified tables.");
            }
        }
        exportedKeyRs.close();

        // Now get crossrefs that were declared explicitly (not in the DB).
        for (Iterator crossrefs = pkTable.getTableElement().getCrossrefs().iterator(); crossrefs
            .hasNext();) {
            CrossrefElement crossref = (CrossrefElement) crossrefs.next();

            if (wantedTables.containsKey(crossref.getFktable())) {
                String fkName = crossref.getName();
                if (fkName == null) {
                    bogusFkName++;
                    fkName = String.valueOf(bogusFkName);
                }
                String pkColumnName = crossref.getPkcolumn();
                if (pkColumnName == null) {
                    // If it wasn't specified, get it from the DbTable.
                    Column pkColumn = pkTable.getPkColumn();
                    if (pkColumn != null) {
                        pkColumnName = pkColumn.getSqlName();
                    } else {
                        throw new MiddlegenException(
                            "In custom defined crossref, the table "
                                    + pkTable.getSqlName()
                                    + " doesn't have a single-column primary key. You must therefore explicitly "
                                    + "declare pkcolumn=\"something\" in the crossref element.");
                    }
                }
                addCrossref(pkTable, pkColumnName, crossref.getFktable(), crossref.getFkcolumn(),
                    fkName, fkTables);
            } else {
                _log.info("The declared relation between " + pkTable.getSqlName() + " and "
                          + crossref.getFktable() + " will be skipped since "
                          + crossref.getFktable() + " isn't among the specified tables.");
            }
        }

        // Now create relations.
        for (Iterator fkTableIterator = fkTables.keySet().iterator(); fkTableIterator.hasNext();) {
            DbTable fkTable = (DbTable) fkTableIterator.next();

            Map fkNameToColumnMapsMap = (Map) fkTables.get(fkTable);
            for (Iterator fkIterator = fkNameToColumnMapsMap.keySet().iterator(); fkIterator
                .hasNext();) {
                String fkName = (String) fkIterator.next();
                Collection columnMaps = (Collection) fkNameToColumnMapsMap.get(fkName);
                // Warn if the number of column maps is inferior to the number of PKs
                if (columnMaps.size() < pkTable.getPrimaryKeyColumns().size()) {
                    _log.warn("WARNING: There is a relation between " + fkTable.getSqlName()
                              + " and " + pkTable.getSqlName() + " that doesn't include all the "
                              + " primary key columns. This may cause errors later on.");
                }

                ColumnMap[] columnMapArray = new ColumnMap[columnMaps.size()];
                columnMapArray = (ColumnMap[]) columnMaps.toArray(columnMapArray);

                // If there is more than one relation, we'll use the fk name(s)
                // as suffix for the relation name
                String relationSuffix = "";
                String fkRoleSuffix = "";
                if (fkNameToColumnMapsMap.size() > 1) {
                    relationSuffix = "-";
                    fkRoleSuffix += "_by_";
                    for (int i = 0; i < columnMapArray.length; i++) {
                        if (i >= 1) {
                            relationSuffix += "-";
                            fkRoleSuffix += "_and_";
                        }
                        relationSuffix += columnMapArray[i].getForeignKey().toLowerCase();
                        fkRoleSuffix += columnMapArray[i].getForeignKey();
                    }
                }
                fkRoleSuffix = DbNameConverter.getInstance().columnNameToVariableName(fkRoleSuffix);
                _log.debug("relationSuffix:" + relationSuffix + " (" + fkRoleSuffix + ")");
                Relation relation = new Relation(pkTable, columnMapArray, fkTable,
                    new ColumnMap[0], null, relationSuffix, fkRoleSuffix);
                _middlegen.addRelation(relation);

            }
        }

        // The same table might be referenced more than once.
        // This has an impact on the relation's getter suffix
        // Therefore, we store all column maps in a List
    }

    /**
     * Gets the SchemaName attribute of the MiddlegenPopulator object
     *
     * @return The SchemaName value
     */
    String getSchemaName() {
        return _schema;
    }

    /**
     * Returns true if the table is in the list of wanted tables. Uses a
     * case-insensitive search
     *
     * @param wantedTables The lsit of wanted tables
     * @param tableName The table name
     * @return true if the table is in the list of wanted tables.
     */
    private boolean isWantedTable(Map wantedTables, String tableName) {
        if (wantedTables.containsKey(tableName)) {
            return true;
        }
        for (Iterator i = wantedTables.keySet().iterator(); i.hasNext();) {
            String wantedTableName = (String) i.next();
            if (wantedTableName.toLowerCase().equals(tableName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the DatabaseStructure attribute of the MiddlegenPopulator object
     *
     * @todo-javadoc Write javadocs for exception
     * @return The DatabaseStructure value
     * @exception MiddlegenException Describe the exception
     */
    private String getDatabaseStructure() throws MiddlegenException {
        ResultSet schemaRs = null;
        ResultSet catalogRs = null;
        String nl = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer(nl);
        // Let's give the user some feedback. The exception
        // is probably related to incorrect schema configuration.
        sb.append("Configured schema:").append(_schema).append(nl);
        sb.append("Configured catalog:").append(_catalog).append(nl);

        try {
            schemaRs = getMetaData().getSchemas();
            sb.append("Available schemas:").append(nl);
            while (schemaRs.next()) {
                sb.append("  ").append(schemaRs.getString("TABLE_SCHEM")).append(nl);
            }
        } catch (SQLException e2) {
            _log.warn("Couldn't get schemas", e2);
            sb.append("  ?? Couldn't get schemas ??").append(nl);
        } finally {
            try {
                schemaRs.close();
            } catch (Exception ignore) {
            }
        }

        try {
            catalogRs = getMetaData().getCatalogs();
            sb.append("Available catalogs:").append(nl);
            while (catalogRs.next()) {
                sb.append("  ").append(catalogRs.getString("TABLE_CAT")).append(nl);
            }
        } catch (SQLException e2) {
            _log.warn("Couldn't get catalogs", e2);
            sb.append("  ?? Couldn't get catalogs ??").append(nl);
        } finally {
            try {
                catalogRs.close();
            } catch (Exception ignore) {
            }
        }
        return sb.toString();
    }

    /**
     * @todo-javadoc Write javadocs for exception
     * @return a list of tables found in the database
     * @throws MiddlegenException Describe the exception
     */
    private String getDatabaseTables() throws MiddlegenException {
        String nl = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer(nl);
        sb.append("Found the following tables:");
        sb.append(nl);

        ResultSet tableRs = null;
        try {
            tableRs = getMetaData().getTables(_catalog, _schema, null, _types);
            while (tableRs.next()) {
                String realTableName = tableRs.getString("TABLE_NAME");
                sb.append(realTableName);
                sb.append(" ");
            }
        } catch (SQLException e2) {
            _log.warn("Couldn't get schemas", e2);
            sb.append("  ?? Couldn't get schemas ??").append(nl);
        } finally {
            try {
                tableRs.close();
            } catch (Exception ignore) {
                // ignore
            }
        }

        sb.append(nl);
        sb.append("----");
        sb.append(nl);
        return sb.toString();
    }

    /**
     * Gets the Connection attribute of the MiddlegenPopulator object
     *
     * @todo-javadoc Write javadocs for exception
     * @return The Connection value
     * @exception MiddlegenException Describe the exception
     */
    private Connection getConnection() throws MiddlegenException {
        if (_connection == null) {
            _connection = _database.getConnection();
        }
        return _connection;
    }

    /**
     * Gets the MetaData attribute of the SchemaFactory object
     *
     * @todo-javadoc Write javadocs for exception
     * @return The MetaData value
     * @exception MiddlegenException Describe the exception
     */
    private DatabaseMetaData getMetaData() throws MiddlegenException {
        if (_metaData == null) {
            try {
                _metaData = getConnection().getMetaData();
            } catch (SQLException e) {
                throw new MiddlegenException("Couldn't load Metadata");
            }
        }
        return _metaData;
    }

    /**
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Write javadocs for method parameter
     * @param table1 Describe what the parameter does
     * @param table2 Describe what the parameter does
     * @return a collection of registered (in Ant) Many2ManyElement
     */
    private Collection getM2Ms(String table1, String table2) {
        if (table1.compareTo(table2) > 0) {
            String swap = table1;
            table1 = table2;
            table2 = swap;
        }
        // See Many2ManyElement.getOrderedNameWithoutJoinTable()
        String orderedNameWithoutJoinTable = table1 + "--" + table2;
        Collection result = (Collection) _many2many.get(orderedNameWithoutJoinTable);
        if (result == null) {
            result = EMPTY_COLLECTION;
        }
        return result;
    }

    /**
     * Gets the Wanted attribute of the MiddlegenPopulator object
     *
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Write javadocs for method parameter
     * @param table1 Describe what the parameter does
     * @param jointable Describe what the parameter does
     * @param table2 Describe what the parameter does
     * @return The Wanted value
     */
    private boolean isWanted(String table1, String jointable, String table2) {
        boolean result = false;
        Collection candidates = getM2Ms(table1, table2);
        for (Iterator i = candidates.iterator(); i.hasNext();) {
            Many2ManyElement eminem = (Many2ManyElement) i.next();
            if (eminem.matches(table1, jointable, table2)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Gets the RelationSuffix attribute of the MiddlegenPopulator object
     *
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Write javadocs for method parameter
     * @param a Describe what the parameter does
     * @param b Describe what the parameter does
     * @return The RelationSuffix value
     */
    private String getRelationSuffix(Relation a, Relation b) {
        String result;
        if (a.getRelationSuffix().equals("") || b.getRelationSuffix().equals("")) {
            result = a.getRelationSuffix() + b.getRelationSuffix();
        } else {
            result = a.getRelationSuffix() + "-" + b.getRelationSuffix();
        }
        if (!result.equals("")) {
            result = "-by-" + result;
        }
        return result;
    }

    /**
     * Gets the FkRoleSuffix attribute of the MiddlegenPopulator object
     *
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Write javadocs for method parameter
     * @param a Describe what the parameter does
     * @param b Describe what the parameter does
     * @return The FkRoleSuffix value
     */
    private String getFkRoleSuffix(Relation a, Relation b) {
        String result;
        if (a.getFkRoleSuffix().equals("") || b.getFkRoleSuffix().equals("")) {
            result = a.getFkRoleSuffix() + b.getFkRoleSuffix();
        } else {
            result = a.getFkRoleSuffix() + "-" + b.getFkRoleSuffix();
        }
        if (!result.equals("")) {
            result = "_by_" + result;
        }
        return result;
    }

    /**
     * Returns if we are on Oracle
     *
     * @todo-javadoc Write javadocs for exception
     * @todo-javadoc Write javadocs for exception
     * @return <code>true</code> we are on Oracle, <code>false</code> otherwise
     */
    private boolean isOracle() {
        boolean ret = false;
        try {
            ret = (getMetaData().getDatabaseProductName().toLowerCase().indexOf("oracle") != -1);
        } catch (Exception ignore) {
        }
        return ret;
    }

    /**
     * Returns synonym owner for Oracle.
     *
     * @param synonymName Syn name
     * @return Synonym owner for Oracle
     * @throws MiddlegenException If something orrible happens
     */
    private String getSynonymOwner(String synonymName) throws MiddlegenException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String ret = null;
        try {
            ps = getConnection().prepareStatement(
                "select table_owner from sys.all_synonyms where table_name=? and owner=?");
            ps.setString(1, synonymName);
            ps.setString(2, _schema);
            rs = ps.executeQuery();
            if (rs.next()) {
                ret = rs.getString(1);
            } else {
                String databaseStructure = getDatabaseStructure();
                throw new MiddlegenException("Wow! Synonym " + synonymName
                                             + " not found. How can it happen? "
                                             + databaseStructure);
            }
        } catch (SQLException e) {
            String databaseStructure = getDatabaseStructure();
            _log.error(e.getMessage(), e);
            throw new MiddlegenException("Exception in getting synonym owner " + databaseStructure);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                }
            }
        }
        return ret;
    }

    /**
     * Marks the columns as foreign keys if they have a relationship to an
     * unwanted table
     *
     * @todo-javadoc Write javadocs for exception
     * @throws MiddlegenException Describe the exception
     */
    private void markFksToUnwantedTables() throws MiddlegenException {
        ResultSet tableRs = null;
        try {
            tableRs = getMetaData().getTables(_catalog, _schema, null, _types);
            while (tableRs.next()) {
                String tableName = tableRs.getString("TABLE_NAME");
                String tableType = tableRs.getString("TABLE_TYPE");
                // ignore the views, they don't have foreign key relationships
                if (("TABLE".equals(tableType) && !_middlegen.containsTable(tableName))
                    || ("SYNONYM".equals(tableType) && isOracle())) {
                    String ownerSinonimo = null;
                    if ("SYNONYM".equals(tableType) && isOracle()) {
                        ownerSinonimo = getSynonymOwner(tableName);
                    }
                    ResultSet exportedKeyRs = null;
                    if (ownerSinonimo != null) {
                        exportedKeyRs = getMetaData().getExportedKeys(_catalog, ownerSinonimo,
                            tableName);
                    } else {
                        exportedKeyRs = getMetaData().getExportedKeys(_catalog, _schema, tableName);
                    }
                    while (exportedKeyRs.next()) {
                        String fkTableName = exportedKeyRs.getString("FKTABLE_NAME");
                        String fkColumnName = exportedKeyRs.getString("FKCOLUMN_NAME");
                        // Mark the fk field as an fk anyway. This will be useful for column sorting for example
                        if (_middlegen.containsTable(fkTableName)) {
                            DbTable fkTable = _middlegen.getTable(fkTableName);
                            DbColumn fkColumn = (DbColumn) fkTable.getColumn(fkColumnName);
                            fkColumn.setFk(true);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // schemaRs and catalogRs are only used for error reporting if we get an exception
            String databaseStructure = getDatabaseStructure();
            _log.error(e.getMessage(), e);
            throw new MiddlegenException(
                "Couldn't get list of tables from database. Probably a JDBC driver problem."
                        + databaseStructure);
        } finally {
            try {
                tableRs.close();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Describe the method
     *
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Describe the method parameter
     * @param pkTable Describe the method parameter
     * @param pkColumnName Describe the method parameter
     * @param fkTableName Describe the method parameter
     * @param fkColumnName Describe the method parameter
     * @param fkName Describe the method parameter
     * @param fkTables Describe the method parameter
     */
    private void addCrossref(DbTable pkTable, String pkColumnName, String fkTableName,
                             String fkColumnName, String fkName, Map fkTables) {

        DbTable fkTable = _middlegen.getTable(fkTableName);
        DbColumn pkColumn = (DbColumn) pkTable.getColumn(pkColumnName);
        if (!pkColumn.isPk()) {
            _log
                .warn("WARNING: In the relation involving foreign key column "
                      + fkTableName
                      + "("
                      + fkColumnName
                      + ") and primary key column "
                      + pkTable.getSqlName()
                      + "("
                      + pkColumnName
                      + ") the primary key column isn't "
                      + "declared as a primary key column in the database. This may cause errors later on.");
        }

        Map fkNameToColumnMapsMap = (Map) fkTables.get(fkTable);
        if (fkNameToColumnMapsMap == null) {
            fkNameToColumnMapsMap = new HashMap();
            fkTables.put(fkTable, fkNameToColumnMapsMap);
        }

        Collection columnMaps = (Collection) fkNameToColumnMapsMap.get(fkName);
        if (columnMaps == null) {
            columnMaps = new ArrayList();
            fkNameToColumnMapsMap.put(fkName, columnMaps);
        }
        columnMaps.add(new ColumnMap(pkColumnName, fkColumnName));
        // Also, mark the fk field as an fk. It hasn't been done before.
        DbColumn fkColumn = (DbColumn) fkTable.getColumn(fkColumnName);
        fkColumn.setFk(true);
    }

    /**
     * Adds columns to the table, and registers any relations.
     *
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Write javadocs for exception
     * @todo-javadoc Write javadocs for exception
     * @param table The new DbColumns value
     * @exception MiddlegenException Describe the exception
     * @exception SQLException Describe the exception
     */
    private void addColumns(DbTable table) throws MiddlegenException, SQLException {
        _log.debug("-------setColumns(" + table.getSqlName() + ")");

        // get the primary keys
        List primaryKeys = new LinkedList();
        ResultSet primaryKeyRs = null;
        if (table.getTableElement().getOwnerSynonymName() != null) {
            primaryKeyRs = getMetaData().getPrimaryKeys(_catalog,
                table.getTableElement().getOwnerSynonymName(), table.getSqlName());
        } else {
            primaryKeyRs = getMetaData().getPrimaryKeys(_catalog, _schema, table.getSqlName());
        }
        while (primaryKeyRs.next()) {
            String columnName = primaryKeyRs.getString("COLUMN_NAME");
            _log.debug("primary key:" + columnName);
            primaryKeys.add(columnName);
        }
        primaryKeyRs.close();

        // get the indices and unique columns
        List indices = new LinkedList();
        // maps index names to a list of columns in the index
        Map uniqueIndices = new HashMap();
        // maps column names to the index name.
        Map uniqueColumns = new HashMap();
        ResultSet indexRs = null;

        try {

            if (table.getTableElement().getOwnerSynonymName() != null) {
                indexRs = getMetaData().getIndexInfo(_catalog,
                    table.getTableElement().getOwnerSynonymName(), table.getSqlName(), false, true);
            } else {
                indexRs = getMetaData().getIndexInfo(_catalog, _schema, table.getSqlName(), false,
                    true);
            }
            while (indexRs.next()) {
                String columnName = indexRs.getString("COLUMN_NAME");
                if (columnName != null) {
                    _log.debug("index:" + columnName);
                    indices.add(columnName);
                }

                // now look for unique columns
                String indexName = indexRs.getString("INDEX_NAME");
                boolean nonUnique = indexRs.getBoolean("NON_UNIQUE");

                if (!nonUnique && columnName != null && indexName != null) {
                    List l = (List) uniqueColumns.get(indexName);
                    if (l == null) {
                        l = new ArrayList();
                        uniqueColumns.put(indexName, l);
                    }
                    l.add(columnName);
                    uniqueIndices.put(columnName, indexName);
                    _log.debug("unique:" + columnName + " (" + indexName + ")");
                }
            }
        } catch (Throwable t) {
            // Bug #604761 Oracle getIndexInfo() needs major grants
            // http://sourceforge.net/tracker/index.php?func=detail&aid=604761&group_id=36044&atid=415990
        } finally {
            if (indexRs != null) {
                indexRs.close();
            }
        }

        // get the columns
        List columns = new LinkedList();
        ResultSet columnRs = null;
        if (table.getTableElement().getOwnerSynonymName() != null) {
            columnRs = getMetaData().getColumns(_catalog,
                table.getTableElement().getOwnerSynonymName(), table.getSqlName(), null);
        } else {
            columnRs = getMetaData().getColumns(_catalog, _schema, table.getSqlName(), null);
        }
        while (columnRs.next()) {
            int sqlType = columnRs.getInt("DATA_TYPE");
            String sqlTypeName = columnRs.getString("TYPE_NAME");
            String columnName = columnRs.getString("COLUMN_NAME");
            String columnDefaultValue = columnRs.getString("COLUMN_DEF");
            // if columnNoNulls or columnNullableUnknown assume "not nullable"
            boolean isNullable = (DatabaseMetaData.columnNullable == columnRs.getInt("NULLABLE"));
            int size = columnRs.getInt("COLUMN_SIZE");
            int decimalDigits = columnRs.getInt("DECIMAL_DIGITS");

            boolean isPk = false;
            Collection pkColumnsOverride = table.getTableElement().getPkColumnsOverrideCollection();
            if (pkColumnsOverride.size() > 0) {
                isPk = pkColumnsOverride.contains(columnName);
            } else {
                isPk = primaryKeys.contains(columnName);
            }
            boolean isIndexed = indices.contains(columnName);
            String uniqueIndex = (String) uniqueIndices.get(columnName);
            List columnsInUniqueIndex = null;
            if (uniqueIndex != null) {
                columnsInUniqueIndex = (List) uniqueColumns.get(uniqueIndex);
            }

            boolean isUnique = columnsInUniqueIndex != null && columnsInUniqueIndex.size() == 1;
            if (isUnique) {
                _log.debug("unique column:" + columnName);
            }

            Column column = new DbColumn(table, sqlType, sqlTypeName, columnName, size,
                decimalDigits, isPk, isNullable, isIndexed, isUnique, columnDefaultValue);
            columns.add(column);
        }
        columnRs.close();

        for (Iterator i = columns.iterator(); i.hasNext();) {
            Column column = (Column) i.next();
            table.addColumn(column);
        }

        // for each unique index, add a unique tuple to the table
        for (Iterator i = uniqueColumns.values().iterator(); i.hasNext();) {
            List l = (List) i.next();
            List uniqueTuple = new ArrayList();
            for (Iterator j = l.iterator(); j.hasNext();) {
                String colName = (String) j.next();
                Column column = table.getColumn(colName);
                uniqueTuple.add(column);
            }
            table.addUniqueTuple(uniqueTuple);
        }

        // In case none of the columns were primary keys, issue a warning.
        if (primaryKeys.size() == 0) {
            _log.warn("WARNING: The JDBC driver didn't report any primary key columns in "
                      + table.getSqlName());
        }
    }

    /**
     * Tunes the settings depending on database.
     *
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Write javadocs for exception
     * @param metaData Describe what the parameter does
     * @exception SQLException Describe the exception
     */
    private void tune(DatabaseMetaData metaData) throws SQLException {
        String databaseProductName = metaData.getDatabaseProductName();
        String databaseProductVersion = metaData.getDatabaseProductVersion();
        String driverName = metaData.getDriverName();
        String driverVersion = metaData.getDriverVersion();

        DatabaseInfo databaseInfo = new DatabaseInfo(databaseProductName, databaseProductVersion,
            driverName, driverVersion);

        _middlegen.setDatabaseInfo(databaseInfo);

        _log.debug("databaseProductName=" + databaseProductName);
        _log.debug("databaseProductVersion=" + databaseProductVersion);
        _log.debug("driverName=" + driverName);
        _log.debug("driverVersion=" + driverVersion);
        _log.debug("schema=" + _schema);
        _log.debug("catalog=" + _catalog);

        // ORACLE TUNING
        if (isOracle()) {
            // Provided by Michael Szlapa to make it work with Oracle 8.1.6

            // capitalize catalogue
            if (_catalog != null) {
                _catalog = _catalog.toUpperCase();
            }

            // usually the access rights are set up so that you can only query your schema
            // ie. schema = username
            if (_schema != null) {
                _schema = _schema.toUpperCase();
            }
            // null will also retrieve objects for which only synonyms exists, but this objects will not
            // be successfully processed anyway - did not check why  -probably columns not retrieved
            _types = new String[] { "TABLE", "VIEW", "SYNONYM" };
        }

        // MSSQL TUNING
        // TODO David Cowan: check the driverName instead. All drivers for MSSQL
        // will probably not behave in the same way. Possibly add other types too...
        if (databaseProductName.toLowerCase().indexOf("microsoft") != -1) {
            // UNIQUEIDENTIFIER type will return BINARY with the XXXXX driver
            Sql2Java.overridePreferredJavaTypeForSqlType(Types.BINARY, "java.lang.String");

            // possibly other nonstandard types here
            // Sql2Java.overridePreferredJavaTypeForSqlType(???, ???);
            // Sql2Java.overrideAllowedJavaTypesForSqlType( ????, new String[]{???,???,???});
        }
        if (databaseProductName.toLowerCase().indexOf("hsql") != -1) {
            // hsqldb is ok now. no hacks needed anymore.
        }
    }

    /** Establishes m:n relationships */
    private void addMany2ManyRelations() {
        List relations = _middlegen.getRelations();
        int one2manyCount = relations.size();
        for (int i = 0; i < one2manyCount - 1; i++) {
            Relation firstRelation = (Relation) relations.get(i);
            RelationshipRole firstRole = firstRelation.getLeftRole();
            _log.debug("first:" + firstRole.getName());
            for (int j = i + 1; j < one2manyCount; j++) {
                Relation secondRelation = (Relation) relations.get(j);
                RelationshipRole secondRole = secondRelation.getLeftRole();
                _log.debug("second:" + secondRole.getName());
                if (firstRole.getTarget() == secondRole.getTarget()
                    && firstRole.getTarget() != null) {
                    // OK this is a potential m:n. See if it's really wanted.
                    if (isWanted(firstRole.getOrigin().getSqlName(), firstRole.getTarget()
                        .getSqlName(), secondRole.getOrigin().getSqlName())) {
                        Collection m2mElements = getM2Ms(firstRole.getOrigin().getSqlName(),
                            secondRole.getOrigin().getSqlName());
                        String relationSuffix = null;
                        String fkRoleSuffix = null;
                        if (m2mElements.size() > 1) {
                            // There are several relations for those extremities. Use join table in suffix
                            // flights-persons-via-reservations-by-a-b-c-d
                            relationSuffix = "-via-" + firstRole.getTarget().getSqlName()
                                             + getRelationSuffix(firstRelation, secondRelation);
                            fkRoleSuffix = "_via_" + firstRole.getTarget().getSqlName()
                                           + getRelationSuffix(firstRelation, secondRelation);
                            fkRoleSuffix = DbNameConverter.getInstance().columnNameToVariableName(
                                fkRoleSuffix);
                        } else {
                            // Only one. Don't use join table in suffix
                            relationSuffix = getRelationSuffix(firstRelation, secondRelation);
                            fkRoleSuffix = getRelationSuffix(firstRelation, secondRelation);
                        }
                        Relation m2m = new Relation(firstRole.getOrigin(), firstRole
                            .getColumnMaps(), secondRole.getOrigin(), secondRole.getColumnMaps(),
                            firstRole.getTarget(), relationSuffix, fkRoleSuffix);
                        _middlegen.addRelation(m2m);
                    }
                }
            }
        }
    }

    /**
     * Describe what the method does
     *
     * @todo-javadoc Write javadocs for method
     */
    private void warnUnidentifiedM2ms() {
        for (Iterator i = _many2many.values().iterator(); i.hasNext();) {
            Collection c = (Collection) i.next();
            for (Iterator j = c.iterator(); j.hasNext();) {
                Many2ManyElement eminem = (Many2ManyElement) j.next();
                if (!eminem.isMatched()) {
                    _log.warn("The many2many relation " + eminem.toString()
                              + " was declared, but not identified.");
                }
            }
        }
    }

    /**
     * Describe the method
     *
     * @todo-javadoc Describe the method
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Write javadocs for exception
     * @todo-javadoc Write javadocs for exception
     * @param wantedTables Describe the method parameter
     * @exception MiddlegenException Describe the exception
     * @exception SQLException Describe the exception
     */
    private void addTables(Map wantedTables) throws MiddlegenException, SQLException {
        // get the tables
        _log.debug("-- tables --");

        // We're keeping track of the table names so we can detect if a table
        // occurs in different schemas
        Map tableSchemaMap = new HashMap();

        for (Iterator tableElementIterator = wantedTables.values().iterator(); tableElementIterator
            .hasNext();) {
            TableElement tableElement = (TableElement) tableElementIterator.next();
            String tableName = tableElement.getName();
            String schemaName = null;
            // check that the table really exists
            ResultSet tableRs = null;
            try {
                tableRs = getMetaData().getTables(_catalog, _schema, tableName, _types);
                if (!tableRs.next()) {
                    tableRs = getMetaData().getTables(_catalog, _schema, tableName.toLowerCase(),
                        _types);
                    if (!tableRs.next()) {
                        tableRs = getMetaData().getTables(_catalog, _schema,
                            tableName.toUpperCase(), _types);
                        if (!tableRs.next()) {
                            throw new MiddlegenException(
                                "The database doesn't have any table named "
                                        + tableName
                                        + ".  Please make sure the table exists. Also note that some databases are case sensitive."
                                        + getDatabaseTables());
                        }
                    }
                }
                // BUG [ 596044 ] Case in table names - relationships
                // Update the tableElement with the name reported by the resultset.
                // The case might not be the same, and some drivers want correct case
                // in getCrossReference/getExportedKeys which we'll call later.
                schemaName = Util.ensureNotNull(tableRs.getString("TABLE_SCHEM"));
                String realTableName = tableRs.getString("TABLE_NAME");
                String tableType = tableRs.getString("TABLE_TYPE");
                tableElement.setPhysicalName(realTableName);

                if ("SYNONYM".equals(tableType) && isOracle()) {
                    tableElement.setOwnerSynonymName(getSynonymOwner(realTableName));
                }
                // do this for non-synonyms only
                // Test for tables in different schemas
                String alreadySchema = (String) tableSchemaMap.get(realTableName);
                if (alreadySchema != null) {
                    throw new MiddlegenException(
                        "The table named " + realTableName + " was found both in the schema "
                                + "named " + alreadySchema + " and in the schema named "
                                + schemaName + ". "
                                + "You have to specify schema=\"something\" in the middlegen task.");
                }

                tableSchemaMap.put(realTableName, schemaName);

                // Some more schema sanity testing
                // do for non-synonyms only
                if (!("".equals(schemaName)) && !("null".equals(schemaName))
                    && !Util.equals(_schema, schemaName)
                    && !("SYNONYM".equals(tableType) && isOracle())) {
                    _log
                        .warn("The table named "
                              + realTableName
                              + " was found in the schema "
                              + "named \""
                              + schemaName
                              + "\". However, Middlegen was not configured "
                              + "to look for tables in a specific schema. You should consider specifying "
                              + "schema=\"" + schemaName + "\" instead of schema=\"" + _schema
                              + "\" in the middlegen task.");
                }
            } finally {
                try {
                    tableRs.close();
                } catch (SQLException ignore) {
                } catch (NullPointerException ignore) {
                }
            }
            DbTable table = new DbTable(tableElement, schemaName);
            table.init();
            _middlegen.addTable(table);
        }
    }

    /**
     * Describe what this class does
     *
     * @author ewa
     * @created 8. mars 2004
     * @todo-javadoc Write javadocs
     */
    private static class ColumnComparator implements Comparator {

        /**
         * @todo-javadoc Describe the field
         */
        private String _orderBy;

        /**
         * Describe what the ColumnComparator constructor does
         *
         * @todo-javadoc Write javadocs for constructor
         * @todo-javadoc Write javadocs for method parameter
         * @param orderBy Describe what the parameter does
         */
        public ColumnComparator(String orderBy) {
            _orderBy = orderBy;
        }

        /**
         * {@inheritDoc}
         *
         * @param o1
         * @param o2
         * @return
         */
        public int compare(Object o1, Object o2) {
            Column c1 = (Column) o1;
            Column c2 = (Column) o2;
            if (c1.equals(c2)) {
                return 0;
            }
            int priority1 = getColumnPriority(c1);
            int priority2 = getColumnPriority(c2);
            if (priority1 < priority2) {
                return -1;
            }
            if (priority1 > priority2) {
                return 1;
            }
            return c1.getSqlName().compareTo(c2.getSqlName());
        }

        /**
         * Gets the ColumnPriority attribute of the ColumnComparator object
         *
         * @todo-javadoc Write javadocs for method parameter
         * @param column Describe what the parameter does
         * @return The ColumnPriority value
         */
        private int getColumnPriority(Column column) {
            int priority = _orderBy.length();
            int pos = -1;
            if (column.isPk()) {
                pos = _orderBy.indexOf("pk");
                if (pos >= 0 && pos < priority) {
                    priority = pos;
                }
            }
            if (column.isFk()) {
                pos = _orderBy.indexOf("fk");
                if (pos >= 0 && pos < priority) {
                    priority = pos;
                }
            }
            if (column.isNullable()) {
                pos = _orderBy.indexOf("nullable");
                if (pos >= 0 && pos < priority) {
                    priority = pos;
                }
            }
            if (!column.isNullable()) {
                pos = _orderBy.indexOf("mandatory");
                if (pos >= 0 && pos < priority) {
                    priority = pos;
                }
            }
            if (column.isIndexed()) {
                pos = _orderBy.indexOf("indexed");
                if (pos >= 0 && pos < priority) {
                    priority = pos;
                }
            }
            if (column.isUnique()) {
                pos = _orderBy.indexOf("unique");
                if (pos >= 0 && pos < priority) {
                    priority = pos;
                }
            }
            return priority;
        }
    }

}
