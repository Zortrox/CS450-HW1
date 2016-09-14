/**
 * Created by Zortrox on 9/13/2016.
 */

import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.concurrent.*;

public class NetServer extends NetObject {
	private BlockingQueue<Socket> qSockets = new LinkedBlockingQueue<>();
	private Thread tSockets;

	private BlockingQueue<DatagramPacket> qPackets = new LinkedBlockingQueue<>();
	private Thread tPackets;

	NetServer(boolean UDP, int port, String IP) {
		mUDP = UDP;
		mPort = port;
		mIP = IP;

		JFrame frame = new JFrame("Server");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(300, 200));
		frame.setVisible(true);
	}

	public void run() {
		if (mUDP) {
			try {
				UDPConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				TCPConnection();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void UDPConnection() throws Exception {
		DatagramSocket serverSocket = new DatagramSocket(mPort);

		//queue packets
		tPackets = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("<server>: Listening for packets.");

					while (true) {
						byte[] receiveData = new byte[PACKET_SIZE];
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						serverSocket.receive(receivePacket);

						qPackets.put(receivePacket);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		tPackets.start();

		while(true)
		{
			//receive data
			DatagramPacket receivePacket = qPackets.take();
			System.out.println("<server>: New Packet.");
			Message msg = new Message();
			//process packet into the message
			processUDPData(receivePacket, msg);

			switch (msg.mType) {
				case MSG_INIT:
					String strInitial = new String(msg.mData);
					msg.mData = ("Message received, " +
							strInitial.substring(strInitial.lastIndexOf(' ') + 1)).getBytes();
					sendUDPData(serverSocket, msg);
					break;
				case MSG_TEXT:
					String recMsg = new String(msg.mData);
					msg.mData = recMsg.toUpperCase().getBytes();
					sendUDPData(serverSocket, msg);
					break;
				case MSG_FILE:
					String filename = new String(msg.mData);
					sendFile(serverSocket, msg, filename);
					System.out.println("Sent: " + filename);
					break;
			}
		}
	}

	private void TCPConnection() throws Exception{
		//queue up new requests
		tSockets = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("<server>: Listening for connections.");

					ServerSocket serverSocket = new ServerSocket(mPort);

					while (true) {
						Socket newSocket = serverSocket.accept();
						qSockets.put(newSocket);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		tSockets.start();

		//process requests
		while (true)
		{
			Socket clientSocket = qSockets.take();
			System.out.println("<server>: New connection.");

			Message msg = new Message();
			receiveTCPData(clientSocket, msg);

			switch (msg.mType) {
				case MSG_INIT:
					String strInitial = new String(msg.mData);
					msg.mData = ("Message received, " +
							strInitial.substring(strInitial.lastIndexOf(' ') + 1)).getBytes();
					sendTCPData(clientSocket, msg);
					break;
				case MSG_TEXT:
					String recMsg = new String(msg.mData);
					msg.mData = recMsg.toUpperCase().getBytes();
					sendTCPData(clientSocket, msg);
					break;
				case MSG_FILE:
					String filename = new String(msg.mData);
					sendFile(clientSocket, msg, filename);
					break;
			}
		}
	}
}
