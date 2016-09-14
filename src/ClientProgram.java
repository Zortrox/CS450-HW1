/**
 * Created by Zortrox on 9/12/2016.
 */

public class ClientProgram {

	public static void main(String[] args) {

		boolean useUDP = false;
		int port = 6789;

		NetClient client = new NetClient(useUDP, port, "localhost");
		client.start();
	}
}
