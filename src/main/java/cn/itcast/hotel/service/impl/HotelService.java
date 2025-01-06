package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
    @Autowired
    private RestHighLevelClient client;
    @Override
    public PageResult search(RequestParams params) {
        try {
            SearchRequest request = new SearchRequest("hotel");
            buildBasicQuery(params, request);

            Integer page = params.getPage();
            Integer size = params.getSize();
            request.source().from((page - 1) * size).size(size);


            //排序
            String location = params.getLocation();
            if (location != null && !"".equals(location)){
                request.source().sort(SortBuilders
                        .geoDistanceSort("location",new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS));
            }
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildBasicQuery(RequestParams params, SearchRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //关键字搜索
        String key = params.getKey();
        if (key == null || "".equals(key)){
            boolQuery.must(QueryBuilders.matchAllQuery());
        }else {
            boolQuery.must(QueryBuilders.matchQuery("all",key));
        }
        //条件过滤 不参与算分
        //城市条件
        if (params.getCity() != null && !"".equals(params.getCity())){
            boolQuery.filter(QueryBuilders.termQuery("city", params.getCity()));
        }
        //品牌条件
        if (params.getBrand() != null && !"".equals(params.getBrand())){
            boolQuery.filter(QueryBuilders.termQuery("brand", params.getBrand()));
        }

        if (params.getStarName() != null && !"".equals(params.getStarName())){
            boolQuery.filter(QueryBuilders.termQuery("starName", params.getStarName()));
        }

        //价格
        if (params.getMinPrice() != null && !"".equals(params.getMinPrice())){
            boolQuery.filter(QueryBuilders
                    .rangeQuery("price").gte(params.getMinPrice()).lte(params.getMaxPrice()));
        }
        request.source().query(boolQuery);
    }

    private PageResult handleResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        long total = searchHits.getTotalHits().value;
        SearchHit[] hits = searchHits.getHits();
        List<HotelDoc> hotels = new ArrayList<>();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            hotels.add(hotelDoc);
        }
        //封装返回
        return new PageResult(total,hotels);
    }
}
