package net.ddns.mucaho.jnetrobust.controller;

import net.ddns.mucaho.jnetrobust.data.Packet;
import net.ddns.mucaho.jnetrobust.util.Config;

public class RetransmissionController extends Controller {
	protected ResponseControl responseHandler;
	
	public RetransmissionController(Config config) {
		super(config);
		responseHandler = new ResponseControl(pendingMapHandler.dataMap.getMap().values(), 
				config.listener);
	}

	public synchronized void retransmit() {
		// Update outdated not acked packets
		boolean shouldRetransmit = responseHandler.updatePendingTime(rttHandler.getRTO());
		if (shouldRetransmit) {
			rttHandler.backoff();
		}
	}
	
	
	@Override
	public synchronized Packet send(Object data) {
		// Reset continuous receive time	
		responseHandler.resetReceivedTime();
		
		return super.send(data);
	}
	
	
	@Override
	public synchronized Object receive(Packet pkg) {
		// Update continuous receive time
		responseHandler.updateReceivedTime(rttHandler.getRTO() / 2);
		
		return super.receive(pkg);
	}
}
