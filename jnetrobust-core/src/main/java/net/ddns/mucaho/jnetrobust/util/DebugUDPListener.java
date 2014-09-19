package net.ddns.mucaho.jnetrobust.util;

public class DebugUDPListener extends UDPListener {

	private final String name;
	private final Logger logger;
	
	public DebugUDPListener(String name) {
		this(name, new Logger() {
			@Override
			public void log(String... texts) {
                for (String text: texts)
				    System.out.print(text + "\t");
                System.out.println();
			}
		});
	}
	
	public DebugUDPListener(String name, Logger logger) {
		this.name = name;
		this.logger = logger;
	}
	
	@Override
	public void handleOrderedTransmission(Object iterPkg) {
		logger.log(name, "Package received ordered", iterPkg != null ? iterPkg.toString() : "null");
	}
	
	@Override
	public void handleUnorderedTransmission(Object unorderedPkg) {
        logger.log(name, "Package received unordered", unorderedPkg != null ? unorderedPkg.toString() : "null");
	}

	@Override
	public void handleNotAckedTransmission(Object timedoutPkg) {
        logger.log(name, "Package timed out", timedoutPkg != null ? timedoutPkg.toString() : "null");
    }
	
	@Override
	public void handleAckedTransmission(Object ackedPkg) {
		logger.log(name, "Package delivered", ackedPkg != null ? ackedPkg.toString() : "null");
	}
	


}
