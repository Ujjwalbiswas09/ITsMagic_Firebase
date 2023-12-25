

# FirebaseWriter Demo
```java
pulbic static void main(String[] args){
FirebaseWriter writer = new FirebaseWriter(FIREBASE_URL);
writer.connect ();
writer.updateChild(KEY,VALUE);

//if you don't need to continue
writer.disconnect();
}
```
# FirebaseReader Demo
```java
public static void main(String[] args){
FirebaseReader reader = new FirebaseReader(FIREBASE_URL);
reader.setListener(new ValueEventListener(){
public void onChange(String path,Object value) throws Exception {
Console.log("path: "+path+" value :"+val.toString());
}
});
reader.connect();
}
```

# Connection Observer Demo
```java
public static void main(String [] args){
ConnectionObserver observer = new ConnectionObserver();
observer.setListener(new ConnectionListener(){
public void onConnected(){
Console.log("Internet is Connected");
}
public void onUpdate(boolean state){
Console.log(state?"Still Connected":"Still Disconnected");
}
public void onDisconnect(){
Console.log("Internet is Disconnected");
}
});
observer.startListening();
}
```

# A Proper Small Demo
```java
public class Main implements ValueEventListener, ConnectionListener {

  public static final String ur = "Your Firebase Database URL";

  @Override
  public void onUpdate(boolean state) {
    if (state) {
      if (!reader.isConnected()) {
        reader.connect();
      }
      if (!writer.isConnected()) {
        writer.connect();
      }
    }
  }

  private FirebaseWriter writer;
  private FirebaseReader reader;
  private ConnectionObserver observer;
  public void start() {
    writer = new FirebaseWriter(ur);
    reader = new FirebaseReader(ur, this);
    observer = new ConnectionObserver(this);
    observer.startListening();
    while (true) {
      if (writer.isConnected()) {
        long i = System.currentTimeMillis();
        writer.setValue(i);
        System.out.println(i);
      }
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  @Override
  public void onConnected() {
    if (!reader.isConnected()) {
      reader.connect();
    }
    if (!writer.isConnected()) {
      writer.connect();
    }
    System.out.println("connected");
  }

  @Override
  public void onDisconnect() {
    if (writer.isConnected()) {
      writer.disconnect();
    }
    if (reader.isConnected()) {
      reader.disconnect();
    }
    System.out.println("disconnected");
  }

  @Override
  public void onChange(String path, Object obj) throws Exception {
    System.out.println(obj);
  }

  public static void main(String[] args) throws Exception {

    new Main().start();
  }
}
```

# Tutorial Link 
<a>https://bitly.ws/UxBe</a>
