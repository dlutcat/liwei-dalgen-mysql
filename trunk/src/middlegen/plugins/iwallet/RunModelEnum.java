/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2008 All Rights Reserved.
 */
package middlegen.plugins.iwallet;

import com.alibaba.common.lang.StringUtil;

/**
 * ����ģʽ
 * 
 * @author xu.zhaox
 * @version $Id: RunModelEnum.java, v 0.1 2008-1-24 ����03:50:58 xu.zhaox Exp $
 */
public enum RunModelEnum {

    ORGI, // ԭʼ
    SOFA, // SOFAƽ̨
    COMPARE; // �Ƚ�
    
    public static RunModelEnum getEnumByName(String name) {
        for (RunModelEnum item : RunModelEnum.values()) {
            if (StringUtil.equalsIgnoreCase(item.name(), name)) {
                return item;
            }
        }
        
        return null;
    }
}
