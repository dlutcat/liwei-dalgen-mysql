/*
 * Taobao.com Inc.
 * Copyright (c) 2000-2004 All Rights Reserved.
 */
package middlegen.plugins.iwallet.operation;

import java.util.Vector;

import middlegen.Table;
import middlegen.plugins.iwallet.IWalletColumn;
import middlegen.plugins.iwallet.IWalletOperation;
import middlegen.plugins.iwallet.IWalletPlugin;
import middlegen.plugins.iwallet.IWalletTable;
import middlegen.plugins.iwallet.config.IWalletOperationConfig;
import middlegen.plugins.iwallet.util.DalUtil;
import middlegen.plugins.iwallet.util.SqlParser;

import org.apache.log4j.Logger;

import Zql.ZQuery;
import Zql.ZSelectItem;

import com.alibaba.common.lang.StringUtil;

/**
 * An implementation of "select" operation.
 *
 * @author Cheng Li
 *
 * @version $Id: IWalletSelect.java,v 1.4 2006/07/18 08:36:54 lusu Exp $
 */
public class IWalletSelect extends IWalletOperation {
    /** logger */
    public static final Logger logger                             = Logger
                                                                      .getLogger(IWalletSelect.class);

    /** default java type to return the result of multiple records */
    public static final String DEFAULT_MANY_RETURN_TYPE_NO_PAGING = "java.util.List";

    /** default java type to return the result of multiple records with paging */
    public static final String DEFAULT_MANY_RETURN_TYPE_PAGING    = "com.iwallet.biz.common.util.PageList";
    public static final String OP_TYPE                            = "select";
    private String             parsedSqlForCount                  = null;

    /**
     * Constructor for IWalletSelect.
     */
    public IWalletSelect(IWalletOperationConfig conf) {
        super(conf);

        if (PARAM_TYPE_OBJECT.equals(conf.getParamType())) {
            paramType = PARAM_TYPE_OBJECT;
        } else {
            // default
            paramType = PARAM_TYPE_PRIMITIVE;
        }

        if (MULTIPLICITY_MANY.equals(conf.getMultiplicity())) {
            multiplicity = MULTIPLICITY_MANY;
        } else {
            // default
            multiplicity = MULTIPLICITY_ONE;
        }
    }

    protected String getReturnTypeOne() {
        if (opConfig.getResultMap() != null) {
            return ((IWalletTable) getTable()).getResultMap(opConfig.getResultMap()).getClassAttr();
        } else if (opConfig.getResultClass() != null) {
            return opConfig.getResultClass();
        } else {
            return getColumnType();
        }
    }

