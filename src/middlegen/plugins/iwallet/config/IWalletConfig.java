/*
 * Taobao.com Inc.
 * Copyright (c) 2000-2004 All Rights Reserved.
 */
package middlegen.plugins.iwallet.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;

/**
 * The top class to hold dal configuration.
 * 
 * @author Cheng Li
 * @version $Id: IWalletConfig.java,v 1.6 2006/07/18 08:36:54 lusu Exp $
 */
public class IWalletConfig {
	/** logger */
	private static final Logger logger = Logger.getLogger(IWalletConfig.class);

	private static IWalletConfig instance = null;

	/** a map of all configured tables */
	private static Map<String, IWalletTableConfig> tableConfigs = new HashMap<String, IWalletTableConfig>();

	/** a map to convert from one java type to another java type */
	private Map<String, String> javaTypeMaps = new HashMap<String, String>();

	/** a list of all configured sequences */
	private List<IWalletSeqConfig> seqConfigs = new ArrayList<IWalletSeqConfig>();

	/** a list of included tables */
	private static List<String> includes = new ArrayList<String>();

	/** 忽略的表名前缀列表 */
	private List<String> tablePrefixList = new ArrayList<String>();

	/** 系统名 added by yangyanzhao 20091231 */
	public static String appName = null;

	public void setAppName(String name) {
		appName = name;
	}

	public String getAppName() {
		return appName;
	}

	public static IWalletConfig getInstance() throws IWalletConfigException {
		if (instance == null) {
			logger.error("DAL配置没有初始化。");
			throw new IWalletConfigException("DAL配置没有初始化。");
		}
		return instance;
	}

	/**
	 * Constructor for IWalletConfig.
	 */
	public IWalletConfig() {
		super();
	}

	/**
	 * Add a table config.
	 * 
	 * @param tableConfig
	 */
	public void addTableConfig(IWalletTableConfig tableConfig) {
		if (tableConfig != null) {
			tableConfigs.put(tableConfig.getSqlName().toLowerCase(),
					tableConfig);
		}
	}

	/**
	 * Add a type mapping.
	 * 
	 * @param fromType
	 * @param toType
	 */
	public void addJavaTypeMap(String fromType, String toType) {
		javaTypeMaps.put(fromType, toType);
	}

	/**
	 * Add a type mapping.
	 * 
	 * @param fromType
	 * @param toType
	 */
	public void addInclude(String table) {
		includes.add(table);
	}

	/**
	 * Add a table name prefix to list.
	 * 
	 * @param prefix
	 */
	public void addTablePrefix(String prefix) {
		tablePrefixList.add(prefix);
	}

