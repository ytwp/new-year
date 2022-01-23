package wang.yeting.newyear.Interceptor;

import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import wang.yeting.newyear.annotation.Permission;
import wang.yeting.newyear.model.Result;
import wang.yeting.newyear.model.bo.UserBo;
import wang.yeting.newyear.util.TokenUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author : weipeng
 * @date : 2020-08-20 18:45
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenUtils tokenUtils;

    @SneakyThrows
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            Permission permission = method.getAnnotation(Permission.class);
            Permission classPermission = method.getDeclaringClass().getAnnotation(Permission.class);
            String token = request.getHeader("token");
            UserBo user = null;
            if (token != null) {
                user = tokenUtils.getLoginDtoByToken(token);
                request.setAttribute("user", user);
            }
            if (permission != null || classPermission != null) {
                if (permission == null) {
                    permission = classPermission;
                }
                if (permission.login() && user == null) {
                    response.getOutputStream().print(JSONUtil.toJsonStr(Result.tokenExpired()));
                    return false;
                }
                if (permission.cms()) {
                    boolean checkRole = tokenUtils.checkCms(request.getHeader("cmsToken"));
                    if (!checkRole) {
                        response.getOutputStream().print(JSONUtil.toJsonStr(Result.parameterError()));
                        return false;
                    }
                }
                if (permission.permissions().length > 0) {
                }
            }
        }
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

}
