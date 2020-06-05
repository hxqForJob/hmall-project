package com.hmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hmall.manage.service.mapper.SpuImageMapper;
import com.hmall.manage.service.mapper.SpuInfoMapper;
import com.hmall.manage.service.mapper.SpuSaleAttrMapper;
import com.hmall.manage.service.mapper.SpuSaleAttrValueMapper;
import com.hmall.pojo.SpuImage;
import com.hmall.pojo.SpuInfo;
import com.hmall.pojo.SpuSaleAttr;
import com.hmall.pojo.SpuSaleAttrValue;
import com.hmall.service.SpuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * 商品信息Spu业务逻辑实现
 */
@Service
public class SpuInfoServiceImpl implements SpuInfoService {

    //注入商品信息数据访问
    @Autowired
    private SpuInfoMapper spuInfoMapper;

    //注入商品销售属性Mapper
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    //注入商品图片Mapper
    @Autowired
    private SpuImageMapper spuImageMapper;

    //注入商品销售属性值Mapper
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;




    //根据三级分类获取商品信息
    @Override
    public List<SpuInfo> getSpuInfoByCatalogId3(String cataLogId3) {
        Example example=new Example(SpuInfo.class);
        example.createCriteria().andEqualTo("catalog3Id",cataLogId3);
        List<SpuInfo> spuInfos = spuInfoMapper.selectByExample(example);
        return  spuInfos;
    }

    /**
     * 添加商品信息
     * @param spuInfo
     */
    @Override
    @Transactional
    public void addSpuInfo(SpuInfo spuInfo) {

        //SpuInfo(id=null, spuName=小米手机1, description=很好很漂亮, catalog3Id=61,
        // spuSaleAttrList=[SpuSaleAttr(id=null, spuId=null, saleAttrId=1, saleAttrName=颜色,
        //                  spuSaleAttrValueList=[SpuSaleAttrValue(id=null, spuId=null, saleAttrId=1, saleAttrValueName=岩石黑, isChecked=null), SpuSaleAttrValue(id=null, spuId=null, saleAttrId=1, saleAttrValueName=流光金, isChecked=null)]), SpuSaleAttr(id=null, spuId=null, saleAttrId=2, saleAttrName=版本, spuSaleAttrValueList=[SpuSaleAttrValue(id=null, spuId=null, saleAttrId=2, saleAttrValueName=4+32G, isChecked=null), SpuSaleAttrValue(id=null, spuId=null, saleAttrId=2,
        //                  saleAttrValueName=8+128G, isChecked=null)])], spuImageList=[SpuImage(id=null, spuId=null, imgName=黑背.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECAfPUIAACbuutfIxI484.jpg), SpuImage(id=null, spuId=null, imgName=黑正.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECAVUi5AAGxOgdhnng018.jpg), SpuImage(id=null, spuId=null, imgName=黑正背.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECASghlAAIXc1LGT24174.jpg), SpuImage(id=null, spuId=null, imgName=金背.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECAM4XzAADfnMDJkAM382.jpg), SpuImage(id=null, spuId=null, imgName=黑侧.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECAB-GlAAGNGWcf4Bg865.jpg), SpuImage(id=null, spuId=null, imgName=黑全.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECAaky7AAImKTV0RBw782.jpg), SpuImage(id=null, spuId=null, imgName=金侧.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECADeGbAAGr4YZ6yQs390.jpg), SpuImage(id=null, spuId=null, imgName=金全.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECAaOqEAAIsKfx4n_g934.jpg), SpuImage(id=null, spuId=null, imgName=金正.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECAOTeSAAG6_8yB2-c997.jpg), SpuImage(id=null, spuId=null, imgName=金正背.jpg, imgUrl=http://192.168.25.133/group1/M00/00/01/wKgZhV6ZRECANomlAAJJbuXt4Qg737.jpg)])
        //System.out.println(spuInfo);
        //添加商品基本信息
        spuInfoMapper.insertSelective(spuInfo);
        //添加图片
        for (SpuImage spuImage : spuInfo.getSpuImageList()) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
        }
        //添加销售属性
        for (SpuSaleAttr spuSaleAttr : spuInfo.getSpuSaleAttrList()) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insert(spuSaleAttr);
            //添加销售属性值
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttr.getSpuSaleAttrValueList()) {
                    spuSaleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }
        }
    }

    /**
     * 根据Spuid查询图片
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> spuImageList(String spuId) {
        SpuImage spuImage=new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }
}
