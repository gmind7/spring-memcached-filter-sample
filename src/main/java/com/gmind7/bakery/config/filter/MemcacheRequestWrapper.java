package com.gmind7.bakery.config.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class MemcacheRequestWrapper extends HttpServletRequestWrapper{

	protected ServletInputStream stream;
	protected HttpServletRequest origRequest = null;
	protected BufferedReader reader = null;
	
	public MemcacheRequestWrapper(HttpServletRequest request) throws IOException{
		super(request);
		origRequest = request;
	}
	
	public ServletInputStream createInputStream() throws IOException{
		return (new WrappedInputStream(origRequest));
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException{
		if( reader != null){
			throw new IllegalStateException("getReader() has already been called for this request.");
		}
		if( stream == null){
			stream = createInputStream();
		}
		return stream;
	}
	
	@Override
	public BufferedReader getReader() throws IOException{
		 if( reader != null){
			 return reader;
		 }
		 if( stream != null){
			 throw new IllegalStateException("getReader() has already been called for this request.");
		 }
		 stream = createInputStream();
		 reader = new BufferedReader(new InputStreamReader(stream));
		 return reader;
	}
	
	private class WrappedInputStream extends ServletInputStream {
		private StringBuffer originalInput = new StringBuffer();
		private ByteArrayInputStream byteArrayInputStream;
		
		public WrappedInputStream(HttpServletRequest request) throws IOException {
			BufferedReader bufferedReader = null;
			try{
				InputStream inputStream = request.getInputStream();
				if( inputStream != null){
					bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
					char[] charBuffer = new char[128];
					int bytesRead = -1;
					while( (bytesRead = bufferedReader.read(charBuffer)) > 0) {
						originalInput.append(charBuffer, 0, bytesRead);
					}
				}
				byteArrayInputStream = new ByteArrayInputStream(originalInput.toString().getBytes());
			} catch (IOException ex){
				throw ex;
			} finally {
				if( bufferedReader != null){
					try{
						bufferedReader.close();
					} catch (IOException ex) {
						throw ex;
					}
				}
			}
		}
		
		@Override
		public int read() throws IOException {
			return byteArrayInputStream.read();
		}
		
		@Override
		public String toString(){
			return this.originalInput.toString();
		}
		
	}

}
