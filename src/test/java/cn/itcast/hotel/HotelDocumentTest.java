package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static cn.itcast.hotel.constants.HotelConstants.MAPPING_TEMPLATE;

@SpringBootTest
public class HotelDocumentTest {
    private RestHighLevelClient client;

    @Autowired
    private IHotelService hotelService;

    /**
     * 添加文档（对应mysql数据库中表格的一行记录）
     * 把数据库表中的一行记录数据存到ES的索引库中
     * @throws IOException
     */
    @Test
    void testAddDocument() throws IOException {
        Hotel hotel = hotelService.getById(61083L);
        // 转换为文档类型
        HotelDoc hotelDoc = new HotelDoc(hotel);
        //1.准备Request对象
        IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());

        //2.准备Json文档
        request.source(JSON.toJSONString(hotelDoc),XContentType.JSON);
        //3.发送请求
        client.index(request,RequestOptions.DEFAULT);
    }

    /**
     * 根据文档id获取文档
     * @throws IOException
     */
    @Test
    void testGetDocumentById() throws IOException {
        //准备request对象
        GetRequest request = new GetRequest("hotel","61083");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    /**
     * 更新文档，对字段进行增删改查
     * @throws IOException
     */
    @Test
    void testUpdateDocumentById() throws IOException {
        UpdateRequest request = new UpdateRequest("hotel", "61083");
        request.doc(
                "price","999",
                "starName","四钻",
                "dd","123"
        );
        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);

    }

    /**
     * 删除文档
     * @throws IOException
     */
    @Test
    void testDeleteDocumentById() throws IOException {
        DeleteRequest request = new DeleteRequest("hotel", "61083");
        client.delete(request, RequestOptions.DEFAULT);

    }
    @Test
    void testBulkRequest() throws IOException {
        List<Hotel> hotels = hotelService.list();
        for (Hotel hotel : hotels) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
        }
        BulkRequest request = new BulkRequest();
        for (Hotel hotel : hotels) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            request.add(new IndexRequest("hotel")
                    .id(hotel.getId().toString())
                    .source(JSON.toJSONString(hotelDoc),XContentType.JSON));
        }
        client.bulk(request,RequestOptions.DEFAULT);
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
