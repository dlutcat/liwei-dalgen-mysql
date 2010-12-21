/*
 * Taobao.com Inc.
 * Copyright (c) 2000-2004 All Rights Reserved.
 */
package middlegen.plugins.iwallet.operation;

import middlegen.Column;
import middlegen.Table;
import middlegen.plugins.iwallet.IWalletColumn;
import middlegen.plugins.iwallet.IWalletOperation;
import middlegen.plugins.iwallet.config.IWalletOperationConfig;
import middlegen.plugins.iwallet.util.DalUtil;

import org.apache.log4j.Logger;

import com.alibaba.common.lang.StringUtil;

/**
 * An implementation of insert operation decorator.
 *
 * @author Cheng Li
 *
 * @version $Id: IWalletInsert.java,v 1.2 2005/04/15 04:02:24 lei.shi Exp $
 */
public class IWalletInsert extends IWalletOperation {
    /** logger */
    public static final Logger logger = Logger.getLogger(IWalletInsert.class);
    public static final String OP_TYPE = "insert";

    /**
     * Constructor for IWalletInsert.
     */
    public IWalletInsert(IWalletOperationConfig opConfig) {
        super(opConfig);

        paramType     = PARAM_TYPE_OBJECT;

        multiplicity = MULTIPLICITY_ONE;
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.Operation#getReturnTypeName()
     */
    public String getSimpleReturnType() {
        return DalUtil.getSimpleJavaType(getReturnType());
    }

    public String getReturnType() {
        if (logger.isDebugEnabled()) {
//            logger.debug("Get return type for table: " + getTable().getSqlName());
        }
        
        if (getTable().getPkColumn() == null) {
            // add by zhaoxu 2007-10-26
            // 当无主键或多主键时，使用虚拟主键
            String dummyPk = opConfig.getTableConfig().getDummyPk();
            if (StringUtil.isNotBlank(dummyPk)) {
                Column dummyPkColumn = getTable().getColumn(dummyPk);
                if (dummyPkColumn != null) {
                    return ((IWalletColumn) dummyPkColumn).getJavaType();
                }
            }
        	throw new IllegalStateException(getTable().getSqlName() + "无主键或多主键，可在operation中指定虚拟主键dummypk。");
        } else {
            return ((IWalletColumn) getTable().getPkColumn()).getJavaType();
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
     * @param t
     *
     * @see middlegen.plugins.iwallet.IWalletOperation#setTable(middlegen.Table)
     */
    public void setTable(Table t) {
        super.setTable(t);
    }

    /**
     * @return
     */
    public String getMappedStatementType() {
        return OP_TYPE;
    }
}
