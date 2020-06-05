package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

//商品图片
@Data
public class SpuImage  implements Serializable {
    @Column
    @Id
    private String id; //id
    @Column
    private String spuId; //商品Id
    @Column
    private String imgName; //图片介绍
    @Column
    private String imgUrl; //图片路径
}