	/**
	 * Load configuration from file.
	 */
	public static synchronized void init(File configFile)
			throws IWalletConfigException {
		if (instance != null) {
			return;
		}

		try {
			instance = new IWalletConfig();

			Digester digester = new Digester();

			digester.push(instance);
			digester.setValidating(false);

			// added by yangyanzhao 20091231 support for appName property
			digester.addCallMethod("tables/appName", "setAppName", 0);

			// support for type map config
			digester.addCallMethod("tables/typemap", "addJavaTypeMap", 2,
					new String[] { "java.lang.String", "java.lang.String" });
			digester.addCallParam("tables/typemap", 0, "from");
			digester.addCallParam("tables/typemap", 1, "to");

			// support for include config
			digester.addCallMethod("tables/include", "addInclude", 1,
					new String[] { "java.lang.String" });
			digester.addCallParam("tables/include", 0, "table");

			// support for tableprefix config
			digester.addCallMethod("tables/tableprefix", "addTablePrefix", 1,
					new String[] { "java.lang.String" });
			digester.addCallParam("tables/tableprefix", 0, "prefix");

			// parse sequence
			digester.addObjectCreate("tables/seq", IWalletSeqConfig.class);
			digester.addSetProperties("tables/seq", "name", "name");
			digester.addSetNext("tables/seq", "addSeqConfig");

			digester.parse(configFile);

			if (null == appName)
				throw new IWalletConfigException(
						"必须在"
								+ configFile.getPath()
								+ "文件中的<tables>内设置子元素<appName>，其值为当前系统的名称，如cif、acctrans等");

			if (!includes.isEmpty()) {
				digester = new Digester();
				digester.setValidating(false);

				// parse basic table config
				digester.addObjectCreate("table", IWalletTableConfig.class);
				digester.addSetProperties("table", "sqlname", "sqlName");
				digester.addSetProperties("table", "doname", "doName");
				digester.addSetProperties("table", "subpackage", "subPackage");
				digester.addSetProperties("table", "sequence", "sequence");
				// add by zhaoxu 20061225
				digester.addSetProperties("table", "autoswitchdatasrc",
						"autoSwitchDataSrc");
				// add by zhaoxu 2007-10-26 虚拟主键配置，insert时，当无主键或多主键时，虚拟字段为主键
				digester.addSetProperties("table", "dummypk", "dummyPk");

				// parse column
				digester.addObjectCreate("table/column",
						IWalletColumnConfig.class);
				digester.addSetProperties("table/column", "name", "name");
				digester.addSetProperties("table/column", "javatype",
						"javaType");
				digester.addSetNext("table/column", "addColumn");

				// parse resultmap
				digester.addObjectCreate("table/resultmap",
						IWalletResultMapConfig.class);
				digester.addSetProperties("table/resultmap", "name", "name");
				digester.addSetNext("table/resultmap", "addResultMap");

				// parse resultmap column
				digester.addObjectCreate("table/resultmap/column",
						IWalletColumnConfig.class);
				digester.addSetProperties("table/resultmap/column", "name",
						"name");
				digester.addSetProperties("table/resultmap/column", "javatype",
						"javaType");
				digester.addSetNext("table/resultmap/column", "addColumn");

				// parse operation
				digester.addObjectCreate("table/operation",
						IWalletOperationConfig.class);
				digester.addSetProperties("table/operation", "name", "name");
				digester.addSetProperties("table/operation", "paramtype",
						"paramType");
				digester.addSetProperties("table/operation", "multiplicity",
						"multiplicity");
				digester
						.addSetProperties("table/operation", "paging", "paging");
				digester.addSetProperties("table/operation", "resultmap",
						"resultMap");
				digester.addSetProperties("table/operation", "resultclass",
						"resultClass");
				digester.addSetProperties("table/operation", "append", "append");
				digester.addSetProperties("table/operation", "countSqlEndAt", "countSqlEndAt");

				digester.addObjectCreate("table/operation/extraparams/param",
						IWalletParamConfig.class);
				digester.addSetProperties("table/operation/extraparams/param",
						"name", "name");
				digester.addSetProperties("table/operation/extraparams/param",
						"javatype", "javaType");
				digester.addSetNext("table/operation/extraparams/param",
						"addExtraParam");

				digester.addCallMethod("table/operation/sql", "setSql", 0);
				digester
						.addCallMethod("table/operation/sqlmap", "setSqlmap", 0);
				// added by yangyanzhao 2010-02-08
				digester.addCallMethod("table/operation/description",
						"setDescription", 0);
				digester.addSetNext("table/operation", "addOperation");
				digester.addSetNext("table", "addTableConfig");

				for (Iterator<String> i = includes.iterator(); i.hasNext();) {
					digester.clear();
					digester.push(instance);

					digester.parse(new File(configFile.getParentFile(),
							(String) i.next()));
				}
			}
		} catch (Exception e) {
			// clear table config
			tableConfigs = new HashMap<String, IWalletTableConfig>();

			throw new IWalletConfigException("Can't parse configuration file "
					+ configFile.getPath() + " due to exception.", e);
		}
	}

	/**
	 * Get the table configuration by its database name.
	 * 
	 * @param sqlName
	 * 
	 * @return
	 */
	public IWalletTableConfig getTableConfig(String sqlName) {
		if (sqlName != null) {
			return (IWalletTableConfig) tableConfigs.get(sqlName.toLowerCase());
		} else {
			return null;
		}
	}

	/**
	 * Map a java type to a more proper one.
	 * 
	 * @param javaType
	 * 
	 * @return
	 */
	public String getMappedJavaType(String javaType) {
		if (javaTypeMaps.containsKey(javaType)) {
			return (String) javaTypeMaps.get(javaType);
		} else {
			return javaType;
		}
	}

	/**
	 * @param
	 */
	public void addSeqConfig(IWalletSeqConfig seqConfig) {
		if (seqConfig != null) {
			seqConfigs.add(seqConfig);
		}
	}

	/**
	 * @return
	 */
	public List<IWalletSeqConfig> getSeqConfigs() {
		return seqConfigs;
	}

	/**
	 * 取得所有的TableName。
	 */
	public List<String> getAllTableNames() {
		List<String> tableNames = new ArrayList<String>(tableConfigs.keySet());

		Collections.sort(tableNames);
		return tableNames;
	}

	/**
	 * 取得要忽略的表名前缀列表
	 * 
	 * @return Returns the tablePrefixList.
	 */
	public List<String> getTablePrefixList() {
		return tablePrefixList;
	}
}
