
package com.example.stage.core.config;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 接口文档注解
 *
 * @author 郑延康
 * @date: 2023/3/11 17:32
 */
@Configuration
@EnableSwagger2
@EnableSwaggerBootstrapUI
public class Swagger2Config {

    @Value("${swagger2.enabled}")
    private boolean swaggerEnabled;

    @Bean
    public Docket createQrApiSystem() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swaggerEnabled)
                .groupName("所有接口")
                .apiInfo(apiInfo("Hello~ 前端通用接口文档"))
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example"))// controller路径
                .paths(PathSelectors.any()).build();// 所有Controller
    }

    private ApiInfo apiInfo(String title) {
        return new ApiInfoBuilder().title(title).description("Welcome to Silicon Valley").build();
    }

}
