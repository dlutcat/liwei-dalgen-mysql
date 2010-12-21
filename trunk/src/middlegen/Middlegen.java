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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class implements the core engine that will initialise and invoke all the
 * plugins.
 *
 * @author <a href="mailto:aslak.hellesoy@bekk.no">Aslak Hellesøy</a>
 * @created 21. mars 2002
 * @version $Id: Middlegen.java,v 1.2 2006/09/18 12:43:50 zhaoxu Exp $
 */
public class Middlegen {
    /**
     * @todo-javadoc Describe the field
     */
    private String                           _appName;

    /**
     * @todo-javadoc Describe the field
     */
    private DatabaseInfo                     _databaseInfo;

    /**
     * @todo-javadoc Describe the column
     */
    private final HashMap                    _tableElements = new HashMap();
    /**
     * @todo-javadoc Describe the column
     */
    private MiddlegenTask                    _middlegenTask;

    /**
     * @todo-javadoc Describe the column
     */
    private final Map                        _tables        = new HashMap();

    /**
     * @todo-javadoc Describe the column
     */
    private final List                       _relations     = new LinkedList();

    /** All plugins */
    private final Collection                 _plugins       = new LinkedList();

    /**
     * @todo-javadoc Describe the column
     */
    private File                             _rootDir;

    /** Maps logical name to plugin class */
    private final Map                        _pluginClasses = new HashMap();

    /**
     * @todo-javadoc Describe the column
     */
    public static final String               _NL            = System.getProperty("line.separator");

    /**
     * @todo-javadoc Describe the column
     */
    public static final String               BUGREPORT      = _NL
                                                              + _NL
                                                              + "PLEASE FILE A BUG REPORT TO http://sourceforge.net/tracker/?group_id=36044&atid=415990."
                                                              + _NL
                                                              + "ENCLOSE THE FOLLOWING INFORMATION:"
                                                              + _NL
                                                              + "-THIS STACK TRACE"
                                                              + _NL
                                                              + "-THE VERSION YOU'RE USING (DATE IF YOU'RE USING CVS VERSION)"
                                                              + _NL + _NL + _NL;

    /** Get static reference to Log4J Logger */
    private static org.apache.log4j.Category _log           = org.apache.log4j.Category
                                                                .getInstance(Middlegen.class
                                                                    .getName());

    /**
     * Creates a new Middlegen object
     *
     * @todo find a smarter way to register handlers and tag factories
     * @todo remove dependency on MiddlegenTask
     * @todo-javadoc Write javadocs for method parameter
     * @param middlegenTask Describe what the parameter does
     */
    public Middlegen(MiddlegenTask middlegenTask) {
        _middlegenTask = middlegenTask;

        //register standard plugins
        registerPlugin("simple", middlegen.Plugin.class);
        registerPlugin("java", middlegen.javax.JavaPlugin.class);
        // for bwc only
        registerPlugin("plugin", middlegen.Plugin.class);
    }

    /**
     * Sets the Appname attribute of the Middlegen object
     *
     * @param s The new Appname value
     */
    public void setAppname(String s) {
        _appName = s;
    }

    /**
     * Gets the Appname attribute of the Middlegen object
     *
     * @return The Appname value
     */
    public String getAppname() {
        return _appName;
    }

    /**
     * Gets the DatabaseInfo attribute of the Middlegen object
     *
     * @return The DatabaseInfo value
     */
    public DatabaseInfo getDatabaseInfo() {
        return _databaseInfo;
    }

    /**
     * Gets the TableElements attribute of the Middlegen object
     *
     * @return The TableElements value
     */
    public Map getTableElements() {
        return _tableElements;
    }

    /**
     * Gets the MiddlegenTask attribute of the Middlegen object
     *
     * @return The MiddlegenTask value
     */
    public MiddlegenTask getMiddlegenTask() {
        return _middlegenTask;
    }

    /**
     * Gets all the registered tables
     *
     * @return A Collection of {@link DbTable} objects
     */
    public Collection getTables() {
        return _tables.values();
    }

    /**
     * Gets all the tables. The returned tables are decorated by the plugin.
     *
     * @param plugin Describe what the parameter does
     * @return A Collection of {@link TableDecorator} objects (of the class
     *      specified by the plugin's {@link Plugin#getTableDecoratorClass()}
     *      method)
     */
    public Collection getTables(Plugin plugin) {
        Collection result = new LinkedList();
        Iterator i = getTables().iterator();
        while (i.hasNext()) {
            result.add(plugin.decorate((DbTable) i.next()));
        }
        return result;
    }

    /**
     * Gets all the relations
     *
     * @todo-javadoc Write javadocs for method parameter
     * @return A Collection of {@link Relation} objects
     */
    public List getRelations() {
        return _relations;
    }

    /**
     * Gets the Plugins attribute of the Middlegen object
     *
     * @return The Plugins value
     */
    public Collection getPlugins() {
        return _plugins;
    }

    /**
     * Gets the Plugin attribute of the Middlegen object
     *
     * @todo-javadoc Write javadocs for method parameter
     * @param name Describe what the parameter does
     * @return The Plugin value
     */
    public Plugin getPlugin(String name) {
        // A plugin might change name after it's added. That's why
        // we don't store it in a map. just iterate.
        for (Iterator p = getPlugins().iterator(); p.hasNext();) {
            Plugin plugin = (Plugin) p.next();
            if (plugin.getName().equals(name)) {
                return plugin;
            }
        }
        return null;
    }

