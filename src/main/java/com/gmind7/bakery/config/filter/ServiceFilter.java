package com.gmind7.bakery.config.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class ServiceFilter implements Filter {
	
	protected Logger log = LoggerFactory.getLogger(ServiceFilter.class);

	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("serviceFilter is initialized");
	}
	
	@Override
	public void destroy() {
		log.info("serviceFilter is destroyed");
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
		ServletException {

		HttpServletRequest request = (HttpServletRequest)servletRequest;
		//HttpServletResponse response = (HttpServletResponse)servletResponse;

		String url = request.getRequestURL() != null ? request.getRequestURL().toString() : "";
		String queryString = request.getQueryString();
		url = new StringBuilder(String.valueOf(url)).append(queryString != null ? ( new StringBuilder("?")).append(queryString).toString() : "").toString();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			StringBuffer sbf = new StringBuffer();
			for (Cookie cookie : cookies) {
				sbf.append(cookie.getName()).append("=").append(cookie.getValue()).append("\n");
			}
			MDC.put("cookie", sbf.toString());
		}
		MDC.put("hostName", request.getLocalName());
		MDC.put("client", request.getLocalAddr());
		MDC.put("user-agent", request.getHeader("user-agent"));
		if (request.getHeader("REFERER") != null) {
			MDC.put("referer", request.getHeader("REFERER"));
		}
		MDC.put("url", url);

		filterChain.doFilter(servletRequest, servletResponse);
		MDC.clear();
	}

}
