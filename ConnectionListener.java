package JAVARuntime;

public interface ConnectionListener
{
		void onConnected();
		void onDisconnect();
		void onUpdate(boolean state);
}