    /**
     * Gets the Table attribute of the Middlegen object
     *
     * @todo-javadoc Write javadocs for method parameter
     * @param tableSqlName Describe what the parameter does
     * @return The Table value
     * @throws MiddlegenException if no table can be found for the given
     *      tableSqlName
     */
    public DbTable getTable(String tableSqlName) {
        DbTable result = (DbTable) _tables.get(tableSqlName.toLowerCase());
        if (result == null) {
            throw new IllegalArgumentException(
                "Couldn't find any table named "
                        + tableSqlName
                        + ". Check the spelling and make sure it figures among the declared tables.");
        }
        return result;
    }

    /**
     * Gets the Class for a logical name. The name must match a name in one of
     * the middlegen-plugins.xml files inside one of the plugin jars on the
     * classpath
     *
     * @todo-javadoc Write javadocs for method parameter
     * @param name logical plugin name
     * @return The Plugin's Class
     */
    public Class getPluginClass(String name) {
        return (Class) _pluginClasses.get(name);
    }

    /**
     * Returns true if Middlegen contains the table
     *
     * @param tableSqlName The sql name of the table
     * @return
     */
    public boolean containsTable(String tableSqlName) {
        return _tables.get(tableSqlName.toLowerCase()) != null;
    }

    /**
     * Adds a plugin
     *
     * @param plugin the one to add
     */
    public void addPlugin(Plugin plugin) {
        _plugins.add(plugin);
        plugin.setMiddlegen(this);
    }

    /**
     * Describe the method
     *
     * @todo-javadoc Describe the method
     * @todo-javadoc Describe the method parameter
     * @todo-javadoc Write javadocs for exception
     * @param tableElement Describe the method parameter
     */
    public void addTableElement(TableElement tableElement) {
        _tableElements.put(tableElement.getName(), tableElement);
    }

    /**
     * Describe what the method does
     *
     * @todo-javadoc Write javadocs for method
     */
    public void clear() {
        _tables.clear();
        _relations.clear();
    }

    /**
     * Adds a table
     *
     * @param table The table to add
     */
    public void addTable(DbTable table) {
        _tables.put(table.getSqlName().toLowerCase(), table);
    }

    /**
     * Adds a feature to the Relation attribute of the Schema object
     *
     * @param relation The feature to be added to the Relation attribute
     */
    public void addRelation(Relation relation) {
        _relations.add(relation);
    }

    /**
     * Describe the method
     *
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Write javadocs for method parameter
     * @todo-javadoc Describe the method
     * @todo-javadoc Describe the method parameter
     * @param name Describe what the parameter does
     * @param clazz Describe what the parameter does
     */
    public void registerPlugin(String name, Class clazz) {
        _log.info("Registering plugin " + name + "->" + clazz.getName());
        _pluginClasses.put(name, clazz);
    }

    /**
     * Tells all file types to decorate all columns and tables. Called by ant
     * task before gui is shown and the generation begins.
     */
    public void decorateAll() {
        Iterator i = getPlugins().iterator();
        while (i.hasNext()) {
            Plugin plugin = (Plugin) i.next();
            // passing ourself, so plugin can ask us for the stuff it wants to decorate.
            plugin.decorateAll(getTables());
        }
    }

    /**
     * Describe what the method does
     *
     * @todo-javadoc Write javadocs for method
     * @todo-javadoc Write javadocs for exception
     * @exception MiddlegenException Describe the exception
     */
    public void validate() throws MiddlegenException {

        // verify that we don't already have a plugin with the same name
        Set pluginNames = new HashSet();

        for (Iterator pluginIterator = getPlugins().iterator(); pluginIterator.hasNext();) {
            Plugin plugin = (Plugin) pluginIterator.next();

            if (pluginNames.contains(plugin.getName())) {
                String msg = "There is already a plugin with the name "
                             + plugin.getName()
                             + ". If you're trying to use several plugins of the same type, please "
                             + "give them a different name. This can be done with the name attribute in the plugin.";

                _log.error(msg);
                throw new MiddlegenException(msg);
            }
            pluginNames.add(plugin.getName());

            _log.info("Validating " + plugin.getName());
            plugin.validate();
        }
    }

    /**
     * Generates source files for all registered file types
     *
     * @todo move this method to FileProducer
     * @todo-javadoc Write javadocs for exception
     * @todo-javadoc Write javadocs for exception
     * @exception MiddlegenException Describe the exception
     */
    public void writeSource() throws MiddlegenException {
        // Loop over all plugins
        for (Iterator pluginIterator = getPlugins().iterator(); pluginIterator.hasNext();) {
            Plugin plugin = (Plugin) pluginIterator.next();
            _log.info("Invoking plugin " + plugin.getName());
            plugin.generate();
        }
    }

    /**
     * Sets the DatabaseInfo attribute of the Middlegen object
     *
     * @param databaseInfo The new DatabaseInfo value
     */
    void setDatabaseInfo(DatabaseInfo databaseInfo) {
        _databaseInfo = databaseInfo;
    }
}
