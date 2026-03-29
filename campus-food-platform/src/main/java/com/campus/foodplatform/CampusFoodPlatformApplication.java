/** Spring Boot 应用入口，启动校园美食平台服务 */
package com.campus.foodplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CampusFoodPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusFoodPlatformApplication.class, args);
    }
}
