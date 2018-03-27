package com.ht.redis;

/**
 * @auth Qiu
 * @time 2018/3/23
 **/
public class GoodsKey extends  BasePrefix{
    private GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    //60秒失效 商品列表
    public static GoodsKey getGoodsList = new GoodsKey(60, "goodsList");
    //60秒失效 商品详情
    public static GoodsKey getGoodsDetail = new GoodsKey(60, "goodsDetail");

    //永久保存  商品库存
    public static GoodsKey getSeckillGoodsStock= new GoodsKey(0, "goodsStock_");

}
