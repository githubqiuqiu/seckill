package com.ht.controller;

import com.ht.annotation.AccessLimit;
import com.ht.domain.OrderInfo;
import com.ht.domain.SeckillOrder;
import com.ht.domain.SeckillUser;
import com.ht.rabbitmq.MQSender;
import com.ht.rabbitmq.SeckillMessage;
import com.ht.redis.GoodsKey;
import com.ht.redis.RedisService;
import com.ht.result.CodeMsg;
import com.ht.result.Result;
import com.ht.service.GoodsService;
import com.ht.service.OrderService;
import com.ht.service.SeckillService;
import com.ht.service.SeckillUserService;
import com.ht.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * 秒杀功能 controller
 * @auth Qiu
 * @time 2018/3/23
 **/
@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    //定义一个map  设置一个内存标记  目的是为了减少访问redis 获取商品的库存
    private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();


    /**
     * 系统初始化时  回调该方法  实现InitializingBean
     * 系统初始化的时候  就把所有商品的库存写到redis里面去了
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //查询所有商品的信息
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if(goodsList == null) {
            return;
        }
        //循环把商品的库存信息 写到redis中
        for(GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getSeckillGoodsStock, ""+goods.getId(), goods.getStockCount());
            //设置一个内存标记  目的是为了减少访问redis 获取商品的库存 key为商品的id
            localOverMap.put(goods.getId(), false);
        }
    }



    /**
     * 秒杀商品
     * 把之前的同步下单  通过RabbitMQ改成异步下单
     * 优化:1.系统初始化的时候  把商品的数据 写到redis里面去
     * 2.收到请求后 从redis里面保存的库存预减库存  判断库存是否足够
	 * 3.如果库存足够 就会把该消息请求入队  返回的消息既不是成功  也不是失败 而是正在排队中
	 * 4.请求出队 生成订单 减少库存
	 * 5.客户端轮询 判断是否秒杀成功
     * 加上了 path 属性  秒杀接口就不能直接访问了
     * 防止有人在未开始秒杀之前 提前知道了秒杀接口 恶意访问
     * GET 和 POST 的区别: GET是幂等的
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value="/{path}/do_seckill", method= RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model,SeckillUser user,
                                   @RequestParam("goodsId")long goodsId,
                                   @PathVariable("path") String path) {
        //设置用户信息
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证path  通过返回true
        boolean check = seckillService.checkPath(user, goodsId, path);
        if(!check){
            //如果验证不通过  抛出请求非法异常
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        //内存标记，减少redis访问  根据id查询该 map
        boolean over = localOverMap.get(goodsId);
        if(over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //预减库存  减少redis里的值
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, ""+goodsId);//10
        if(stock < 0) {//库存小于0 说明没库存了 秒杀失败
            //如果库存不足了  修改内存标记为 true
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //判断是否已经秒杀到了
        SeckillOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        //已经秒杀成功 不需要重复秒杀
        if(order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        /**
         * 消息队列 注意要建一个 交换机 Exchange 绑定这个队列
         */

        //入队   没有重复秒杀的话  可以把消息入队 消息就是 秒杀的对象信息 和秒杀的商品id
        SeckillMessage seckillMessage=new SeckillMessage();
        //设置秒杀对象和秒杀的商品id
        seckillMessage.setUser(user);
        seckillMessage.setGoodsId(goodsId);

        //发送一个秒杀商品的信息
        //注意 此时的判断库存等逻辑 已经放到 接收消息的时候做了 所以这里只需要把消息放到队列就可以了 剩下的事情 接收端会做
        mqSender.sendSeckillMessage(seckillMessage);

        //返回一个 排队中
        return Result.success(0);
        /*
         优化前的秒杀代码
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了  这个地方有优化  原来是从数据库查  现在变成从缓存中查询是否该用户已经秒杀过了该商品
        SeckillOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = seckillService.miaosha(user, goods);
        return Result.success(orderInfo);
        */
    }


    /**
     * 轮询判断用户是否秒杀成功
     * orderId：成功  成功返回订单id
     * -1：秒杀失败   库存不足
     * 0： 排队中
     * */
    @RequestMapping(value="/result", method=RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model,SeckillUser user,
                                      @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //根据用户的id 和商品的id 查询一下 是否有订单生成
        long result  =seckillService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }


    /**
     * 在访问秒杀接口 之前  生成一个 path
     * @param request
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    //@AccessLimit 自定义注解 第一个参数是设定时间  第二个参数是设定次数 第三个参数是 用户是否需要登录
    @AccessLimit(seconds=15, maxCount=5, needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, SeckillUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value="verifyCode", defaultValue="0")int verifyCode){
        //先判断用户是否为空
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证 图片验证码的信息是否正确
        boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
        if(!check) {
            //如果秒杀成功了 会把redis里的验证码移除
            //如果回退到上一个页面 再次点击秒杀的时候 会判断验证码错误或者已使用 不会判断到订单重复秒杀
            //当重新开一个页面点击秒杀的时候 如果验证码正确 这个地方会通过 但是到了秒杀逻辑里面 会判断到订单重复秒杀
            return Result.error(CodeMsg.VERIFYCODE_ERROR);
        }
        //验证码正确 才会去生成 秒杀接口的 path

        //创建一个秒杀接口的 path参数  返回path参数保存在redis里面的值
        String path  =seckillService.createMiaoshaPath(user, goodsId);
        return Result.success(path);
    }


    /**
     *
     * 创建图片验证码
     * @param response
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response, SeckillUser user,
                                              @RequestParam("goodsId")long goodsId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            //创建一个验证码  创建验证码的时候  就把值写入到 redis里面了 所以每次刷新  redis里的值也跟着改变了
            BufferedImage image  = seckillService.createVerifyCode(user, goodsId);
            //写入到页面
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            //因为图片是通过流的形式写入页面的 所以返回null就可以了
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }


}
