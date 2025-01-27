package com.example.cdcconsumer.global.aop;

import com.example.cdcconsumer.global.annotation.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimerAspect {

    @Around("@annotation(timer)") // @Timer가 있는 메소드만 적용
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, Timer timer) throws Throwable {
        long start = System.currentTimeMillis();

        System.out.println("Method " + joinPoint.getSignature() + " 시작");

        Object result = joinPoint.proceed(); // 실제 메소드 실행

        long end = System.currentTimeMillis();
        long executionTime = end - start;

        System.out.println("Method " + joinPoint.getSignature() + " 종료");
        System.out.println("소요 시간: " + executionTime + "ms");

        return result;
    }
}
