package com.hmall.pojo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 用来封装经过查询后得到Sku的结果
 */
@Data
public class SkuLsResult  implements Serializable {

    /**
     * skuLsInfo集合
     */
    private List<SkuLsInfo> skuLsInfoList;

    /**
     * 总数
     */
    private long total;

    /**
     * 总页数
     */
    private long totalPages;

    /**
     * 平台属性值Id
     */
    private List<String> attrValueIdList;

}
