
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class My_Server {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;
  //create Queue for wait exceeding thread
  public static Queue<Socket> queue = new ArrayDeque<Socket>();
// here maximum 10 number of clients can be connected
  private static final int maxClientsCount = 1;
  public static final clientThread[] threads = new clientThread[maxClientsCount];
  public static PrintStream os1;
  
  
  public static void main(String args[]) {

    // The default port number.
    int portNumber = 1978;
    if (args.length < 1) {
      System.out.println("Server Listning Port Number Is: " + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket, threads)).start();
            
            break;
          }
          
        }
        
        if (i == maxClientsCount) {
          os1 = new PrintStream(clientSocket.getOutputStream());
          os1.println("Server too busy. your request is waiting on a queue.");
          //os.close();
          queue.add(clientSocket);
          //clientSocket.close();
        }
       
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
  
}


class clientThread extends Thread {

  private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  public clientThread(Socket clientSocket, clientThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
    
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;
    My_Server ms= new My_Server();

    try {
    	
    	
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      while (true) {
        os.println("Enter your name.");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
        System.out.print(name+" Connected");
        System.out.printf("%n");
          break;
        } else {
          os.println("The name should not contain '@' character.");
        }
      }

      /* Welcome the new the client. */
      os.println("Welcome " + name
          + " you are now connected.\n To leave enter /disconnect in a new line.");
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] == this) {
            clientName = "@" + name;
            break;
          }
        }

      }
      
    while (true) {
        String line = is.readLine();
        if (line.startsWith("/disconnect")) {
          break;
        }

      }
      os.println(name +"  You are Disconnected");
      System.out.println(name+" Disconnected");
      
     
      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      if(ms.queue.isEmpty()==false)
      {
    	  
    	  clientSocket= ms.queue.peek();
    	  for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] == null) {
                (threads[i] = new clientThread(clientSocket, ms.threads)).start();
                //PrintStream os = new PrintStream(clientSocket.getOutputStream());
                ms.os1.println("You are connected to the server. connection made from queue");
                ms.os1.close();
                break;
              }
              
            }
      }

      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}





