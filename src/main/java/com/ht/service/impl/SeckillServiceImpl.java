package com.ht.service.impl;

import com.ht.domain.OrderInfo;
import com.ht.domain.SeckillOrder;
import com.ht.domain.SeckillUser;
import com.ht.redis.RedisService;
import com.ht.redis.SeckillKey;
import com.ht.redis.SeckillUserKey;
import com.ht.service.GoodsService;
import com.ht.service.OrderService;
import com.ht.service.SeckillService;
import com.ht.util.MD5Util;
import com.ht.util.UUIDUtil;
import com.ht.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

/**
 * @auth Qiu
 * @time 2018/3/23
 **/
@Service
@Transactional
public class SeckillServiceImpl implements SeckillService{
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;


    @Override
    public OrderInfo miaosha(SeckillUser user, GoodsVo goods) {
        //减库存 下订单
        boolean isreduce=goodsService.reduceStock(goods);
        if(isreduce){
            //创建订单
            return orderService.createOrder(user, goods);
        }else {
            //设置商品已经秒杀完了
            setGoodsOver(goods.getId());
            return  null;
        }
    }


    /**
     * 轮询判断用户是否秒杀成功
     * @param userId 用户id
     * @param goodsId 商品id
     * @return
     */
    @Override
    public long getMiaoshaResult(Long userId, long goodsId) {
        SeckillOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if(order != null) {//秒杀成功
            return order.getOrderId();
        }else {
            //判断商品是否已经秒杀完了
            boolean isOver = getGoodsOver(goodsId);
            if(isOver) {
                //秒杀完了返回-1
                return -1;
            }else {
                //没有卖完 继续轮询
                return 0;
            }
        }
    }

    /**
     * 设置已经秒杀完的商品id 到redis中
     * @param goodsId
     */
    @Override
    public void setGoodsOver(Long goodsId) {
        redisService.set(SeckillKey.isGoodsOver, ""+goodsId, true);
    }

    /**
     * 根据商品的id  从redis里获取商品的库存
     * @param goodsId
     * @return
     */
    @Override
    public boolean getGoodsOver(long goodsId) {
        return redisService.exists(SeckillKey.isGoodsOver, ""+goodsId);
    }

    /**
     * 重置库存
     * @param goodsList
     */
    @Override
    public void reset(List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }

    /**
     * 验证秒杀接口的path 参数是否正确
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    @Override
    public boolean checkPath(SeckillUser user, long goodsId, String path) {
        if(user == null || path == null) {
            return false;
        }
        //从redis中获取 之前保存的path参数
        String pathOld = redisService.get(SeckillKey.getSeckillPath, ""+user.getId() + "_"+ goodsId, String.class);
        //把得到的path参数和redis的path参数做比较
        return path.equals(pathOld);
    }

    /**
     * 生成秒杀接口的 path参数
     * @param user 用户信息
     * @param goodsId 商品id
     * @return
     */
    @Override
    public String createMiaoshaPath(SeckillUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        //随机生成一个uuid 并且把uuid用md5加密
        String str = MD5Util.md5(UUIDUtil.uuid()+"123456");
        //把生成的path参数设置到redis中  过期时间为1分钟
        redisService.set(SeckillKey.getSeckillPath, ""+user.getId() + "_"+ goodsId, str);
        return str;
    }

    /**
     * 创建一个图片验证码
     * @param user 用户信息
     * @param goodsId 商品id
     * @return
     */
    @Override
    public BufferedImage createVerifyCode(SeckillUser user, long goodsId) {
        //先验证用户信息和 商品id
        if(user == null || goodsId <=0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image 创建一个图形对象
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color 设置背景颜色
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        //生成验证码  实际上是一个表达式
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中  先计算 把计算后的值存到redis中
        int rnd = calc(verifyCode);
        redisService.set(SeckillKey.getSeckillVerifyCode, user.getId()+"_"+goodsId, rnd);
        //输出图片
        return image;
    }

    //定义运算符
    private static char[] ops = new char[] {'+', '-', '*'};

    /**
     * 生成验证码
     * + - *
     * */
    private String generateVerifyCode(Random rdm) {
        //随机生成3个数字  随机做加减乘
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        //随机生成两个 运算符
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    //根据string类型的公式  计算结果
    private static int calc(String exp) {
        try {
            //ScriptEngineManager实现字符串公式灵活计算
            ScriptEngineManager manager = new ScriptEngineManager();
            //利用JavaScript 的引擎
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 验证 验证码是否匹配
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    public boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode) {
        //先判断用户和商品id
        if(user == null || goodsId <=0) {
            return false;
        }
        //从redis里获取 验证码的值
        Integer codeOld = redisService.get(SeckillKey.getSeckillVerifyCode, user.getId()+"_"+goodsId, Integer.class);
        //判断验证码是否相等  如果不相等
        if(codeOld == null || codeOld - verifyCode != 0 ) {
            return false;
        }
        //判断完之后 删除这个redis的值  相等的情况删掉这个key
        redisService.delete(SeckillKey.getSeckillVerifyCode, user.getId()+"_"+goodsId);
        return true;
    }



}
