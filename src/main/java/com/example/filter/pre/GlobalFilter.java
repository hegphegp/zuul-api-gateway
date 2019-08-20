package com.example.filter.pre;

import javax.servlet.http.HttpServletRequest;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Enumeration;

@Component
public class GlobalFilter extends ZuulFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        printAllHeaders(request);
        dynamicAddZuulRequestHeader(ctx);
        ctx.setSendZuulResponse(true); // 对该请求进行路由
        ctx.set("isSuccess", true);    // 设值，让下一个Filter看到上一个Filter的状态
        return null;
    }

    private void dynamicAddZuulRequestHeader(RequestContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        // 如果网关前面经过nginx,nginx一定要配置 proxy_set_header X-Forwarded-Uri $uri; , 否则后面的服务取不到完整的请求URL
        // 如果网关服务前面没有nginx的代理,网关一定要配置 "x-forwarded-uri" 参数, 否则后面的服务取不到完整的请求URL
        if (StringUtils.isEmpty(request.getHeader("x-forwarded-uri"))) { // 有,不添加,没有,才添加
            ctx.addZuulRequestHeader("x-forwarded-uri", request.getRequestURI());
        }
    }

    public void printAllHeaders(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            logger.debug(headerName + "  ===>>>  " + request.getHeader(headerName));
        }
    }

    @Override
    public boolean shouldFilter() {
        return true;// 是否执行该过滤器，此处为true，说明需要过滤
    }

    @Override
    public int filterOrder() {
        return 0;// 优先级为0，数字越大，优先级越低
    }

    @Override
    public String filterType() {
        return "pre";// 前置过滤器
    }
}
