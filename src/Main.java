import javax.swing.*;

/**
 * Created by Zortrox on 9/12/2016.
 */
public class Main {

	public static void main(String[] args) {

		boolean useUDP = false;
		int port = 6789;

		ImgClient client = new ImgClient(useUDP, port);
		ImgServer server = new ImgServer(useUDP, port);

		client.start();
		server.start();

		System.out.println("Hello, world");
	}
}
