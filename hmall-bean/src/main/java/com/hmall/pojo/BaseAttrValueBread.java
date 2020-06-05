package com.hmall.pojo;

import lombok.Data;

/**
 * 平台属性面包屑
 */
@Data
public class BaseAttrValueBread {

    private  String attrInfo; //平台属性和平台属性值，例如（手机存储：64G）
    private  String urlParam;//删除面包屑后拼接对应的url参数
}
