/*
 * Taobao.com Inc.
 * Copyright (c) 2000-2004 All Rights Reserved.
 */
package middlegen.plugins.iwallet.config;


/**
 * A bean class represents configuration concerning a column.
 *
 * <p>
 *
 * @author Cheng Li
 *
 * @version $Id: IWalletColumnConfig.java,v 1.1 2004/12/24 07:34:20 baobao Exp $
 */
public class IWalletColumnConfig {
    /** name of the column, it's the name in database */
    private String name;

    /** the java type of the column, used to override default mapping by middlegen */
    private String javaType;

    /**
     * Constructor for IWalletFieldConfig.
     */
    public IWalletColumnConfig() {
        super();
    }

    /**
     * @return
     */
    public String getJavaType() {
        return javaType;
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
    public void setJavaType(String string) {
        javaType = string;
    }

    /**
     * @param string
     */
    public void setName(String string) {
    	if (string != null) {
    		name = string.toLowerCase();
    	} else {
    		name = null;
    	}
    }

    /**
     * @return
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[name=").append(name).append(", javaType=").append(javaType).append("]");

        return sb.toString();
    }
}
