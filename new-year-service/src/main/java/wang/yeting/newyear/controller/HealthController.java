package wang.yeting.newyear.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.yeting.newyear.model.Result;

/**
 * @author : weipeng
 * @date : 2020-10-19 12:52 下午
 */
@RestController
public class HealthController {

    @RequestMapping("/health")
    public Result<?> health() {
        return Result.success();
    }
}
