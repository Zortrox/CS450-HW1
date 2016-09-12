/**
 * Created by Zortrox on 9/12/2016.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

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
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String name = JOptionPane.showInputDialog(frame, "What's your name?");
			};
		});

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
		sentence = inFromUser.readLine();
		outToServer.writeBytes(sentence + '\n');

		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(0xAABBCCDD);
		byte[] dataSize = b.array();
		ByteBuffer buf = ByteBuffer;

		//read first 4 bytes (full int)

		DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
		byte[] receiveData = new byte[1024];
		inFromServer.readFully(receiveData);
		modifiedSentence = new String(receiveData);


		System.out.println("FROM SERVER: " + modifiedSentence);
		clientSocket.close();
	}

	private void UDPConnection() throws IOException {
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("localhost");

		String strInitial = "Hello, this is Matthew";

		//sending data
		byte[] sendData = new byte[1024];
		sendData = strInitial.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, clientPort);
		clientSocket.send(sendPacket);

		//receiving data
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);

		String strReceived = new String(receivePacket.getData());
		System.out.println("<server>: " + strReceived);


		clientSocket.close();
	}
}
