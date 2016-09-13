/**
 * Created by Zortrox on 9/13/2016.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class NetObject extends Thread{
	public static final int NET_SERVER = 0;
	public static final int NET_CLIENT = 1;

	int mType;
	boolean mUDP;
	private int mPort;
	String mIP;

	NetObject(int netType, boolean UDP, int port, String IP) {
		mType = netType;
		mUDP = UDP;
		mPort = port;
		mIP = IP;

		JFrame frame;
		if (mType == NET_SERVER) {
			frame = new JFrame("Server");
		}
		else if (mType == NET_CLIENT) {
			frame = new JFrame("Client");
		}
		else return;

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(300, 200));
		frame.setVisible(true);
	}

	public void run() {
		if (mType == NET_SERVER || mType == NET_CLIENT) {
			if (mUDP) {
				try {
					UDPConnection();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} else {
				try {
					TCPConnection();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private byte[] receiveTCPData(Socket socket) throws IOException{
		DataInputStream inData = new DataInputStream(socket.getInputStream());

		//get size of receiving data
		byte[] byteSize = new byte[4];
		inData.readFully(byteSize);
		ByteBuffer bufSize = ByteBuffer.wrap(byteSize);
		int dataSize = bufSize.getInt();

		//receive data
		byte[] receiveData = new byte[dataSize];
		inData.readFully(receiveData);

		return receiveData;
	}

	private void sendTCPData(Socket socket, byte[] sendData) throws IOException{
		DataOutputStream outData = new DataOutputStream(socket.getOutputStream());

		//send size of data
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(sendData.length);
		byte[] dataSize = b.array();
		outData.write(dataSize);

		//send data
		outData.write(sendData);
	}

	private void TCPConnection() throws IOException {
		if (mType == NET_CLIENT) {
			Socket socket = new Socket(mIP, mPort);
			//initialize message
			String msgSend = "Hello, this is Matthew.";
			sendTCPData(socket, msgSend.getBytes());
			String msgReceive = new String(receiveTCPData(socket));
			System.out.println("<server>: " + msgReceive);
			socket.close();

			socket = new Socket(mIP, mPort);
			//custom message
			msgSend = JOptionPane.showInputDialog("What is your message?");
			sendTCPData(socket, msgSend.getBytes());
			msgReceive = new String(receiveTCPData(socket));
			System.out.println("<server>: " + msgReceive);
			socket.close();
		}
		else if (mType == NET_SERVER) {
			ServerSocket serverSocket = new ServerSocket(mPort);

			//initialize message

			while (true)
			{
				Socket clientSocket = serverSocket.accept();
				byte[] dataReceive = receiveTCPData(clientSocket);
				String msgReceive = new String(dataReceive);

				byte[] dataSend;
				if (msgReceive.substring(0, msgReceive.lastIndexOf(' ')).equals("Hello, this is")) {
					String msgSend = "Message received, " + msgReceive.substring(msgReceive.lastIndexOf(' ') + 1);
					dataSend = msgSend.getBytes();
				} else {
					dataSend = msgReceive.toUpperCase().getBytes();
				}

				sendTCPData(clientSocket, dataSend);
			}
		}
	}

	private void UDPConnection() throws IOException {
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(mIP);

		String strInitial = "Hello, this is Matthew";

		//sending data
		byte[] sendData = new byte[1024];
		sendData = strInitial.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, mPort);
		clientSocket.send(sendPacket);

		//receiving data
		byte[] receiveData = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);

		String strReceived = new String(receivePacket.getData());
		System.out.println("<server>: " + strReceived);


		clientSocket.close();
	}

	private void UDPConnectionu() throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(mPort);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];

		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);

			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			String strInitial = new String(receivePacket.getData());
			String strToSend = "Message received, " + strInitial.substring(strInitial.lastIndexOf(' ') + 1);

			sendData = strToSend.getBytes();
			DatagramPacket sendPacket =
					new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);

			serverSocket.close();
		}
	}
}
