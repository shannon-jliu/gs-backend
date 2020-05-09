package org.cuair.ground.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class LoggingFilter implements Filter {

  private static final Logger logger = LoggerFactory.getLogger("loggingFilter");

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
                       final FilterChain chain)
      throws IOException, ServletException {
    long time = System.currentTimeMillis();
    try {
      chain.doFilter(request, response);
    } finally {
      time = System.currentTimeMillis() - time;
      HttpServletRequest req = (HttpServletRequest) request;
      HttpServletResponse res = (HttpServletResponse) response;
      logger.info("{}: {} - {} - {}ms - {}", req.getRemoteAddr(), req.getMethod(),
          req.getRequestURI(), time, res.getStatus());
    }
  }
}
