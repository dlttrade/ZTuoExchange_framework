package cn.ztuo.bitrade.aspect;

import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 登录之后发送邮件或者短信频率最快也只能一分钟一次
 *
 * @author GuoShuai
 * @date 2018年04月03日
 */
@Aspect
@Component
@Slf4j
public class AntiAttackAspect {
    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * cn.ztuo.bitrade.controller.RegisterController.sendBindEmail(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.RegisterController.sendAddAddress(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.SmsController.sendResetTransactionCode(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.SmsController.setBindPhoneCode(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.SmsController.updatePasswordCode(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.SmsController.addAddressCode(..))" +
            "||execution(public * cn.ztuo.bitrade.controller.SmsController.resetPhoneCode(..))")
    public void antiAttack() {
    }

    @Before("antiAttack()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        log.info("❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤❤");
        check(joinPoint);
    }

    public void check(JoinPoint joinPoint) throws Exception {
        startTime.set(System.currentTimeMillis());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String key = SysConstant.ANTI_ATTACK_ + request.getSession().getId();
        Object code = redisUtil.get(key);
        if (code != null) {
            throw new IllegalArgumentException(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
        }
    }

    @AfterReturning(pointcut = "antiAttack()")
    public void doAfterReturning() throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String key = SysConstant.ANTI_ATTACK_ + request.getSession().getId();
        redisUtil.set(key, "send sms all too often", 1, TimeUnit.MINUTES);
        log.info("处理耗时：" + (System.currentTimeMillis() - startTime.get()) + "ms");
        log.info("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
    }
}
