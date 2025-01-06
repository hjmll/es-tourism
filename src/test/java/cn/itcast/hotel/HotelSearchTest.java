package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
public class HotelSearchTest {
    private RestHighLevelClient client;
    @Test
    void testMatchAll() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchAllQuery());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }
    @Test
    void testMatch() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("name","如家"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }
    @Test
    void testBool() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("city","上海"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(250));
        request.source().query(boolQueryBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);

    }
    @Test
    void testPageAndSort() throws IOException {
        int page = 1;
        int size = 5;
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().sort("price", SortOrder.ASC);
        request.source().from((page - 1) * size).size(5);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);

    }

    @Test
    void testHighlight() throws IOException {

        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);

    }

    private static void handleResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        long total = searchHits.getTotalHits().value;
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();

            if (!CollectionUtils.isEmpty(highlightFields)){
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField != null){
                    String string = highlightField.getFragments()[0].string();
                    hotelDoc.setName(string);
                }

            }
            System.out.println(hotelDoc);
        }
    }

    @BeforeEach
    void setUp(){
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.71.128:9200")
        ));
    }
    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }
}
