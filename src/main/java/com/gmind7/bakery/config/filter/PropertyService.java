package com.gmind7.bakery.config.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ExtendedProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

public class PropertyService implements InitializingBean, DisposableBean, ResourceLoaderAware {
	
	protected Logger log = LoggerFactory.getLogger(PropertyService.class);
	
	private ExtendedProperties properties = null;
	private ResourceLoader resourceLoader = null;

	private Set<String> extFileName;
	
	private void refreshPropertyFiles() throws Exception {
		
        String fileName = null;
		try {			
			Iterator<String> it = extFileName.iterator();
			while (it != null && it.hasNext()) {
		        // Get element
		        Object element = it.next();
		        String enc = null;
		        fileName = (String)element;
				loadPropertyResources( fileName , enc );	        	
		    }

		} catch (Exception e) {
			log.debug("Exception occured. {}", e);
		}
	}

	private void loadPropertyResources(String location, String encoding)
			throws Exception {

		if (resourceLoader instanceof ResourcePatternResolver) {
			try {
				Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
				loadPropertyLoop(resources, encoding);
			} catch (IOException ex) {
				throw new BeanDefinitionStoreException("Could not resolve Properties resource pattern [" + location + "]", ex);
			}
		} else {
			Resource resource = resourceLoader.getResource(location);
			loadPropertyRes(resource, encoding);
		}
	}
	
	private void loadPropertyLoop(Resource[] resources, String encoding)
			throws Exception {
		Assert.notNull(resources, "Resource array must not be null");
		for (int i = 0; i < resources.length; i++) {
			loadPropertyRes(resources[i], encoding);
		}
	}

	private void loadPropertyRes(Resource resource, String encoding)
			throws Exception {
		properties.load(resource.getInputStream(), encoding);
	}
	
	@SuppressWarnings("rawtypes")
	public void afterPropertiesSet() {
		try {
			properties = new ExtendedProperties();
			
			// 외부파일이 정의되었을때
			if ( extFileName != null ){			
				refreshPropertyFiles();
			}
			
			Iterator it = properties.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				System.setProperty(key, value);
			}
			
		} catch (Exception e) {
			log.debug("Exception occured. {}", e);
		}
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;		
	}

	@Override
	public void destroy() throws Exception {
		properties = null ;		
	}
	
}
