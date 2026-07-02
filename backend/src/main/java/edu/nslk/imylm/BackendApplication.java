// /backend/src/main/java/edu/nslk/imylm/BackendApplication.java
// 职责描述：Spring Boot 应用启动类

package edu.nslk.imylm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("edu.nslk.imylm.mapper")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
