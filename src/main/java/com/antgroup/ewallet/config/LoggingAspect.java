//package com.antgroup.ewallet.config;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Aspect
//@Component
//public class LoggingAspect {
//    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
//    private final ObjectMapper objectMapper;
//
//    public LoggingAspect(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//    }
//
//    @Around("execution(* com.antgroup.ewallet.controller..*(..)) || " +
//            "execution(* com.antgroup.ewallet.service..*(..))")
//    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
//        String className = joinPoint.getTarget().getClass().getSimpleName();
//        String methodName = joinPoint.getSignature().getName();
//
//        try {
//            // Log method entry
//            logger.debug("Entering {}.{} with parameters: {}",
//                    className,
//                    methodName,
//                    objectMapper.writeValueAsString(joinPoint.getArgs())
//            );
//
//            long startTime = System.currentTimeMillis();
//            Object result = joinPoint.proceed();
//            long executionTime = System.currentTimeMillis() - startTime;
//
//            // Log method exit
//            logger.debug("Exiting {}.{} with result: {}. Execution time: {}ms",
//                    className,
//                    methodName,
//                    objectMapper.writeValueAsString(result),
//                    executionTime
//            );
//
//            return result;
//        } catch (Exception e) {
//            // Log error
//            logger.error("Error in {}.{}: {}",
//                    className,
//                    methodName,
//                    e.getMessage()
//            );
//            throw e;
//        }
//    }
//
//    @Around("execution(* com.antgroup.ewallet.service.AliPayService.*(..))")
//    public Object logPaymentOperations(ProceedingJoinPoint joinPoint) throws Throwable {
//        String methodName = joinPoint.getSignature().getName();
//
//        try {
//            // Log payment operation start
//            logger.info("Starting payment operation: {} with parameters: {}",
//                    methodName,
//                    objectMapper.writeValueAsString(joinPoint.getArgs())
//            );
//
//            long startTime = System.currentTimeMillis();
//            Object result = joinPoint.proceed();
//            long executionTime = System.currentTimeMillis() - startTime;
//
//            // Log payment operation completion
//            logger.info("Completed payment operation: {}. Execution time: {}ms. Result: {}",
//                    methodName,
//                    executionTime,
//                    objectMapper.writeValueAsString(result)
//            );
//
//            return result;
//        } catch (Exception e) {
//            // Log payment operation error
//            logger.error("Payment operation failed: {} with error: {}",
//                    methodName,
//                    e.getMessage(),
//                    e
//            );
//            throw e;
//        }
//    }
//}