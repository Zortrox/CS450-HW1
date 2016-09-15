/**
 * Created by Zortrox on 9/12/2016.
 */

public class ClientProgram {

	public static void main(String[] args) {
		if (args.length > 0) {
			boolean useUDP = (args[0].toLowerCase().equals("-udp"));
			int locPos = args.length > 1 ? 1 : 0;
			String loc = args[locPos];
			int port = Integer.parseInt(loc.substring(loc.indexOf(':') + 1));
			String IP = loc.substring(0, loc.indexOf(':'));

			if (useUDP) System.out.println("Starting Client in UDP\n");
			else System.out.println("Starting Client in TCP\n");

			NetClient client = new NetClient(useUDP, port, IP);
			client.start();
		}
	}
}
