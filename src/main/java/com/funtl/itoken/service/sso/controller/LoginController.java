package com.funtl.itoken.service.sso.controller;


import com.funtl.itoken.common.domain.TbSysUser;
import com.funtl.itoken.common.utils.CookieUtils;
import com.funtl.itoken.common.utils.MapperUtils;
import com.funtl.itoken.service.sso.service.LoginService;
import com.funtl.itoken.service.sso.service.consumer.RedisService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class LoginController {


    @Qualifier("itoken-service-redis")
    @Autowired
    private RedisService redisService ;

    @Autowired
    private LoginService loginService ;

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login(HttpServletRequest request,
                        Model model,
                        @ RequestParam(required = false) String url){
        String token = CookieUtils.getCookieValue(request, "token") ;

        //token不为空，可能已经登录
        if (StringUtils.isNotBlank(token)) {
            String loginCode = redisService.get(token);
            if(StringUtils.isNotBlank(loginCode)){
                String json = redisService.get(loginCode) ;
                if (StringUtils.isNotBlank(json)){
                    try {
                        TbSysUser tbSysUser = MapperUtils.json2pojo(loginCode, TbSysUser.class) ;
                        if(tbSysUser != null){
                            if(StringUtils.isNotBlank(url)){
                                return "redirect:" + url ;
                            }
                        }
                        model.addAttribute("tbSysUser", tbSysUser) ;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return "login" ;
    }


    @RequestMapping(value = "login", method = RequestMethod.POST)
    public String login(@RequestParam(required = true) String loginCode,
                        @RequestParam(required = false) String url,
                        @RequestParam(required = true) String password,
                        HttpServletRequest request,HttpServletResponse response, RedirectAttributes redirectAttributes){

        TbSysUser tbSysUser = loginService.login(loginCode, password);
        if (tbSysUser == null){
            redirectAttributes.addFlashAttribute("message", "用户名或密码错误，请重新输入！" );
        }else{
            String token = UUID.randomUUID().toString() ;
            String result = redisService.put(token, loginCode, 60 * 60 * 24);
            if (tbSysUser != null && StringUtils.isNotBlank(result) && "ok".equals(result)){
                CookieUtils.setCookie(request, response,"token", token, 60 * 60 * 24);
                if (StringUtils.isNotBlank(url)){
                    return "redirect:" + url ;
                }
            }
            else{
                redirectAttributes.addFlashAttribute("message", "服务器异常，请稍后再试" );
            }
        }
        return "redirect:/login";

    }

}
