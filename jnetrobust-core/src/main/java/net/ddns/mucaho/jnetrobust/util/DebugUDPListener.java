package net.ddns.mucaho.jnetrobust.util;

import java.util.Arrays;
import java.util.Collection;

import net.ddns.mucaho.jnetrobust.data.MultiKeyValue;


public class DebugUDPListener extends UDPListener {

	private final String name;
	private final Logger logger;
	
	public DebugUDPListener(String name) {
		this(name, new Logger() {
			@Override
			public void log(String... text) {
				System.out.println(text);
			}
		});
	}
	
	public DebugUDPListener(String name, Logger logger) {
		this.name = name;
		this.logger = logger;
	}
	
	@Override
	public void handleOrderedTransmission(Object iterPkg) {
		logger.log("["+name+"]: Package received ordered: " + iterPkg);
	}
	
	@Override
	public void handleUnorderedTransmission(Object unorderedPkg) {
		logger.log("["+name+"]: Package received unordered: " + unorderedPkg);
	}

	@Override
	public void handleNotAckedTransmission(Object timedoutPkg) {
		logger.log("["+name+"]: Package timedout: " + timedoutPkg);
	}
	
	@Override
	public void handleAckedTransmission(Object ackedPkg) {
		logger.log("["+name+"]: Package delivered: " + ackedPkg);
	}
	
	@Override
	public void handleTransmissionRequest() {
		logger.log("["+name+"]: Null transmission request: ");
	}

	@Override
	public void handleTransmissionRequests(Collection<? extends MultiKeyValue> retransmitDatas) {
		logger.log("["+name+"]: Package retransmission requests: " + Arrays.deepToString(retransmitDatas.toArray()));
	}
	


}
