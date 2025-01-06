package cn.itcast.hotel.pojo;

import lombok.Data;

import java.util.List;

/**
 * @Author: hjm
 * @Date: 2025/01/05/21:41
 * @Description: 搜索结果类
 */
@Data
public class PageResult {
    private Long total;
    private List<HotelDoc> hotels;
    public PageResult(Long total, List<HotelDoc> hotels)
    {
        this.total = total;
        this.hotels = hotels;
    }
    public PageResult() {}
}
