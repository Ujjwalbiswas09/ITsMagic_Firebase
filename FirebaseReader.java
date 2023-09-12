package JAVARuntime;
import java.util.*;
import java.lang.Runnable;
import java.lang.Thread;
import java.net.*;
import java.io.*;
import org.json.*;
import java.util.concurrent.*;
import android.net.*;

public class FirebaseReader implements Runnable
{

	@Override
	public void run(){
		initiateConnection(url);
		reader();
	}
	private boolean connected = true;
	private URL url;

	
	public FirebaseReader(String ur){
	connected = true;
	url = getURL(ur+".json");
	t = new Thread(this);
	//NetworkInfo info = new NetworkInfo();
	}
	
	public void connect(){
	t.start();
	}
	
	private URL getURL(String val){
		try
		{
			return new URL(val);
		}
		catch (MalformedURLException e)
		{}
		return null;
	}
	public void setValueEvent(ValueEventListener lia){
	even = lia;
	}
	private Thread t;
	private ExecutorService pool;
	private void onReceive(String str){
	try{
	JSONObject js =new JSONObject(str);
	if(even != null){
	even.onChange(js.getString("path"),js.get("data")); }
	}catch(Exception e){
	System.out.println(e.toString());
	}
	}
	private ValueEventListener even;
	private InputStream inputStream;
	private HttpURLConnection connection;
	
	private void initiateConnection(URL firebaseUrl){
	try{
	connection = (HttpURLConnection) url.openConnection();
	connection.setRequestMethod("GET");
	connection.setRequestProperty("Accept", "text/event-stream");
	inputStream = connection.getInputStream();
	}catch(Exception e){
	connected = false;
	}
	
	}
	private String event;
	private void reader(){
		try{
		BufferedReader reaf = new BufferedReader(new InputStreamReader(inputStream));
		while (true) {
			String data = reaf.readLine();
			if(data == null){
			connected = false;
			break;
			}
			String[] lines = data.split("\\n");
			for (String line : lines) {
				if (line.startsWith("event:")) {
				event = line.substring(6).trim();
				} else if (line.startsWith("data:")) {
					if(!event.equals("keep-alive")){
				String jsonData = line.substring(5).trim();
				onReceive(jsonData); }
				} 
			} }
		}catch(Exception e){
		connected = false;
		//System.out.println(e.toString());
		}
	}
	
	public void disconnect(){
	t.interrupt();
	pool.shutdown();
	connection.disconnect();
	}
}
