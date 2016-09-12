/**
 * Created by Zortrox on 9/12/2016.
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ImgClient extends Thread{

	boolean mUDP;
	private int clientPort;

	ImgClient(boolean UDP, int port) {
		mUDP = UDP;
		clientPort = port;

		JFrame frame = new JFrame("Client");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(300, 200));
		frame.setVisible(true);

		JButton button = new JButton("New Message");
		button.addActionListener();

		frame.getContentPane().add(button);
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
		String sentence;
		String modifiedSentence;
		BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
		Socket clientSocket = new Socket("localhost", clientPort);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		sentence = inFromUser.readLine();
		outToServer.writeBytes(sentence + '\n');
		modifiedSentence = inFromServer.readLine();
		System.out.println("FROM SERVER: " + modifiedSentence);
		clientSocket.close();
	}

	private void UDPConnection() throws IOException {
		BufferedReader inFromUser =
				new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		String sentence = inFromUser.readLine();
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, clientPort);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		String modifiedSentence = new String(receivePacket.getData());
		System.out.println("FROM SERVER:" + modifiedSentence);
		clientSocket.close();
	}
}
