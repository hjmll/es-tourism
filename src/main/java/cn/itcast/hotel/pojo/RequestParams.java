package cn.itcast.hotel.pojo;

import lombok.Data;

/**
 * @Author: hjm
 * @Date: 2025/01/05/21:39
 * @Description: 请求参数实体类
 */
@Data
public class RequestParams {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String city;
    private String brand;
    private String starName;
    private String minPrice;
    private String maxPrice;
    private String location;
}
