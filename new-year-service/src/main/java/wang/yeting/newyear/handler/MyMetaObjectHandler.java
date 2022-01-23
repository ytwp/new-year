package wang.yeting.newyear.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author : weipeng
 * @date : 2020-05-18 20:36
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setInsertFieldValByName("createTime", LocalDateTime.now(), metaObject);
        this.setUpdateFieldValByName("modifyTime", LocalDateTime.now(), metaObject);

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setUpdateFieldValByName("modifyTime", LocalDateTime.now(), metaObject);
    }
}
