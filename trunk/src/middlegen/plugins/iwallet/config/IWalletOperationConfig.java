/*
 * Taobao.com Inc.
 * Copyright (c) 2000-2004 All Rights Reserved.
 */
package middlegen.plugins.iwallet.config;

import Zql.ZStatement;

import middlegen.plugins.iwallet.util.SqlParser;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A bean class represents configuration concerning an operation.
 *
 * <p>
 * It's the base class for all sorts of operations, maintains the
 * commonality of all operations. Each operation subclass will handle
 * a specific operation type, such as "insert", "delete-by-pk", etc.
 *
 * @author Cheng Li
 *
 * @version $Id: IWalletOperationConfig.java,v 1.3 2006/07/18 08:36:53 lusu Exp $
 */
public class IWalletOperationConfig {
    /** logger */
    private static final Logger logger      = Logger.getLogger(IWalletOperationConfig.class);
    public static final String  CDATA_START = "${cdata-start}";
    public static final String  CDATA_END   = "${cdata-end}";

    /** the name (method name) of this operation */
    private String name;

    /** how to pass parameters to dao: primitive/object */
    private String paramType;

    /** the multiplicity of results: one/many */
    private String multiplicity;

    /** whether paging is needed */
    private boolean paging;

    /** the name of the resultMap, if null then use the table's default result map */
    private String resultMap;
    
    /** the full qualified class name of the result class */
    private String resultClass;

    /** the instance of the table configuration as container */
    private IWalletTableConfig tableConfig;

    /** the parsed sql */
    private SqlParser sqlParser;
    
    private String append;
    
    private String countSqlEndAt;
    
    /** extra params */
    private List extraParams = new ArrayList();

    /**
     * the sql statement of the operation.
     *
     * <p>
     * Not all operations have explictly specified sql statement.
     * The sql statement for operations of some types may be
     * deducible from table meta data.
     * */
    private String sql;

    /**
     * the customized sqlmap mapped statement.
     */
    private String sqlmap;
    
    //added by yangyanzhao 2010-02-08
    /** operation description */
    private String description;

    /**
     * Constructor for IWalletOperationConfig.
     */
    public IWalletOperationConfig() {
        super();
    }

    /**
     * @return
     */
    public String getSql() {
        return sql;
    }

    /**
     * @param string
     */
    public void setSql(String string) {
        if (string == null) {
            sql = "";
        } else {
            sql = string;
        }

        if (sql.endsWith(";")) {
            sqlParser = new SqlParser(sql);
        } else {
            sqlParser = new SqlParser(sql + ";");
        }

        if (sqlParser == null) {
            logger.error("Can't parse sql: " + sql);
        }
    }

    /**
     * @return
     */
    public IWalletTableConfig getTableConfig() {
        return tableConfig;
    }

    /**
     * @param config
     */
    public void setTableConfig(IWalletTableConfig config) {
        tableConfig = config;
    }

    /**
     * @return
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[name=").append(name).append(", paramtype=").append(paramType)
          .append(", multiplicity=").append(multiplicity).append(", paging=").append(paging)
          .append(", sql=").append(sql).append(", zstatement=").append(getZst()).append("]");

        return sb.toString();
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    /**
     * @return
     */
    public ZStatement getZst() {
        if (sqlParser != null) {
            return sqlParser.getZst();
        } else {
            return null;
        }
    }

    /**
     * @return
     */
    public String getMultiplicity() {
        return multiplicity;
    }

    /**
     * @return
     */
    public String getParamType() {
        return paramType;
    }

    /**
     * @param string
     */
    public void setMultiplicity(String string) {
        multiplicity = string;
    }

    /**
     * @param string
     */
    public void setParamType(String string) {
        paramType = string;
    }

    /**
     * @return
     */
    public SqlParser getSqlParser() {
        return sqlParser;
    }

    /**
     * @return
     */
    public boolean isPaging() {
        return paging;
    }

    /**
     * @param b
     */
    public void setPaging(boolean b) {
        paging = b;
    }

    /**
     * @param string
     */
    public void setSqlmap(String string) {
        if (StringUtils.isBlank(string)) {
            return;
        }

        String temp = StringUtils.replace(string, CDATA_START, "<![CDATA[");

        temp = StringUtils.replace(temp, CDATA_END, "]]>");

        sqlmap = temp;
    }

    /**
     * @return
     */
    public String getSqlmap(List params) {
        if (params == null) {
            return sqlmap;
        }

        String temp = sqlmap;

        if (params.size() == 1) {
            temp = StringUtils.replace(temp, "${param1}", "value");
        } else {
            for (int i = 0; i < params.size(); i++) {
                temp = StringUtils.replace(temp, "${param" + (i + 1) + "}", (String) params.get(i));
            }
        }

        return temp;
    }

    /**
     * @return
     */
    public boolean isHasSqlmap() {
        return StringUtils.isNotBlank(sqlmap);
    }

    /**
     * @return
     */
    public String getResultMap() {
        return resultMap;
    }

    /**
     * @param string
     */
    public void setResultMap(String string) {
        resultMap = string;
    }

    /**
     * @return the resultClass
     */
    public String getResultClass() {
        return resultClass;
    }

    /**
     * @param resultClass the resultClass to set
     */
    public void setResultClass(String resultClass) {
        this.resultClass = resultClass;
    }

    /**
     * @return Returns the extraParams.
     */
    public List getExtraParams() {
        return extraParams;
    }

    /**
     * Add one more extra params.
     *
     * @param paramConfig
     */
    public void addExtraParam(IWalletParamConfig paramConfig) {
        extraParams.add(paramConfig);
    }

    /**
     * @return Returns the append.
     */
    public String getAppend() {
        return append;
    }

    /**
     * @param append The append to set.
     */
    public void setAppend(String append) {
        this.append = append;
    }
    
    /**
     * 设置sql操作(select/update等)的注释
     * @param description
     */
    public void setDescription(String description) {
        if (description == null) {
            this.description = "";
        } else {
            this.description = description;
        }
    }
    
    /**
     * 获取sql操作的注释
     * @param description
     */    
    public String getDescription(){
        return this.description;
    }

    /**
     * 设置需要截掉的语句的起始字符串（起始字符串长度任意）
     * @param countSqlEndAt
     */
    public void setCountSqlEndAt(String countSqlEndAt) {
        this.countSqlEndAt = countSqlEndAt;
    }
    /**
     * 获得需要截掉的语句的起始字符串
     * @return
     */
    public String getCountSqlEndAt() {
        return countSqlEndAt;
    }
    
}