    private String getColumnType() {
        SqlParser parser = opConfig.getSqlParser();

        if (parser.isSelectItemSingle()) {
            ZSelectItem item = parser.getSelectItem();

            if (item.getAggregate() != null) {
                // the select item is an aggregate
                String aggregateFunc = item.getAggregate();

                if (logger.isDebugEnabled()) {
                    //                        logger.debug("The aggregate func is " + aggregateFunc);
                }

                if (aggregateFunc.equalsIgnoreCase("COUNT")) {
                    return "long";
                } else if (aggregateFunc.equalsIgnoreCase("SUM")
                           || aggregateFunc.equalsIgnoreCase("AVG")
                           || aggregateFunc.equalsIgnoreCase("MAX")
                           || aggregateFunc.equalsIgnoreCase("MIN")) {
                    String columnName = item.getColumn();
                    int indexStart = columnName.indexOf("(");
                    int indexEnd = columnName.indexOf(")", indexStart);

                    columnName = columnName.substring(indexStart + 1, indexEnd);

                    if (logger.isDebugEnabled()) {
                        //                            logger.debug("The column to be aggregated is " + columnName + ".");
                    }

                    return ((IWalletColumn) (getTable().getColumn(columnName))).getJavaType();
                } else {
                    // can not happen
                    return "void";
                }
            } else {
                return ((IWalletColumn) (getTable().getColumn(item.getColumn()))).getJavaType();
            }
        } else {
            return ((IWalletTable) getTable()).getQualifiedDOClassName();
        }
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.Operation#getReturnTypeName()
     */
    public String getReturnType() {
        if (MULTIPLICITY_MANY.equals(multiplicity)) {
            if (isPaging()) {
                return DEFAULT_MANY_RETURN_TYPE_PAGING;
            } else {
                //���ӷ�����ĵ��� , modify by lejin , 2009.07.31
                this.addImprotForGenericType();
                return DEFAULT_MANY_RETURN_TYPE_NO_PAGING;
            }
        } else {
            return getReturnTypeOne();
        }
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.Operation#getTemplateSuffix()
     */
    public String getTemplateSuffix() {
        return OP_TYPE;
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.IWalletOperation#isPaging()
     */
    public boolean isPaging() {
        return opConfig.isPaging();
    }

    /**
     * @param t
     *
     * @see middlegen.plugins.iwallet.IWalletOperation#setTable(middlegen.Table)
     */
    public void setTable(Table t) {
        super.setTable(t);

        // add additional imports used by method body
        if (isPaging()) {
            ((IWalletTable) t).addIbatisImport(IWalletPlugin.PAGINATOR_CLASS);
        }
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.Operation#getSimpleReturnType()
     */
    public String getSimpleReturnType() {
        String simpleReturnType = DalUtil.getSimpleJavaType(getReturnType());
        if (StringUtil.equals("List", simpleReturnType)) {
            String itemType = this.getColumnType();
            if (StringUtil.equals(itemType, "long")) {
                itemType = "Long";
            } else if (StringUtil.equals(itemType, "int")) {
                itemType = "Integer";
            } else {
                itemType = DalUtil.getSimpleJavaType(itemType);
            }
            simpleReturnType += "<" + itemType + ">";
        }

        return simpleReturnType;
    }

    /**
     * ���ӷ�����ĵ���
     * <p>
     * ��������������ΪListʱ,���ǽ���ת��Ϊ����,������List<???>����;<br>
     * �˷���,��������������,�����Ӷ�???��ĵ���<br>
     * ���???����Ϊlong,int,����Ҫ���ӵ���;
     * </p>
     * 
     * add by lejin,2009-7-31 ����03:14:27,
     * @author lejin 
     */
    private void addImprotForGenericType() {

        String itemType = this.getColumnType();
        if (StringUtil.equals(itemType, "long") || StringUtil.equals(itemType, "int")) {
            //����Ϊlong,int,����Ҫ���ӵ���;
            return;
        } else {
            //add by lejin,2009-7-31 ����11:14:04, ��*DAO���ibatis*DAO��������itemType��improt;
            ((IWalletTable) getTable()).addDaoImport(itemType);
            ((IWalletTable) getTable()).addIbatisImport(itemType);
            return;
        }
    }

    /**
     * @return
     */
    public String getMappedStatementType() {
        return OP_TYPE;
    }

    /**
     * @return
     */
    public String getMappedStatementResult() {
        if (opConfig.getResultMap() != null) {
            return "resultMap=\"" + opConfig.getResultMap() + "\"";
        } else {
            String result = getReturnTypeOne();

            if (((IWalletTable) getTable()).getQualifiedDOClassName().equals(result)) {
                return "resultMap=\"" + ((IWalletTable) getTable()).getResultMapId() + "\"";
            } else if (IWalletPlugin.MONEY_CLASS.equals(result)) {
                return "resultMap=\"" + IWalletPlugin.MONEY_RESULT_MAP_ID + "\"";
            } else {
                return "resultClass=\"" + result + "\"";
            }
        }
    }

    public String getStartRowName() {
        return "startRow";
    }

    public String getEndRowName() {
        return "endRow";
    }

    /**
     * @return
     */
    public String getMappedStatementSqlForPaging() {
        StringBuffer pagingSql = new StringBuffer();

        if (isHasSqlmap()) {
        	pagingSql.append("select /*" + getMappedStatementId(true) + "*/ * from (").append(
                "select T1.*, rownum linenum from (").append(getMappedStatementSqlNoAnnotation()).append(
                ") T1 where rownum &lt;= #").append(getEndRowName()).append("#").append(
                ") T2 where linenum &gt;= #").append(getStartRowName()).append("#");
        } else {
        	pagingSql.append("select /*" + getMappedStatementId(true) + "*/ * from (").append(
                "select T1.*, rownum linenum from (").append(getMappedStatementSqlNoAnnotation()).append(
                ") T1 where rownum <= #").append(getEndRowName()).append("#").append(
                ") T2 where linenum >= #").append(getStartRowName()).append("#");
        }

        return pagingSql.toString();
    }

    /**
     * �ӽ�ԭʼ��SQLת����һ��ͳ��������SQL.
     *
     * <p>
     * ��select�Ӿ�ת��Ϊselect count(*)����order by�Ӿ�ȥ����
     *
     * @return
     */
    public String getMappedStatementSqlForCount() {
        if (isHasSqlmap()) {
            // ԭʼmapped statement - ����Ϊibatis��ʽ�Ķ�̬statement
            String origMs = getMappedStatementSqlNoAnnotation();

            /*
             * ����mapped statement���Ǳ�׼��SQL����˺��ѽ��г��׵ط����������ȡһ�ּ򵥵ķ�����
             * ���ݹؼ��ֺ�һЩ��Ҫ�ļ�������ԭʼ��mapped statement�е�һЩ����ֱ���滻��ɾ����
             */
            int indexSelectStart = StringUtil.indexOfAny(origMs, new String[] { "select ",
                    "SELECT " });
            int indexSelectEnd = StringUtil.indexOfAny(origMs, new String[] { "from ", "FROM " });
            /*
             * ���û������countSqlEndAt���ԣ���Ĭ��ȥ��order by�־�
             * ������ȥ��countSqlEndAt��ָ���ַ�����ͷ���־�
             * ����Ὣ"��ʼ�ַ���"��"]]>"���ַ�����ȥ��
             */
            int indexStart = 0;
            int indexEnd = 0;
            String endingStr = opConfig.getCountSqlEndAt();
            if(!StringUtil.isBlank(endingStr)){
                indexStart = StringUtil.indexOfAny(origMs, new String[] { endingStr, StringUtil.toUpperCase(endingStr) });
            }
            else{
                indexStart = StringUtil.indexOfAny(origMs, new String[] { "order by ","ORDER BY " });
            }
            
            if (indexStart > 0) {
                indexEnd = StringUtil.indexOf(origMs, "]]>", indexStart);
                if (indexEnd < 0) {
                    indexEnd = origMs.length();
                }
                if(origMs.charAt(indexEnd-1) == ')')
                    indexEnd = indexEnd-1;
            } else {
                indexStart = origMs.length();
            }

            // ת�����mapped statement
            StringBuffer ret = new StringBuffer();

            ret.append(StringUtil.substring(origMs, 0, indexSelectStart));
            ret.append("select count(*) ");
            ret.append(StringUtil.substring(origMs, indexSelectEnd, indexStart));

            if ((indexStart < origMs.length()) && (indexEnd < origMs.length())) {
                ret.append(StringUtil.substring(origMs, indexEnd));
            }

            return addSqlAnnotationForCount(ret.toString());
        } else {
            return addSqlAnnotationForCount(getMappedStatementSql(getParsedSqlForCount()));
        }
    }

    public String addSqlAnnotationForCount(String orgSql) {

    	 String idAnnotation = " /*" + getMappedStatementIdForCount(true) + "*/ ";
        String[] searchStrs = new String[] { "select", "SELECT" };
        int startOperation = StringUtil.indexOfAny(orgSql, searchStrs);
        if (-1 != startOperation) {
            String operation = StringUtil.substring(orgSql, 0, startOperation + 6);
            String afterOperation = StringUtil.substring(orgSql, startOperation + 7, orgSql
                .length());
            orgSql = operation + idAnnotation + afterOperation;
        }
        return orgSql;
    }

    /**
     *
     * @return
     */
    public String getMappedStatementIdForCount(boolean needAppName) {
        return getMappedStatementId(needAppName) + "-COUNT-FOR-PAGING";
    }

    public String getMappedStatementIdForCount() {
        return getMappedStatementIdForCount(false);
    }

    /**
     * @return
     */
    public String getMappedStatementResultForCount() {
        // we use int because Paginator use int to store item count
        return "resultClass=\"int\"";
    }

    /**
     * @return
     */
    public String getParsedSqlForCount() {
        if (parsedSqlForCount == null) {
            ZQuery zst = (ZQuery) opConfig.getZst();
            ZQuery zstCount = new ZQuery();

            Vector select = new Vector();

            select.add(new ZSelectItem("count(*)"));
            zstCount.addSelect(select);

            zstCount.addFrom(zst.getFrom());
            zstCount.addWhere(zst.getWhere());

            // TODO: need to support more features? 
            parsedSqlForCount = zstCount.toString();
        }

        return parsedSqlForCount;
    }
}
