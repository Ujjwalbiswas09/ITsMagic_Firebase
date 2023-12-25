package JAVARuntime;
import java.net.*;
import java.lang.Runnable;
import java.lang.Thread;
import java.util.*;
import java.lang.reflect.*;
import javax.net.ssl.*;
import java.io.*;
import javax.net.*;
import org.json.*;
import java.nio.charset.*;

public class FirebaseWriter {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private final String url;
  private boolean isConnected;
	private URI ui;
	private String host;
	private final ArrayDeque<WriterUnit> units = new ArrayDeque<WriterUnit>();
	private final ArrayDeque<String> pending =new ArrayDeque<String>();
	private WriterUnit current;
	private Thread generator;
	public FirebaseWriter(String rl){
	url = rl+".json";
		
	}
	
	public void setValue(Object obj){
			String to="";
			if(obj.getClass()==String.class){
				to = "\""+obj.toString()+"\"";
			}else{
				to = obj.toString();
			}
			String val ="PUT "+ui.getPath()+" HTTP/1.1\r\n"+
				"Host: "+ui.getHost()+"\r\n"+
				"Content-Type: application/json\r\n"+
				"Accept: */*\r\n"+
				"User-Agent: curl/7.54.0\r\n"+
				"Conntection: keep-alive\r\n"+
				"Content-length: "+to.getBytes().length+"\r\n\n"+to;
		write(val);
	}
	public void updateChild(JSONObject json){
		String val ="PATCH "+ui.getPath()+" HTTP/1.1\r\n"+
			"Host: "+ui.getHost()+"\r\n"+
			"Content-Type: application/json\r\n"+
			"Accept: */*\r\n"+
			"User-Agent: curl/7.54.0\r\n"+
			"Conntection: keep-alive\r\n"+
			"Content-length: "+json.toString().getBytes().length+"\r\n\n"+json.toString();
		write(val);
	}
	public void updateChild(String key,Object obj){
		try{
	JSONObject js = new JSONObject();
	js.put(key,obj);
		String val ="PATCH "+ui.getPath()+" HTTP/1.1\r\n"+
			"Host: "+ui.getHost()+"\r\n"+
			"Content-Type: application/json\r\n"+
			"Accept: */*\r\n"+
			"User-Agent: curl/7.54.0\r\n"+
			"Conntection: keep-alive\r\n"+
			"Content-length: "+js.toString().getBytes().length+"\r\n\n"+js.toString();
		write(val);
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}
	public void delete(){
		String val ="DELETE "+ui.getPath()+" HTTP/1.1\r\n"+
			"Host: "+ui.getHost()+"\r\n"+
			"Content-Type: application/json\r\n"+
			"Accept: */*\r\n"+
			"User-Agent: curl/7.54.0\r\n"+
			"Conntection: keep-alive\r\n";
		write(val);
	}
	
	private void write(String val){
		try{
		if(current.i > 99){
	  if(units.size() > 0){
		current.sok.close();
		current = units.poll();
		}
		}
		current.out.write(val.getBytes(UTF8));
		current.i++;
		}catch (Exception e){
		pending.add(val);
		disconnect();
		}
	}
	
	
	
	public void deleteChild(String key){
	String val ="DELETE "+ui.getPath()+"/"+key+" HTTP/1.1\r\n"+
	"Host: "+ui.getHost()+"\r\n"+
	"Content-Type: application/json\r\n"+
	"Accept: */*\r\n"+
	"User-Agent: curl/7.54.0\r\n"+
	"Conntection: keep-alive\r\n";
	write(val);
	}
	
	public String quickPost(Object obj){
	try{
	String id = UUID.randomUUID().toString();
	updateChild(id,obj);
	return id;
	}catch(Exception e){
		
	}
	return null;
	}
	public boolean isConnected(){
	return isConnected;
	}
	
	public void disconnect(){
			if(!isConnected){
				return;
			}
					isConnected = false;
					units.clear();
	}
	private final Runnable task  =new Runnable(){
								public void run(){
								try{
								while(true){
							  if(units.size()< 2){
								synchronized(units){
								units.add(new WriterUnit(url));
								}}
								}}catch(Exception e){
								synchronized(units){
								units.clear();
								}
							  }
							}};
							
	public void connect() {
      try {
			ui = new URL(url).toURI();
			host = ui.getHost();
			current = new WriterUnit(url);
	    generator = new Thread(task);
			generator.start();
      isConnected = true;
			while(pending.size() > 0){
			write(pending.poll());
			}
      }catch (Exception e) {
      System.out.println(e.toString());
      }
		
    }
	
			private class WriterUnit{

							int i;
							OutputStream out;
							Socket sok;
						  WriterUnit(String url)throws Exception{
																URI ur = new URL(url).toURI();
																sok = c(ur);
																out = sok.getOutputStream();
													


										}

							Socket c(URI url)throws Exception{
												SocketFactory factory = 
														android.net.SSLCertificateSocketFactory.getDefault( 
														6000, null); 
												SSLSocket sslSocket = (SSLSocket) factory.createSocket(url.getHost(), 443); 
												HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier(); 
												SSLSession sslSession = sslSocket.getSession(); 
												hv.verify(url.getHost(), sslSession);
												return sslSocket;
										}
						}
	
	
}
