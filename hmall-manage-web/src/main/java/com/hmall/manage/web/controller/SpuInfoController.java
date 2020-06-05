package com.hmall.manage.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hmall.pojo.SpuImage;
import com.hmall.pojo.SpuInfo;
import com.hmall.service.SpuInfoService;
import com.hmall.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
public class SpuInfoController {


    //fastDfs服务器地址
    @Value("${fastdfs.server}")
    private  String fastDfsServer;

    /**
     * 注入商品spu业务逻辑
     */
    @Reference
    private SpuInfoService spuInfoService;

    /**
     * 根据三级分类获取商品信息
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/spuList")
    public List<SpuInfo> getSpuList(String catalog3Id){
        List<SpuInfo> spuInfos = spuInfoService.getSpuInfoByCatalogId3(catalog3Id);
        return  spuInfos;
    }

    //添加spu信息
    @RequestMapping("/saveSpuInfo")
    public  void  saveSpuInfo(@RequestBody SpuInfo spuInfo){
        spuInfoService.addSpuInfo(spuInfo);
    }

    /**
     * 上传图片到fastdfs
     * @param file
     * @return
     */
    @RequestMapping("/fileUpload")
    public  String fileUpload(MultipartFile file)
    {
        FastDFSClient dfsClient= null;
        String uploadFile=fastDfsServer;
        try {
            dfsClient = new FastDFSClient("classpath:config/fastdfsClient.conf");
            String extName=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
            uploadFile+= dfsClient.uploadFile(file.getBytes(), extName);
        } catch (Exception e) {
            uploadFile=null;
            e.printStackTrace();
        }finally {
            return  uploadFile;
        }
    }

    /**
     * 根据spuid查询spu图片
     * @param spuId
     * @return
     */
    @RequestMapping("/spuImageList")
    public  List<SpuImage> spuImageList(String spuId)
    {
       return spuInfoService.spuImageList(spuId);
    }
}
