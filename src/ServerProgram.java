/**
 * Created by Zortrox on 9/13/2016.
 */

public class ServerProgram {

	public static void main(String[] args) {

		boolean useUDP = true;
		int port = 6789;

		NetServer server = new NetServer(useUDP, port, "localhost");
		server.start();

		NetClient client1 = new NetClient(useUDP, port, "localhost");
		client1.start();
		NetClient client2 = new NetClient(useUDP, port, "localhost");
		client2.start();
		NetClient client3 = new NetClient(useUDP, port, "localhost");
		client3.start();
		NetClient client4 = new NetClient(useUDP, port, "localhost");
		client4.start();
	}
}
