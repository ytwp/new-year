package wang.yeting.newyear.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import wang.yeting.newyear.model.Result;

import java.io.IOException;
import java.util.List;

/**
 * @author : weipeng
 * @since : 2022-01-26 09:36
 */
@RestController
public class Tesr {

    @PostMapping("/test")
    public Result<?> send(@RequestBody Vo vo) throws IOException {
        String post = HttpUtil.post("http://127.0.0.1:8000/test/", JSONUtil.toJsonStr(vo));
        return Result.success();
    }

    @Data
    public static class Vo {
        private List<List<Integer>> imgArray;
        private Integer width;
        private Integer height;
    }
}
