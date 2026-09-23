import java.net.*; // DatagramSocket, DatagramPacket, InetAddress - UDP networking classes
import java.util.Scanner; // Scanner - to read what the user types
public class TimeClient {
public static void main(String[] args) throws Exception {
// Create a UDP socket for this client. No connection is made
// yet - a plain DatagramSocket can send to, and receive from,
// any address.
DatagramSocket clientSocket = new DatagramSocket();
Scanner sc = new Scanner(System.in);
System.out.print("Enter server IP address: ");
String serverIP = sc.nextLine();
System.out.print("Enter server port number: ");
int serverPort = Integer.parseInt(sc.nextLine());
// Resolve the server's IP address so it can be attached to
// the outgoing packet.
InetAddress serverAddress = InetAddress.getByName(serverIP);
// The request itself just needs to be non-empty; the server
// only cares that a packet arrived, not what it says.
String requestMsg = "TIME_REQUEST";
byte[] sendData = requestMsg.getBytes();
// Build the request packet: the data, its length, and the
// server's address/port to deliver it to.
DatagramPacket requestPacket =
new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
// send() hands the packet to the network immediately - UDP
// does not wait for any handshake or acknowledgement.
clientSocket.send(requestPacket);
// Buffer to hold whatever bytes the server sends back.
byte[] receiveBuffer = new byte[1024];
DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
// receive() BLOCKS until the server's reply packet arrives.
clientSocket.receive(responsePacket);
// Convert only the bytes actually written (getLength()) back
// into a readable String.
String timeReceived = new String(responsePacket.getData(), 0, responsePacket.getLength());
System.out.println("Time received from server: " + timeReceived);
// Release the socket and its OS resources.
clientSocket.close();
}
}