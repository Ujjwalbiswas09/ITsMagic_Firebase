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

public class FirebaseWriter {

	private long done=0;
	private final String url;
    private SSLSocket socket;
    private BufferedReader reader;
    private boolean isConnected;
	private InputStream reax;
	private OutputStream write;
	private URI ui;
	private String host;
	private SSLSession sslSession;
	private boolean reconnect=false;
	private BufferedReader input;
	private PipedInputStream pip;
	private PipedOutputStream out;
	public boolean rec;
	public FirebaseWriter(String rl){
	url = rl+".json";
	out = new PipedOutputStream();
		try
		{
			pip = new PipedInputStream(out);
		}
		catch (IOException e)
		{}
		input = new BufferedReader(new InputStreamReader(pip));
		
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
		try
		{
		write.write(val.getBytes());
		}catch (Exception e){
		if(e instanceof SSLProtocolException|| e instanceof SSLException || e instanceof SocketException){
		disconnect();
		connect();
		write(val);
		}else{
		//e.printStackTrace();
		}
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
	
	
	private boolean push=false;
	
	
	public String post(Object obj){
		push=true;
		String to="";
		if(obj.getClass()==String.class){
			to = "\""+obj.toString()+"\"";
		}else{
			to = obj.toString();
		}
		String val ="POST "+ui.getPath()+"?print=pretty HTTP/1.1\r\n"+
			"Host: "+ui.getHost()+"\r\n"+
			"Accept: */*\r\n"+
			"Content-Type: application/json\r\n"+
			"User-Agent: curl/7.54.0\r\n"+
			"Conntection: keep-alive\r\n"+
			"Content-length: "+to.getBytes().length+"\r\n\n"+to;
		try
		{
			write(val);
			while(pip.available() < 1){
			}
			byte[] read = new byte[pip.available()];
			pip.read(read);
			
			String str= new String(read).trim();
			JSONObject js = new JSONObject(str);
			return js.getString("name");
		}
		catch (Exception e){
			
		}
		return "";
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
	
	
	private static synchronized long getId(){
	return i++;
	}
	private static long i=0;
	public void disconnect(){

		try{
			socket.close();
			if(messageListenerThread != null){
				messageListenerThread.interrupt();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public void connect() {
        try {
			ui = new URL(url).toURI();
			host = ui.getHost();
            int port = 80;
			port = ui.getPort();
			if(port == -1){
			port = 443;
			}
			SocketFactory factory =
			android.net.SSLCertificateSocketFactory.getDefault(60000);
			SSLSocket sslSocket = (SSLSocket) factory.createSocket(host, port);
			HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
			sslSession = sslSocket.getSession();
			hv.verify(host,sslSession);
			socket = sslSocket;
		    reax = socket.getInputStream();
			write = socket.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(reax));
		 if(rec){
			performWebSocketHandshake();
			}
			startListeningForMessages();
            isConnected = true;
			reconnect = false;
        } catch (Exception e) {
            //System.out.println(e.toString());
        }
		
    }
	private Thread messageListenerThread;
	private void startListeningForMessages() {
        messageListenerThread = new Thread(){ public void run(){
		while (isConnected) {
		try {
		String message = reader.readLine().trim();
		//System.out.println(message);
		if(message.isEmpty()){
		}
			if(message.startsWith("{")){
			String res = reader.readLine().trim();
			//System.out.println(res);
			out.write(("{"+res+"}").getBytes());
			}
		if(message.trim().equals("Connection: close")){
		reconnect = true;
		break;
		}
		} catch (Exception e) {
		}
				}
			}};
        messageListenerThread.start();
    }
	
	public void performWebSocketHandshake()throws Exception{
		String val ="GET "+ui.getPath()+"?print=pretty HTTP/1.1\r\n"+
			"Host: "+ui.getHost()+"\r\n"+
			"User-Agent: curl/7.54.0\r\n"+
			"Content-Type: application/json\r\n"+
			"Accept: text/event-stream\r\n";//+
			//"Conntection: keep-alive\r\n";
		write.write(val.getBytes());
		write.flush();
	}
	
	
}
