package me.shimi.logback;

import java.io.IOException;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

public class PlotMachineAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private String apiUrl = null;
	
	private AsyncHttpClient asyncHttpClient;
	
	public void setApiUrl(final String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	@Override
	public void start() {
		if (apiUrl == null) {
			 addWarn("No API url was set for the PlotMachineAppender named " + getName());
		}
		asyncHttpClient = new AsyncHttpClient();
	    super.start();
	}
	
	@Override
	protected void append(ILoggingEvent event) {
		String key = event.getLoggerName();
		try {
			String name = event.getMessage();
			Long.parseLong(name);
			sendToPlotMachine(key, name);
		} catch(NumberFormatException e) {
			addError("Message should be double", e);
		}
	}

	private void sendToPlotMachine(final String name, final String value) {
		try {
			asyncHttpClient.prepareGet(apiUrl)
					.addQueryParameter("name", name)
					.addQueryParameter("value", value)
					.execute(new AsyncCompletionHandler<Integer>(){
			    
			    @Override
			    public Integer onCompleted(Response response) throws Exception{
			        int code = response.getStatusCode();
			        if (code != HttpResponseStatus.OK.getCode()) {
			        	addError("Got Status " + code + " code from PlotMachine for (" + name + "," + value + ")");
			        }
			        return 0;
			    }
			    
			    @Override
			    public void onThrowable(Throwable t) {
			    	addError("Failed to send event to PlotMachine", t);
			    }
			});
		} catch (IOException e) {
			addError("Failed to send event to PlotMachine", e);
		}
	}
}
