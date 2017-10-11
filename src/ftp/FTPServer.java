package ftp;

import java.io.*;
import java.net.*;
import java.util.*;
//todo.................
//todo.................

/*********************************************
 * NOTE: THIS IS FIRST BEING IMPLEMENTED W/O MULTITHREADING
 *
 */
class FTPServer {
    byte[] data;


    public static void main(String argv[]) throws Exception {

        //try {
        //socket outside of while loop for listening
        ServerSocket welcomeSocket = new ServerSocket(12000);

        while (true) {
            //listening socket
            Socket connectionSocket = welcomeSocket.accept();
            //handles individual ftp clients
            FTPHandler handler = new FTPHandler(connectionSocket);
            //start thread
            handler.start();
        }

//        } catch (Exception e) {
//            System.err.println(e);
//        }
    }

    //class for handling individual clients
    class FTPHandler extends Thread {
        private Socket client;
        private Scanner input;
        private PrintWriter output;

        public FTPHandler(Socket connectionSocket) throws Exception {

            DataOutputStream outToClient;
            BufferedReader inFromClient;
            String fromClient;
            StringTokenizer tokens;
            String firstLine;
            String clientCommand;
            boolean quit = false;
            //Socket dataSocket = null;
            int port;

            outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());

            inFromClient = new BufferedReader(new
                    InputStreamReader(connectionSocket.getInputStream()));
            while(!quit) {
                fromClient = inFromClient.readLine();

                tokens = new StringTokenizer(fromClient);

                firstLine = tokens.nextToken();
                port = Integer.parseInt(firstLine);
                clientCommand = tokens.nextToken();

                //handle each different command here
                if (clientCommand.toUpperCase().equals("LIST:")) {
                    listCommand(connectionSocket, port);
                }
                if (clientCommand.toUpperCase().equals("RETR:")) {
                    retrCommand(connectionSocket, port);
                }
                if (clientCommand.toUpperCase().equals("STOR:")) {
                    storCommand(connectionSocket, port);
                }
                if (clientCommand.toUpperCase().equals("QUIT:")) {
                    quitCommand(connectionSocket, port);
                }
            }




            //below is original clienthandler code/////////////////////////////////////////////////////
            //Set up reference to associated socket...
            client = connectionSocket;

            try {
                input = new Scanner(client.getInputStream());
                output = new PrintWriter(
                        client.getOutputStream(), true);
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }
        }

        @Override
        public void run() {
            String received;
            do {
                //Accept message from client on
                //the socket's input stream...
                received = input.nextLine();

                //Echo message back to client on
                //the socket's output stream...
                output.println("ECHO: " + received);

                //Repeat above until 'QUIT' sent by client...
            } while (!received.equals("QUIT"));

            try {
                if (client != null) {
                    System.out.println(
                            "Closing down connection...");
                    client.close();
                }
            } catch (IOException ioEx) {
                System.out.println("Unable to disconnect!");
            }
        }
    }

    private void listCommand(Socket connectionSocket, int port) throws Exception{
        Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
        DataOutputStream dataOutToClient =
                new DataOutputStream(dataSocket.getOutputStream());
        //todo..........................

        dataSocket.close();
        System.out.println("Data Socket closed");
    }

    private void retrCommand(Socket connectionSocket, int port)throws Exception{

    }

    private void storCommand(Socket connectionSocket, int port)throws Exception{

    }

    private void quitCommand(Socket connectionSocket, int port)throws Exception{

    }
}
