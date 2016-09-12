/**
 * Created by Zortrox on 9/12/2016.
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ImgServer extends Thread{

	private boolean mUDP;
	private int serverPort;

	ImgServer(boolean UDP, int port) {
		mUDP = UDP;
		serverPort = port;

		JFrame frame = new JFrame("Server");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(300, 200));
		frame.setVisible(true);
	}

	public void run() {
		if (mUDP) {
			try {
				UDPConnection();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		else {
			try {
				TCPConnection();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void TCPConnection() throws IOException {
		String clientSentence;
		String capitalizedSentence;
		ServerSocket welcomeSocket = new ServerSocket(serverPort);

		while(true)
		{
			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient =
					new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			clientSentence = inFromClient.readLine();
			System.out.println("Received: " + clientSentence);
			capitalizedSentence = clientSentence.toUpperCase() + '\n';
			outToClient.writeBytes(capitalizedSentence);
		}
	}

	private void UDPConnection() throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(serverPort);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];

		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = new String( receivePacket.getData());
			System.out.println("RECEIVED: " + sentence);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			String capitalizedSentence = sentence.toUpperCase();
			sendData = capitalizedSentence.getBytes();
			DatagramPacket sendPacket =
					new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
		}
	}
}
