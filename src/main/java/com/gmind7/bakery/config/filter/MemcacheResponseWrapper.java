package com.gmind7.bakery.config.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class MemcacheResponseWrapper extends HttpServletResponseWrapper{

	protected ServletOutputStream stream;
	protected PrintWriter writer = null;
	protected HttpServletResponse origResponse = null;
	private int httpStatus = 200;
	
	public MemcacheResponseWrapper(HttpServletResponse response) {
		super(response);
		response.setContentType("application/json");
		origResponse = response;
	}
	
	public ServletOutputStream createOutputStream() throws IOException {
		return new WrappedOutputStream(origResponse);
	}
	
	public ServletOutputStream getOutputStream() throws IOException {
		if( writer != null ){
			throw new IllegalStateException("getWriter() has already bean called for this response");
		}
		
		if( stream == null ) {
			stream = createOutputStream();
		}
		
		return stream;
	}
	
	public PrintWriter getWriter() throws IOException{
		if( writer != null){
			return writer;
		}
	
		if ( stream != null ){
			throw new IllegalStateException("getOutputStream() has already been called for this response");
		}
		
		stream = createOutputStream();
		writer = new PrintWriter(stream);
		return writer;
	}
	
	@Override
	public void sendError(int sc) throws IOException{
		httpStatus = sc;
		super.sendError(sc);
	}
	
	@Override
	public void sendError(int sc, String msg) throws IOException{
		httpStatus = sc;
		super.sendError(sc, msg);
	}
	
	@Override
	public void setStatus(int sc){
		httpStatus = sc;
		super.setStatus(sc);
	}
	
	public int getStatus(){
		return httpStatus;
	}
	
	private class WrappedOutputStream extends ServletOutputStream{
		private ArrayList<Byte> byteList = new ArrayList<Byte>();
		private HttpServletResponse originalResponse;
		
		public WrappedOutputStream(HttpServletResponse response){
			this.originalResponse = response;
		}

		@Override
		public String toString(){
			byte[] b = new byte[byteList.size()];
			for(int i = 0; i < byteList.size(); i++){
				b[i] = byteList.get(i);
			}
			return new String(b);
		}
		
		@Override
		public void write(int arg0) throws IOException {
			byteList.add((byte) arg0);
			originalResponse.getOutputStream().write(arg0);
			
		}
		
	}
}
