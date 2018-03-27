package com.ht.controller;

import com.ht.domain.SeckillUser;
import com.ht.redis.GoodsKey;
import com.ht.redis.RedisService;
import com.ht.result.Result;
import com.ht.service.GoodsService;
import com.ht.service.SeckillUserService;
import com.ht.vo.GoodsDetailVo;
import com.ht.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @auth Qiu
 * @time 2018/3/22
 **/
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    /**
     * 前端框架渲染
     */
    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    GoodsService goodsService;



    /**
     * 登录成功后 跳转的页面
     * @param model
     * @param seckillUser 使用了UserArgumentResolver 类的参数解析器
     * @return
     */
    @RequestMapping(value = "/list", produces="text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser seckillUser) {

        model.addAttribute("user", seckillUser);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if(!StringUtils.isEmpty(html)) {//如果缓存里有值 就从缓存中取值
            return html;
        }

        /**
         * 页面缓存
         */

        //如果缓存中没值 就手动渲染页面
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);
        //return "goods_list";
        SpringWebContext ctx = new SpringWebContext(request,response,
                request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
        //手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if(!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }
    /**
     * 登录成功后 跳转的页面
     * @param model
     * @param cookietoken cookie里面的token  根据这个token 从redis里面获取用户的信息
     * @param paramtoken 有可能手机端 会通过参数的形式传递token 获取参数传递token的形式
     * @return
     */
    /*@RequestMapping("/list")
    public String list(Model model,
                       HttpServletResponse response,
                       @CookieValue(value = SeckillServiceImpl.COOKI_NAME_TOKEN,required = false)String cookietoken,
                       @RequestParam(value= SeckillServiceImpl.COOKI_NAME_TOKEN,required = false)String paramtoken) {

        //如果两种方式获取token都为空  说明cookie过期了  跳回登录页面
        if(StringUtils.isEmpty(cookietoken)&&StringUtils.isEmpty(paramtoken)){
            return "login";
        }

        //比较两种token的优先级  参数token 优先  如果参数token为空 则取cookietoken
        String token=StringUtils.isEmpty(paramtoken)?cookietoken:paramtoken;

        //调用从token中获取redis值的方法
        SeckillUser seckillUser=seckillService.getSessionByToken(response,token);

        model.addAttribute("user", seckillUser);
        return "goods_list";
    }*/

    /**
     * 跳转到秒杀商品详情页  把整个页面数据 写进redis中
     * @param model
     * @param user
     * @param goodsId 商品的id
     * @return
     */
    @RequestMapping(value = "/to_detailRedis/{goodsId}", produces="text/html")
    @ResponseBody
    public String to_detailRedis(HttpServletRequest request, HttpServletResponse response,Model model,SeckillUser user,
                         @PathVariable("goodsId")long goodsId) {

        //取缓存  URL级的缓存  因为每个页面的商品id不同  所以详情也不同  本质也是把整个页面缓存起来
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        if(!StringUtils.isEmpty(html)) {
            return html;
        }

        //手动渲染
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        //秒杀开始时间
        long startAt = goods.getStartDate().getTime();
        //秒杀结束时间
        long endAt = goods.getEndDate().getTime();
        //当前时间
        long now = System.currentTimeMillis();

        //秒杀的状态 0是未开始 1是进行中  2是已结束
        int miaoshaStatus = 0;
        //剩余开始时间
        int remainSeconds = 0;

        //判断秒杀的时间有没有开始
        if(now < startAt ) {//秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("user", user);
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        SpringWebContext ctx = new SpringWebContext(request,response,
                request.getServletContext(),request.getLocale(), model.asMap(), applicationContext );
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
        if(!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
        }
        return html;
    }


    /**
     * 商品详情页 静态化   直接获取数据到页面渲染
     * @param request
     * @param response
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model,SeckillUser user,
                                        @PathVariable("goodsId")long goodsId) {
        //根据商品的id  查询秒杀商品的信息
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if(now < startAt ) {//秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        return Result.success(vo);
    }
}
