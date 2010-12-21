/*
 * Taobao.com Inc.
 * Copyright (c) 2000-2004 All Rights Reserved.
 */
package middlegen.plugins.iwallet.operation;

import middlegen.Table;
import middlegen.plugins.iwallet.IWalletOperation;
import middlegen.plugins.iwallet.config.IWalletOperationConfig;

import org.apache.log4j.Logger;

/**
 * An implementation of "update" operation.
 *
 * @author Cheng Li
 *
 * @version $Id: IWalletUpdate.java,v 1.1 2004/12/24 07:34:20 baobao Exp $
 */
public class IWalletUpdate extends IWalletOperation {
    /** logger */
    private static final Logger logger = Logger.getLogger(IWalletUpdate.class);
    public static final String  OP_TYPE = "update";

    /**
     * Constructor for IWalletUpdate.
     */
    public IWalletUpdate(IWalletOperationConfig conf) {
        super(conf);

        if (PARAM_TYPE_OBJECT.equals(conf.getParamType())) {
            paramType = PARAM_TYPE_OBJECT;
        } else {
            // default
            paramType = PARAM_TYPE_PRIMITIVE;
        }

        // default
        multiplicity = MULTIPLICITY_ONE;
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.Operation#getReturnType()
     */
    public String getReturnType() {
        return "int";
    }

    /**
     * @return
     *
     * @see middlegen.plugins.iwallet.Operation#getSimpleReturnType()
     */
    public String getSimpleReturnType() {
        return "int";
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
