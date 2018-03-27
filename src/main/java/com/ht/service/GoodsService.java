package com.ht.service;

import com.ht.vo.GoodsVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
public interface GoodsService {
    public List<GoodsVo> listGoodsVo();
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId")long goodsId);

    //减库存  需要判断减库存是否成功
    public boolean reduceStock(GoodsVo goods);

    public void resetStock(List<GoodsVo> goodsList);
}
