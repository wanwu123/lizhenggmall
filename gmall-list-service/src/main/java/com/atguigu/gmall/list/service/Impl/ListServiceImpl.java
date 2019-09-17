package com.atguigu.gmall.list.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gamll.service.ListService;
import com.atguigu.gmall.entity.SkuLsInfo;
import com.atguigu.gmall.entity.SkuLsParams;
import com.atguigu.gmall.entity.SkuLsResult;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
@Service
public class ListServiceImpl implements ListService {
    @Override
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams) {
        String query = null;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(new MatchQueryBuilder("skuName",skuLsParams.getKeyword()));
        boolQueryBuilder.filter(new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id()));
        String[] valueIds = skuLsParams.getValueId();
        for (int i = 0; i < valueIds.length; i++) {
            String valueId = valueIds[i];
            boolQueryBuilder.filter(new TermQueryBuilder("skuAttrValueList.valueId",valueId));
        }
        boolQueryBuilder.filter(new RangeQueryBuilder("price").gte("1800"));
        searchSourceBuilder.query(boolQueryBuilder);
        int form = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(form);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red' >").postTags("</span>"));
        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        //聚合
        TermsBuilder groupby_valueId = AggregationBuilders.terms("groupby_valueId").field("skuAttrValueList.valueId").size(1000);
        searchSourceBuilder.aggregation(groupby_valueId);

        Search.Builder builder = new Search.Builder(searchSourceBuilder.toString());
        Search search = builder.addIndex("gmall_sku_info").addType("doc").build();
        try {
            jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Autowired
    private JestClient jestClient;

    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo)  {
        Index.Builder builder = new Index.Builder(skuLsInfo);
        builder.index("gmall_sku_info").type("doc").id(skuLsInfo.getId());
        Index build = builder.build();
        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
