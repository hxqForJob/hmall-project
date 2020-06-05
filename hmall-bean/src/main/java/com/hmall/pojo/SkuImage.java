package com.hmall.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

//sku图片
@Data
public class SkuImage implements Serializable {

    @Column
    @Id
    private  String id; //主键
    @Column
    private String skuId; //skuId
    @Column
    private String imgName;//图片名称
    @Column
    private String imgUrl;//图片路径
    @Column
    private String spuImgId;//spu下对应图片的Id
    @Column
    private  String isDefault;//是否默认 1 默认 0不默认
}
