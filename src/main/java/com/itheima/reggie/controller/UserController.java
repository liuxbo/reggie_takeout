package com.itheima.reggie.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phoneNumber = user.getPhone();
        log.info(phoneNumber);
        if(StrUtil.isNotEmpty(phoneNumber)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码：{}",code);
            //调用阿里云API向用户手机发送验证码
            //SMSUtils.sendMessage("瑞吉外卖","SMS_270345500",phoneNumber,code);
            //将生成的验证码保存到session中
            session.setAttribute(phoneNumber,code);
            return R.success("手机验证码短信发送成功");
        }

        return R.error("短信发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        log.info(map.toString());
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        //从session中取出保存的验证码
        Object codeInSession = session.getAttribute(phone);
        //验证码比对
        if(codeInSession!=null&&codeInSession.equals(code)){
            //比对成功，登录成功
            //判断是否是新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            if(user == null){
                //是新用户,自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登陆失败");
    }
}
