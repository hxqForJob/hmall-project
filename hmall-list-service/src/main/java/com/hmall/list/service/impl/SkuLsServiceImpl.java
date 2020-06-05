package com.hmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hmall.pojo.SkuLsInfo;
import com.hmall.pojo.SkuLsParams;
import com.hmall.pojo.SkuLsResult;
import com.hmall.service.SkuLsService;
import com.hmall.service.util.redisConfig.RedisKeyUtil;
import com.hmall.service.util.redisConfig.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品列表业务逻辑
 */
@Service
public class SkuLsServiceImpl implements SkuLsService {

    /**
     * 定义ES库
     */
    public static final String ES_INDEX="hmall_index";

    /**
     * 定义Es表
     */
    public static final String ES_TYPE="SkuInfo";


    /**
     * 注入jestClient,方便es查询
     */
    @Autowired
    private JestClient jestClient;

    /**
     * 注入Redis工具类
     */
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 查询sku
     * @param skuLsParams
     * @return
     */
    @Override
    public SkuLsResult getSkuLsResult(SkuLsParams skuLsParams) {
        SkuLsResult skuLsResult=null;
        //创建查询语句
        String queryStr=makeQueryStr(skuLsParams);
        Search search = new Search.Builder(queryStr).addIndex(ES_INDEX).addType(ES_TYPE).build();
        try {
            //执行dsl语句
            SearchResult searchResult = jestClient.execute(search);
            //将es查询的结果封装成自定义结果的javabean
            skuLsResult=makeSkuLsResult(skuLsParams,searchResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  skuLsResult;

    }

    /**
     * 添加商品访问量
     * @param skuId
     */
    @Override
    public void addWatch(Integer skuId) {
        String key= RedisKeyUtil.SKUINFO_PREFIX+skuId+RedisKeyUtil.SKUINFOHOT_POSTFIX;
        Jedis jedis = null;
        try {
            jedis=redisUtil.getJedis();
            Double num = jedis.zincrby(key, 1, skuId.toString());
            if(num%10==0){
                //每增加10次更新一次Es中的数据
                addHotByEs(skuId,Math.floor(num));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    /**
     * 根据Es中对应Sku的热度
     * @param skuId
     * @param floor
     */
    private void addHotByEs(Integer skuId, double floor) {
       String dslStr="{\n" +
               "   \"doc\": {\n" +
               "     \"hotScore\":"+floor+"\n" +
               "   }\n" +
               " }";
        Update update = new Update.Builder(dslStr).index(ES_INDEX).type(ES_TYPE).id(skuId.toString()).build();
        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 制作结果
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    private SkuLsResult makeSkuLsResult(SkuLsParams skuLsParams, SearchResult searchResult) {
        SkuLsResult skuLsResult=new SkuLsResult();
        //设置总数
        skuLsResult.setTotal(searchResult.getTotal());
        //取记录个数并计算出总页数
        long totalPage= (searchResult.getTotal() + skuLsParams.getPageSize() -1) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPage);
        //获取结果集
        List<SkuLsInfo> skuLsInfoList=new ArrayList<>();
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        if(hits!=null&&hits.size()>0){
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
               SkuLsInfo skuLsInfo= hit.source;
               //判断是否有高亮字段
                if(hit.highlight!=null&&hit.highlight.size()>0){
                    //将SkuName设置高亮
                    String skuName = hit.highlight.get("skuName").get(0);
                    skuLsInfo.setSkuName(skuName);
                }

                skuLsInfoList.add(skuLsInfo);

            }
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoList);
        List<String> attrValueIdList=new ArrayList<>();
        //统计当前查询包含的平台属性值Id
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation termsAggregation = aggregations.getTermsAggregation("groupby_attr");
        if(termsAggregation!=null){
            List<TermsAggregation.Entry> buckets = termsAggregation.getBuckets();
            if(buckets!=null&&buckets.size()>0){
                for (TermsAggregation.Entry bucket : buckets) {
                    attrValueIdList.add(bucket.getKey());
                }
            }
        }
        skuLsResult.setAttrValueIdList(attrValueIdList);
        return  skuLsResult;
    }

    /**
     * 创建查询语句
     * @param skuLsParams
     * @return
     */
    private String makeQueryStr(SkuLsParams skuLsParams) {
        /**
         * 类似于dsl的{}
         */
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //判断catalog3Id是否为空
        if(!StringUtils.isEmpty(skuLsParams.getCatalog3Id())){
            //添加catalog3Id过滤条件
            TermQueryBuilder termQueryBuilder=new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //判断条件是否含有平台属性值Id
        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            for (String attrValueId : skuLsParams.getValueId()) {
                //添加平台属性值过滤条件
                TermQueryBuilder termQueryBuilder=new TermQueryBuilder("skuAttrValueList.valueId",attrValueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }
        //判断关键词是否为空
        if(!StringUtils.isEmpty(skuLsParams.getKeyword())){
            MatchQueryBuilder matchQueryBuilder=new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
            //设置高亮
            HighlightBuilder highlightBuilder=new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            sourceBuilder.highlight(highlightBuilder);
        }
        //添加过滤和查询条件
        sourceBuilder.query(boolQueryBuilder);
        //排序
        if(skuLsParams.getOrderBy().equals("price")){
            //价格排序
            if(skuLsParams.getOwp()==1){
                //升序
                sourceBuilder.sort(skuLsParams.getOrderBy(), SortOrder.ASC);
            }else {
                //降序
                sourceBuilder.sort(skuLsParams.getOrderBy(), SortOrder.DESC);
            }
        }else {
            //其他排序
            sourceBuilder.sort(skuLsParams.getOrderBy(), SortOrder.DESC);
        }
        //分页
        int form = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        sourceBuilder.from(form);
        sourceBuilder.size(skuLsParams.getPageSize());
        // 设置聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        sourceBuilder.aggregation(groupby_attr);
        String query = sourceBuilder.toString();
        //System.out.println(query);
        return  query;
    }
}
