package JAVARuntime;

public class ConnectionObserver implements Runnable{
				@Override
public void run(){
while(true){
try{
		int i = Runtime.getRuntime().exec("ping -c 1 www.google.com").waitFor();
		if(listener != null){
		boolean is = i==0;
		if(is==wasConnected){
		listener.onUpdate(is);
		}
		if(is){
		if(!wasConnected){
		wasConnected = true;
		listener.onConnected();
		}
		}else{
		if(wasConnected){
		wasConnected = false;
		listener.onDisconnect();
		}
		}
		
		
		}
}catch(Exception e){ e.printStackTrace();}
try{
		Thread.currentThread().sleep(2000);
    }catch(Exception e){ e.printStackTrace();
		}//catch
		}//while
		
		}
		
		private boolean wasConnected=false;
		private ConnectionListener listener;
		public ConnectionObserver(){
				
		}
		public ConnectionObserver(ConnectionListener list){
		listener = list;
		}
		public void setListener(ConnectionListener lsu){
		listener = lsu;
		}
		private Thread thread;
		public void startListening(){
		if(thread!=null){
		return;
		}
		thread = new Thread(this);
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
		}
		
		public boolean isConnected(){
		return wasConnected;
		}
}
