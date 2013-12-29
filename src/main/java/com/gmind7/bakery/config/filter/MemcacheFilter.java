package com.gmind7.bakery.config.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import net.spy.memcached.MemcachedClientIF;

@Component
public class MemcacheFilter implements Filter {

	protected Logger log = LoggerFactory.getLogger(MemcacheFilter.class);
	
	private MemcachedClientIF client;

	private static final String FILTER_CACHE_NAMESPACE = "GA:";
	
	@Override
	public void destroy() {
		log.debug("MemcacheFilter destroy");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException {
		try{
			if(request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
				MemcacheResponseWrapper responseWrap = new MemcacheResponseWrapper((HttpServletResponse) response);
				MemcacheRequestWrapper requestWrap = new MemcacheRequestWrapper((HttpServletRequest) request);
				
				String key = composeKey(requestWrap);
				if ( key == null ) {
					log.debug("The request for root directory is not cached for checking server launching.");
					filterChain.doFilter(request, response);
				} else {
					String value = (String) client.get(key);
					
					if( value == null){
						log.debug("Cache miss for key : {}", key);
						filterChain.doFilter(requestWrap, responseWrap);
						if( responseWrap.getStatus() == HttpServletResponse.SC_OK){
							value = responseWrap.getOutputStream().toString();
							client.add(key, retrieveExpTime(), value);
							log.debug("Adding response to cache : {}", (value.length() > 50 ? value.substring(0, 50) + "..." : value));
						} else {
							log.debug("Did not add content to cache. ResponseStatus is {}.", responseWrap.getStatus());
						}
					} else {
						log.debug("Cache hit for key {}", key);
						log.debug("Cache hit value is {}", value);
						response.setCharacterEncoding("UTF-8");
						response.getWriter().println(value);
					}
				}
			}
		} catch(Exception ex){
			log.debug("Cache functionality skipped due to exception : {}", ex.getMessage());
			String errorJson = "{\"result\":\"Exception occured.\"}";
			response.getWriter().println(errorJson);
		}

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.debug("MemcacheFilter init");
	}

	private String composeKey(HttpServletRequest httpRequest){
		String uri = httpRequest.getRequestURI();
        // root directory 접근의 경우, cache 처리에서 제외.
        if (uri.equalsIgnoreCase("/")) return null;
		if (uri.indexOf(".") != -1) uri = uri.substring(0, uri.indexOf("."));
        return new StringBuffer(FILTER_CACHE_NAMESPACE).append(uri.replaceAll("/", "_")).toString();
	}
	
	private int retrieveExpTime(){
		return Integer.parseInt(System.getProperty("memcached.expiretime"));
	}
}
