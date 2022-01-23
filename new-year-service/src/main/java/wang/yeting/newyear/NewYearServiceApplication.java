package wang.yeting.newyear;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@MapperScan("wang.yeting.newyear.mapper")
public class NewYearServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewYearServiceApplication.class, args);
    }

}
