/*
 * Taobao.com Inc.
 * Copyright (c) 2000-2004 All Rights Reserved.
 */
package middlegen.plugins.iwallet.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A bean class represents an iwallet table configuration.
 *
 * @author Cheng Li
 *
 * @version $Id: IWalletTableConfig.java,v 1.1 2004/12/24 07:34:20 baobao Exp $
 */
public class IWalletTableConfig {
    /** the table name in database */
    private String sqlName;

    /** the dataobject name */
    private String doName;

    /** the sub package name */
    private String subPackage;

    /** the sequence corresponding to the table */
    private String sequence;
    
    // add by zhaoxu 20061225
    private boolean autoSwitchDataSrc;
    
    /**
     * ÐéÄâÖ÷¼ü
     * add by zhaoxu 2007-10-26
     */
    private String dummyPk;

    /** a list of all configured operations */
    private List operations = new ArrayList();

    /** a map of all column configuration */
    private Map columns = new HashMap();

    /** a list of all result maps */
    private List resultMaps = new ArrayList();
    
    /**
     * Constructor for IWalletTableConfig.
     */
    public IWalletTableConfig() {
        super();
    }

    /**
     * @return
     */
    public String getSqlName() {
        return sqlName;
    }

    /**
     * @return
     */
    public String getDoName() {
        return doName;
    }

    /**
     * @return
     */
    public String getSubPackage() {
        return subPackage;
    }

    /**
     * @param string
     */
    public void setSqlName(String string) {
        sqlName = string;
    }

    /**
     * @param string
     */
    public void setDoName(String string) {
        doName = string;
    }

    /**
     * @param string
     */
    public void setSubPackage(String string) {
        subPackage = string;
    }

    /**
     * @return
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        String       newLine = System.getProperty("line.separator");

        sb.append("[").append("sqlname=").append(sqlName).append(", doname=").append(doName)
          .append(", subpackage=").append(subPackage).append(", sequence=").append(sequence).append("]").append(newLine);

		for (int i = 1; i <= resultMaps.size(); i++) {
			sb.append("rm-").append(i).append(": ").append(resultMaps.get(i - 1)).append(newLine);
		}

        for (int i = 1; i <= operations.size(); i++) {
            sb.append("op-").append(i).append(": ").append(operations.get(i - 1)).append(newLine);
        }

        for (Iterator i = columns.keySet().iterator(); i.hasNext();) {
            String columnName = (String) i.next();

            sb.append("column:").append(columns.get(columnName)).append(newLine);
        }

        return sb.toString();
    }

    /**
     * @return
     */
    public List getOperations() {
        return operations;
    }

    /**
     * Add an operation configuration to the operation list,
     * and have the operation points to this table configuration.
     *
     * @param operationConfig
     */
    public void addOperation(IWalletOperationConfig operationConfig) {
        operations.add(operationConfig);
        operationConfig.setTableConfig(this);
    }

    /**
     * Get a column configuration by its name.
     *
     * @param name
     * @return
     */
    public IWalletColumnConfig getColumn(String name) {
        return (IWalletColumnConfig) columns.get(name.toLowerCase());
    }

    /**
     * Add a column configuration.
     *
     * @param columnConfig
     */
    public void addColumn(IWalletColumnConfig columnConfig) {
        if (columnConfig != null) {
            columns.put(columnConfig.getName().toLowerCase(), columnConfig);
        }
    }

    /**
     * @return
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * @param string
     */
    public void setSequence(String string) {
        sequence = string;
    }

    /**
     * @return
     */
    public List getResultMaps() {
        return resultMaps;
    }
    
    /**
     * @param operationConfig
     */
    public void addResultMap(IWalletResultMapConfig resultMapConfig) {
        resultMaps.add(resultMapConfig);
        resultMapConfig.setTableConfig(this);
    }

	/**
	 * add by zhaoxu 20061225
	 * 
	 * @return the autoSwitchDataSrc
	 */
	public boolean isAutoSwitchDataSrc() {
		return autoSwitchDataSrc;
	}

	/**
	 * add by zhaoxu 20061225
	 * 
	 * @param autoSwitchDataSrc the autoSwitchDataSrc to set
	 */
	public void setAutoSwitchDataSrc(boolean autoSwitchDataSrc) {
		this.autoSwitchDataSrc = autoSwitchDataSrc;
	}

    /**
     * @return Returns the dummyPk.
     */
    public String getDummyPk() {
        return dummyPk;
    }

    /**
     * @param dummyPk The dummyPk to set.
     */
    public void setDummyPk(String dummyPk) {
        this.dummyPk = dummyPk;
    }
}
