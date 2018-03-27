package com.ht.service;

import com.ht.domain.OrderInfo;
import com.ht.domain.SeckillUser;
import com.ht.vo.GoodsVo;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @auth Qiu
 * @time 2018/3/23
 **/
public interface SeckillService {

    //秒杀商品的方法
    public OrderInfo miaosha(SeckillUser user, GoodsVo goods);

    //根据用户id和商品id 查询是否有秒杀订单
    public long getMiaoshaResult(Long userId, long goodsId) ;

    //根据商品id 设置到redis中商品已卖完 库存为0
    public void setGoodsOver(Long goodsId) ;
    //根据商品id 从redis中获取商品是否卖完
    public boolean getGoodsOver(long goodsId);
    //重置商品的库存
    public void reset(List<GoodsVo> goodsList);

    //检查 秒杀接口的path是否正确
    public boolean checkPath(SeckillUser user, long goodsId, String path);

    //创建一个秒杀接口的 path
    public String createMiaoshaPath(SeckillUser user, long goodsId);

    //创建一个图片验证码
    public BufferedImage createVerifyCode(SeckillUser user, long goodsId);

    //验证 图片验证码是否正确
    public boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode);

}
