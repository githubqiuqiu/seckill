package com.ht.service.impl;

import com.ht.domain.SeckillGoods;
import com.ht.mapper.GoodsMapper;
import com.ht.service.GoodsService;
import com.ht.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService{

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public List<GoodsVo> listGoodsVo() {
        return goodsMapper.listGoodsVo();
    }


    @Override
    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsMapper.getGoodsVoByGoodsId(goodsId);
    }

    /**
     * 减商品的库存
     * 需要根据返回值判断是否减库存成功
     * @param goods
     */
    @Override
    public boolean reduceStock(GoodsVo goods) {
        SeckillGoods g = new SeckillGoods();
        g.setGoodsId(goods.getId());
        int i=goodsMapper.reduceStock(g);
        //判断修改条数是否大于0  大于0表示修改成功了
        return i>0;
    }

    @Override
    public void resetStock(List<GoodsVo> goodsList) {
        for(GoodsVo goods : goodsList ) {
            SeckillGoods g = new SeckillGoods();
            g.setGoodsId(goods.getId());
            g.setStockCount(goods.getStockCount());
            goodsMapper.resetStock(g);
        }
    }
}
