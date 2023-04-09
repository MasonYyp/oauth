package com.mason.oauthgateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;


import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置全局web错误异常，会自动捕捉InvalidTokenException、OAuth2Exception等异常
 */

@Configuration
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 判断ServerHttpResponse的底层输出流是否关闭
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置返回JSON
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 设置状态码
        if(ex instanceof InvalidTokenException){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        }

        byte[] msgInfo = null;

        try {
            // 返回响应结果
            Map<String,Object> result = new HashMap<>();
            HttpStatus httpStatus = response.getStatusCode();
            if(httpStatus == null){
                result.put("code", -1);
                result.put("msg", "Status code error");
            }else {
                result.put("code", httpStatus.value());
                result.put("msg", ex.getMessage());
            }

            // 转换为字节数组
            msgInfo = objectMapper.writeValueAsBytes(result);
        }catch (JsonProcessingException e) {
            String writeError = "Error writing response";
            msgInfo = writeError.getBytes();
            System.out.println(writeError + ex);
        }

        byte[] finalMsgInfo = msgInfo;
        DataBufferFactory bufferFactory = response.bufferFactory();

        // 从供应商（Supplier）中创建消息序列
        Mono<DataBuffer> mono = Mono.fromSupplier(() -> bufferFactory.wrap(finalMsgInfo));

        // 写入返回值中
        return response.writeWith(mono);
    }
}