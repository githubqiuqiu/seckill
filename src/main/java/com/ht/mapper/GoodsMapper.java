package com.ht.mapper;

import com.ht.domain.SeckillGoods;
import com.ht.domain.SeckillOrder;
import com.ht.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
@Repository
@Mapper
public interface GoodsMapper {

    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.seckill_price from seckill_goods mg left join goods g on mg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.seckill_price from seckill_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId")long goodsId);

    /**
     * 解决超卖  当库存大于0的时候 才减库存
     * @param g
     * @return
     */
    @Update("update seckill_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
    public int reduceStock(SeckillGoods g);

    @Update("update seckill_goods set stock_count = #{stockCount} where goods_id = #{goodsId}")
    public int resetStock(SeckillGoods g);

}
