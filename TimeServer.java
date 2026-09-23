import java.net.*; // DatagramSocket, DatagramPacket, InetAddress - UDP networking classes
import java.util.Date; // Date - used to fetch the current system time
public class TimeServer {
public static void main(String[] args) throws Exception {
// Create a UDP socket bound to port 9876.
// Unlike TCP, this single socket is used to both receive
// requests from, and send replies to, every client.
DatagramSocket serverSocket = new DatagramSocket(9876);
System.out.println("Concurrent UDP Time Server Started..."); // confirmation message
// Buffer used to hold the bytes of each incoming request packet.
byte[] receiveBuffer = new byte[1024];
// Infinite loop - the server must keep running and keep
// accepting new requests for as long as it is alive.
while (true) {
// A fresh packet object is created for each iteration so
// that one client's data can never overwrite another's
// mid-flight.
DatagramPacket requestPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
// receive() BLOCKS (waits) until a UDP packet arrives.
// Unlike TCP's accept(), no connection is established -
// each packet is independent, and requestPacket now also
// holds the sender's address and port.
serverSocket.receive(requestPacket);
System.out.println("Request received from " + requestPacket.getAddress()
+ ":" + requestPacket.getPort());
// Hand this request to a new thread so the main loop is
// freed immediately and can go back to receive() the next
// request, while this thread replies to the current one.
RequestHandler handler = new RequestHandler(serverSocket, requestPacket);
handler.start();
}
}
}
class RequestHandler extends Thread { // extends Thread => can run concurrently
DatagramSocket socket; // the shared UDP socket used to send the reply
DatagramPacket requestPacket; // the request this thread is responsible for
RequestHandler(DatagramSocket socket, DatagramPacket requestPacket) {
this.socket = socket;
this.requestPacket = requestPacket;
}
public void run() {
try {
InetAddress clientAddress = requestPacket.getAddress();
int clientPort = requestPacket.getPort();
// Fetch the current system date and time as a String.
String timeString = new Date().toString();
// Convert the time String into raw bytes, since UDP
// packets carry byte arrays, not Java objects or text.
byte[] sendData = timeString.getBytes();
// Build the reply packet: the data, its length, and the
// exact address/port pair to deliver it to.
DatagramPacket responsePacket =
new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
// The socket is shared by every RequestHandler thread, so
// synchronizing on it prevents two threads from sending at
// the exact same instant and corrupting each other's data.
synchronized (socket) {
socket.send(responsePacket);
}
System.out.println("Time sent to " + clientAddress + ":" + clientPort
+ " -> " + timeString);
} catch (Exception e) {
// A send failure (e.g. client unreachable) is reported but
// does not crash the server or affect other threads.
e.printStackTrace();
}
}
}