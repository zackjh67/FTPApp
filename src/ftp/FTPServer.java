package ftp;

import java.io.*;
import java.net.*;
import java.util.*;
//todo.................
//todo.................

class FTPServer {
    byte[] data;
    int connSockNum = 8415;

    public static void main(String argv[]) throws Exception {

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
    }
}

    //class for handling individual clients
    class FTPHandler extends Thread {
        private Socket client;
        private Scanner input;
        private PrintWriter output;
        DataOutputStream outToClient;
        BufferedReader inFromClient;
        String fromClient;
        StringTokenizer tokens;
        String firstLine;
        String clientCommand;
        boolean quit = false;
        //Socket dataSocket = null;
        int port;
        Socket connectionSocket;

        public FTPHandler(Socket connectionSocket) throws Exception {
            this.connectionSocket = connectionSocket;


            outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());

            inFromClient = new BufferedReader(new
                    InputStreamReader(connectionSocket.getInputStream()));
        }

        public void run() {
                do {
                    try {
                        System.out.println("under try");
                        fromClient = inFromClient.readLine();
                        System.out.println("fromClient: " + fromClient);


                        tokens = new StringTokenizer(fromClient);
                        //first line is the port number
                        firstLine = tokens.nextToken();
                        System.out.println("firstLine: " + firstLine);
                        port = Integer.parseInt(firstLine);
                        System.out.println("port: " + port);
                        //second line is the command
                        clientCommand = tokens.nextToken();
                        System.out.println("clientCommand: " + clientCommand);

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
                    } catch(Exception e){
                        System.out.println(e);
                        //stop loop, might change later
                        quit = true;
                    }
                } while (quit == false);
        }

        private void listCommand(Socket connectionSocket, int port) throws Exception {
        Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
        DataOutputStream dataOutToClient =
                new DataOutputStream(dataSocket.getOutputStream());

        //read files into file array
        File folder = new File("./");
        File[] listOfFiles = folder.listFiles();

        //iterate through each file and print name to output stream
        for (int i = 0; i < listOfFiles.length; i++) {
            System.out.println("File " + listOfFiles[i].getName());
            //can add this if statement  to only recognize files and not dirs
            //if (listOfFiles[i].isFile()) {
            dataOutToClient.writeUTF("File " + listOfFiles[i].getName());
            //}
        }
        //todo fix this to real EOF if thats a thing
            dataOutToClient.writeUTF(" ");
        dataSocket.close();
        System.out.println("List Data Socket closed");
    }

    private void retrCommand(Socket connectionSocket, int port)throws Exception{

    }

    private void storCommand(Socket connectionSocket, int port)throws Exception{

    }

    private void quitCommand(Socket connectionSocket, int port)throws Exception{

    }
}
