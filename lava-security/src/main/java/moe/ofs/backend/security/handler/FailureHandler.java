package moe.ofs.backend.security.handler;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse
            response, AuthenticationException exception) throws IOException {

//        强制用户跳转
//        new DefaultRedirectStrategy().sendRedirect(request, response, "http://" + ip + ":" + port + "/");
    }
}
