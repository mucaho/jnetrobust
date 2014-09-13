package net.ddns.mucaho.jnetrobust;

//import com.esotericsoftware.kryonet.Connection;
//import com.esotericsoftware.kryonet.Listener;

public class ReliableUDP { //extends Listener {
/*
	private final Map<Connection, Controller> connectionHandlers = 
		new HashMap<Connection, Controller>();
	
	
	public Controller getReliableUDPHandler (Connection key) {
		return connectionHandlers.get(key);
	}


	public Controller putReliableUDPHandler(Connection key, Controller value) {
		return connectionHandlers.put(key, value);
	}


	public Controller removeReliableUDPHandler(Connection key) {
		return connectionHandlers.remove(key);
	}


	@Override
	public void received(Connection connection, Object object) {
		super.received(connection, object);
		
		Controller handler = connectionHandlers.get(connection);
		if (handler != null)
			if (object instanceof Packet)
				handler.receive((Packet) object);
	}
	
	public void send(Connection connection, Object object) {
		Controller handler = connectionHandlers.get(connection);
		if (handler != null)
			connection.sendUDP(handler.send(object));
	}
*/
	
}
