import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.ddns.mucaho.jnetrobust.controller.Controller;
import net.ddns.mucaho.jnetrobust.data.Packet;
import net.ddns.mucaho.jnetrobust.util.Config;
import net.ddns.mucaho.jnetrobust.util.DebugUDPListener;



public class MiscTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		try {
			runMe();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void runMe() throws Exception {
		final Controller handlerA = new Controller(new Config(new DebugUDPListener("A")));
		final Controller handlerB = new Controller(new Config(new DebugUDPListener("B")));
		for (int i=0; i< packetAmount; i++) {
			final Packet pkg = (Packet) handlerA.send(""+i).clone();
			System.out.println("[A]>: "+pkg.getData().getValue());
			schedule(new Runnable() {				
				@Override
				public void run() {
					Object out = handlerB.receive(pkg);
					System.out.println(">[B]: "+out);
					final Packet pkg = (Packet) handlerB.send(out).clone();
					System.out.println("[B]>: "+pkg.getData().getValue());
					try {
						Thread.sleep(packetInterval);
					} catch (InterruptedException e) { e.printStackTrace();}
					schedule(new Runnable(){
						@Override
						public void run() {
							Object out = handlerA.receive(pkg);
							System.out.println(">[A]: "+out);
						}
					});
				}
			});
			Thread.sleep(packetInterval);
		}
		
		
		Thread.sleep(8000);
		Packet pkg; Object out;
		
		pkg = (Packet) handlerA.send("FINISH1").clone();
		System.out.println("[A]>: "+pkg.getData().getValue());
		out = handlerB.receive(pkg);
		System.out.println(">[B]: "+out);
		
		pkg = (Packet) handlerB.send("FINISH2").clone();
		System.out.println("[B]>: "+pkg.getData().getValue());
		out = handlerA.receive(pkg);
		System.out.println(">[A]: "+out);
		
		pkg = (Packet) handlerA.send("FINISH3").clone();
		System.out.println("[A]>: "+pkg.getData().getValue());
		out = handlerB.receive(pkg);
		System.out.println(">[B]: "+out);
	}
	
	// this is needed, else b does not get all delivered info:
	// coz A does not get all B messages before A finishes
	
	// make continuous A and B threads which send all the time @30fps
	// submit tasks to them, when tasks empty they send null
	// OR
	// make auto reply even if not sending messages!!!! (with null)
	// therefor count partners messages, if msgs > 32/x -> reply with null
	
	static int packetAmount = 500;
	static int packetInterval = (1000 / 16);
	static float lossPercentage = 0f;
	static int lagMillisMin = 100;
	static int lagMillisMax = 250;
	static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);
	
	public static void schedule(final Runnable runnable) {
		threadPool.schedule(new Runnable() {
			@Override
			public void run () {
				runnable.run();
			}
		}, calculateLag(), TimeUnit.MILLISECONDS);
	}
	private static long calculateLag() {
		return lagMillisMin + (int)(Math.random() * (lagMillisMax - lagMillisMin));
	}

}
