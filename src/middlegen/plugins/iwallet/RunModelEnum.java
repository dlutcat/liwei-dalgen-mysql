/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2008 All Rights Reserved.
 */
package middlegen.plugins.iwallet;

import com.alibaba.common.lang.StringUtil;

/**
 * 运行模式
 * 
 * @author xu.zhaox
 * @version $Id: RunModelEnum.java, v 0.1 2008-1-24 下午03:50:58 xu.zhaox Exp $
 */
public enum RunModelEnum {

    ORGI, // 原始
    SOFA, // SOFA平台
    COMPARE; // 比较
    
    public static RunModelEnum getEnumByName(String name) {
        for (RunModelEnum item : RunModelEnum.values()) {
            if (StringUtil.equalsIgnoreCase(item.name(), name)) {
                return item;
            }
        }
        
        return null;
    }
}
