/**
 * Created by Zortrox on 9/13/2016.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

class Message {
	byte mType = 0;
	byte[] mData;
	InetAddress mIP;
	int mPort;
}

public class NetObject extends Thread{
	int mType;
	boolean mUDP;
	int mPort;
	String mIP;
	String filePath;

	//packet size in bytes
	static final int PACKET_SIZE = 1024;

	//message types
	static final byte MSG_INIT 		= 0; //init connection
	static final byte MSG_TEXT 		= 1; //sending text
	static final byte MSG_FILE 		= 2; //sending file

	NetObject() {
		filePath = new File("").getAbsolutePath();
	}

	public void run() {	}

	void processUDPData(DatagramPacket packet, Message msg) {
		//store packet data
		msg.mData = packet.getData();

		//get size of receiving data
		byte[] byteSize = new byte[4];
		ByteBuffer bufSize = ByteBuffer.wrap(Arrays.copyOfRange(msg.mData, 0, byteSize.length));
		int dataSize = bufSize.getInt();

		//get type of receiving data
		msg.mType = msg.mData[byteSize.length];

		//get sent data from packet
		msg.mData = Arrays.copyOfRange(msg.mData, byteSize.length + 1, byteSize.length + 1 + dataSize);

		//get location from packet
		msg.mPort = packet.getPort();
		msg.mIP = packet.getAddress();
	}

	void receiveUDPData(DatagramSocket socket, Message msg) throws Exception{
		msg.mData = new byte[PACKET_SIZE];
		DatagramPacket receivePacket = new DatagramPacket(msg.mData, msg.mData.length);
		socket.receive(receivePacket);

		processUDPData(receivePacket, msg);
	}

	void sendUDPData(DatagramSocket socket, Message msg) throws Exception{
		//get size of data
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(msg.mData.length);
		byte[] dataSize = b.array();

		//create array of all data
		byte[] data = new byte[dataSize.length + 1 + msg.mData.length];
		System.arraycopy(dataSize, 0, data, 0, dataSize.length);
		data[dataSize.length] = msg.mType;
		System.arraycopy(msg.mData, 0, data, dataSize.length + 1, msg.mData.length);

		//send data
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, msg.mIP, msg.mPort);
		socket.send(sendPacket);
	}

	void receiveTCPData(Socket socket, Message msg) throws Exception{
		DataInputStream inData = new DataInputStream(socket.getInputStream());

		//get size of receiving data
		byte[] byteSize = new byte[4];
		inData.readFully(byteSize);
		ByteBuffer bufSize = ByteBuffer.wrap(byteSize);
		int dataSize = bufSize.getInt();

		//get message type
		msg.mType = inData.readByte();

		//receive data
		msg.mData = new byte[dataSize];
		inData.readFully(msg.mData);
	}

	void sendTCPData(Socket socket, Message msg) throws Exception{
		DataOutputStream outData = new DataOutputStream(socket.getOutputStream());

		//send size of data
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(msg.mData.length);
		byte[] dataSize = b.array();
		outData.write(dataSize);

		//send message type
		outData.writeByte(msg.mType);

		//send data
		outData.write(msg.mData);
		outData.flush();
	}

	void sendFile(Object socket, Message msg, String filename) {
		Path file = Paths.get(filePath + filename);
		try {
			byte[] fileData = Files.readAllBytes(file);

			msg.mType = MSG_FILE;
			msg.mData = fileData;

			if (mUDP) {
				sendUDPData((DatagramSocket) socket, msg);
			} else {
				sendTCPData((Socket) socket, msg);
			}

		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void receiveFile(Object socket, Message msg, String filename) {
		Path file = Paths.get(filePath + filename);

		try {
			if (mUDP) {
				receiveUDPData((DatagramSocket) socket, msg);
			} else {
				receiveTCPData((Socket) socket, msg);
			}

			Files.write(file, msg.mData);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
