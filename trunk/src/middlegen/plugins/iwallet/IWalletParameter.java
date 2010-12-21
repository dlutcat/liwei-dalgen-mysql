/*
 * Taobao.com Inc.
 * Copyright (c) 2000-2004 All Rights Reserved.
 */
package middlegen.plugins.iwallet;

import middlegen.DbNameConverter;
import middlegen.PreferenceAware;

import middlegen.javax.Sql2Java;

import middlegen.plugins.iwallet.util.DalUtil;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import com.alibaba.common.lang.StringUtil;

/**
 * A decoration class for a dao operation parameter.
 *
 * @author Cheng Li
 *
 * @version $Id: IWalletParameter.java,v 1.2 2005/05/25 06:09:15 lusu Exp $
 */
public class IWalletParameter extends PreferenceAware implements Parameter {
    /** logger */
    private static final Logger logger               = Logger.getLogger(IWalletParameter.class);
    public static final String  PARAM_NAME_PAGE_SIZE = "pageSize";
    public static final String  PARAM_NAME_PAGE_NUM  = "pageNum";

    /** the name of the parameter */
    private String name;

    /** the qualified java type of the parameter */
    private String javaType;

    /** an instance of an IWalletOperation */
    private IWalletOperation operation;
    private String           suffix;
    
    /** the sqlType of the parameter. eg:NUMBER VARCHAR2 */
    private String sqlType;

    /**
     * Constructor for IWalletParameter.
     */
    public IWalletParameter(IWalletOperation operation, String name) {
        this(operation, name, null);
    }

    /**
     * Constructor for IWalletParameter.
     */
    public IWalletParameter(IWalletOperation operation, String name, String suffix) {
        super();

        this.operation = operation;
        this.name      = name;
        this.suffix    = suffix;
    }

    /**
     * @return
     *
     * @see middlegen.PreferenceAware#prefsPrefix()
     */
    protected String prefsPrefix() {
        return "java-method-param";
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.Parameter#getQualifiedJavaType()
     */
    public String getQualifiedJavaType() {
        // special case first
    	if (StringUtil.isNotBlank(javaType)) {
    		return javaType;
    	}
    	
        // parameters used to control paging
        if (PARAM_NAME_PAGE_SIZE.equals(name) || PARAM_NAME_PAGE_NUM.equals(name)) {
            return "int";
        }

        if (name.endsWith("_list")) {
            return "java.lang.String";
        }

        if (name.equalsIgnoreCase("rownum")) {
            return "int";
        }

        // more special parameters can be added here
        // parameter as dataobject
        if (name.equals(((IWalletTable) operation.getTable()).getSingularisedVariableName())) {
            return ((IWalletTable) operation.getTable()).getQualifiedDOClassName();
        }

        // parameter corresponding to database table column
        IWalletColumn column = null;

        try {
            column = (IWalletColumn) operation.getTable().getColumn(name);
        } catch (Exception e) {
            // ignore
            if (logger.isDebugEnabled()) {
                logger.debug("Can't get column for name " + name, e);
            }
        }

        if (column != null) {
            return column.getJavaType();
        }

        return null;
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.Parameter#getSimpleJavaType()
     */
    public String getSimpleJavaType() {
        return DalUtil.getSimpleJavaType(getQualifiedJavaType());
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.Parameter#getName()
     */
    public String getName() {
        String retName = "";

        if (PARAM_NAME_PAGE_SIZE.equals(name) || PARAM_NAME_PAGE_NUM.equals(name)) {
            // parameters used to control paging
            retName = name;
        } else if (name.equals(((IWalletTable) operation.getTable()).getSingularisedVariableName())) {
            retName = name;
        } else {
            IWalletColumn column = null;

            try {
                column = (IWalletColumn) operation.getTable().getColumn(name);
            } catch (Exception e) {
                // ignore
                if (logger.isDebugEnabled()) {
                    //logger.debug("Can't get column by name " + name, e);
                }
            }

            if (column != null) {
                retName = column.getVariableName();
            } else {
                retName = middlegen.Util.decapitalise(DbNameConverter.getInstance()
                                                                     .columnNameToVariableName(name));
            }
        }

        if (StringUtils.isBlank(suffix)) {
            return retName;
        } else {
            return retName + suffix;
        }
    }

    /**
     *
     * @return
     */
    public boolean isJavaTypePrimitive() {
        // simplistic approach
        return Sql2Java.isPrimitive(getQualifiedJavaType());
    }

    /**
     * @return
     */
    public String getJavaTypeForPrimitive() {
        return Sql2Java.getClassForPrimitive(getQualifiedJavaType());
    }

    /**
     * @return
     */
    public String getSimpleJavaTypeForPrimitive() {
        return DalUtil.getSimpleJavaType(getJavaTypeForPrimitive());
    }

    /**
     * @param javaType The javaType to set.
     */
    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

	/**
	 * @return the sqlType
	 */
	public String getSqlType() {
		return sqlType;
	}

	/**
	 * @param sqlType the sqlType to set
	 */
	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}
}
