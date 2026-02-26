package com.ccj.campus.chat.util;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @Author ccj
 * @Date 2024-04-11 16:35
 * @Description 对Spring el 表达式 解析工具类
 */
public class SpElUtils {

    private final static ExpressionParser PARSER = new SpelExpressionParser();
    private final static DefaultParameterNameDiscoverer PARAM_DISCOVERER = new DefaultParameterNameDiscoverer();

    public static String parseSpEl(Method method, Object[] argsVal, String spEl) {
        String[] paramNameS = Optional.ofNullable(PARAM_DISCOVERER.getParameterNames(method)).orElse(new String[]{});
        //el解析需要的上下文对象
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < paramNameS.length; i++) {
            //所有参数都作为原材料扔进去
            context.setVariable(paramNameS[i], argsVal[i]);
        }

        Expression expression = PARSER.parseExpression(spEl);
        return expression.getValue(context, String.class);
    }


}