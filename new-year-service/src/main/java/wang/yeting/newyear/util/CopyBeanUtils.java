package wang.yeting.newyear.util;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;

/**
 * @author : weipeng
 * @date : 2020-10-17 9:50 上午
 */

public class CopyBeanUtils {

    @SneakyThrows
    public static <T> T copyProperties(Object obj, Class<T> clazz) {
        T o = (T) clazz.newInstance();
        BeanUtils.copyProperties(obj, o);
        return o;
    }

}
