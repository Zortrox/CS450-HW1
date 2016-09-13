/**
 * Created by Zortrox on 9/12/2016.
 */



public class Main {

	public static void main(String[] args) {

		boolean useUDP = false;
		int port = 6789;

		NetObject client = new NetObject(NetObject.NET_CLIENT, useUDP, port, "localhost");
		NetObject server = new NetObject(NetObject.NET_SERVER, useUDP, port, "localhost");

		client.start();
		server.start();

		System.out.println("Hello, world");
	}
}
