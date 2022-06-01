package com.rallibau.apps.commons.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HasPermissionAspect {


    @Around("@annotation(org.springframework.jms.annotation.JmsListener) " +
            "|| @annotation( org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public Object profileAllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println("ha llegado un msm");
        return proceedingJoinPoint.proceed();
    }


}

