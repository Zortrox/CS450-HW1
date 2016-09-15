/**
 * Created by Zortrox on 9/13/2016.
 */

public class ServerProgram {

	public static void main(String[] args) {
		if (args.length > 0) {
			boolean useUDP = (args[0].toLowerCase().equals("-udp"));
			int locPos = args.length > 1 ? 1 : 0;
			String loc = args[locPos];
			int port = Integer.parseInt(loc.substring(loc.indexOf(':') + 1));
			String IP = loc.substring(0, loc.indexOf(':'));

			if (useUDP) System.out.println("Starting Server in UDP\n");
			else System.out.println("Starting Server in TCP\n");

			NetServer server = new NetServer(useUDP, port, IP);
			server.start();
		}
	}
}
