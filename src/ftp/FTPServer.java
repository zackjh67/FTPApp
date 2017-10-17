package ftp;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/****************************************************************
 * Server application that can connect to several clients at a
 * time using multithreading, request a list of the server's
 * files, retrieve a specified file, from the server,
 * and send and store a specified file to the server.
 *
 * @author Mike Ames
 * @author Phil "The Chosen One" Garza aka Dipstick
 * @author Zachary Hern
 * @author Adam Slifco
 *
 * @version October 2017
 ******************************************************************/
class FTPServer {
    /* socket the sever uses */
    static final int connSockNum = 8415;

    /******************************************************************
     * Main method for running program based on commands.
     ******************************************************************/
    public static void main(String argv[]) throws Exception {

        // socket outside of while loop for listening
        ServerSocket welcomeSocket = new ServerSocket(connSockNum);
        System.out.println("FTP Server Stared on Port 8415");
        
        // infinite loop to constantly service clients
        while (true) {
            // connection socket
            System.out.println("Waiting for connection");
            Socket connectionSocket = welcomeSocket.accept();
            // handles individual ftp clientsls
            FTPHandler handler = new FTPHandler(connectionSocket);
            //Output Connection
            System.out.println("Serving connection " + connectionSocket.getInetAddress() +
                " on Port: " + connectionSocket.getPort());
            // start thread
            handler.start();
        }
    }
}

/******************************************************************
 * Inner class to handle multiple clients on threads.
 ******************************************************************/
class FTPHandler extends Thread {
    /* EOF character*/
    static final String EOF = "!EOF!";

    /* server file path */
    static final String serverFilePath = System.getProperty("user.dir").toString()+ 
            File.separator + "server_files" + File.separator;

    /* data stream to client */
     DataOutputStream outToClient;

    /* reads data from client */
     BufferedReader inFromClient;

    /* string sent from client */
     String fromClient;

    /* tokenizer for fromClient */
     StringTokenizer tokens;

    /* first line of string */
     String firstLine;

    /* sent command from client */
     String clientCommand;

    /* name of specified file*/
     String filename;

    /* client data string */
     String clientSentence;

    /* boolean for quitting actoins */
     boolean quit = false;

    /* connection port */
     int port;

    /* connection socket between server and client */
     Socket connectionSocket;

    /******************************************************************
     * Handles each client connection socket.
     *
     * @param connectionSocket connection socket between server and client
     ******************************************************************/
    public FTPHandler(Socket connectionSocket) throws Exception {
        this.connectionSocket = connectionSocket;
        outToClient =
                new DataOutputStream(connectionSocket.getOutputStream());
        inFromClient = new BufferedReader(new
                InputStreamReader(connectionSocket.getInputStream()));
        System.out.println("FTP Client connected " + connectionSocket.getInetAddress() +
                " Port:" + connectionSocket.getPort());
    }

    /******************************************************************
     * Runs thread.
     *
     * @return void
     ******************************************************************/
    public void run() {
        do {
            try {

                fromClient = inFromClient.readLine();
                System.out.println("fromClient: " + fromClient);


                tokens = new StringTokenizer(fromClient);
                //first line is the port number
                firstLine = tokens.nextToken();
                port = Integer.parseInt(firstLine);
                //second line is the command
                clientCommand = tokens.nextToken();
            
                //handle each different command here
                if (clientCommand.toUpperCase().equals("LIST")) {
                    listCommand(connectionSocket, port);
                }
                if (clientCommand.toUpperCase().equals("RETR")) {
                    retrCommand(connectionSocket, port);
                }
                if (clientCommand.toUpperCase().equals("STOR")) {
                    storCommand(connectionSocket, port);
                }
                if (clientCommand.toUpperCase().equals("CLOSE")) {
                    quitCommand(connectionSocket, port);
                    quit = true;
                }
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Connection Lost..");
                //stop loop, might change later
                quit = true;
            }
        } while (quit == false);
    }

    /******************************************************************
     * Lists all files in server directory.
     *
     * @param connectionSocket connection socket to client
     * @param port connection socket port
     *
     * @return void
     ******************************************************************/
    private void listCommand(Socket connectionSocket, int port) throws Exception {
        /* socket for data transfer*/
        Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
        DataOutputStream dataOutToClient =
                new DataOutputStream(dataSocket.getOutputStream());

        // read files into file array
        File folder = new File(serverFilePath);
        File[] listOfFiles = folder.listFiles();

        // iterate through each file and print name to output stream
        for (File myFile: listOfFiles){
            // only shows files, not directories
            if (myFile.isFile()) {
                dataOutToClient.writeUTF(myFile.getName());
            }
        }
        // end transaction
        dataOutToClient.writeUTF(EOF);
        dataSocket.close();
        System.out.println("List Data Socket closed");
    }

    /******************************************************************
     * Returns specified file to client.
     *
     * @param connectionSocket connection socket to client
     * @param port connection socket port
     *
     * @return void
     ******************************************************************/
    private void retrCommand(Socket connectionSocket, int port) throws Exception {

        // establish the data socket connection
        Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
        DataOutputStream dataOutToClient =
                new DataOutputStream(dataSocket.getOutputStream());

        // get the filename from the user input
        try {
            filename = tokens.nextToken().toString();
        } catch (NoSuchElementException e) {
            dataOutToClient.writeUTF("550 ERROR");
            dataSocket.close();
            return;
        }

        // read files into file array
        Path filepath = Paths.get(serverFilePath + filename);
        File folders = new File(filepath.toString());

        // Checks to see if the file exists in current directory
        if (folders.exists()) {

            // Writes successful status code
            dataOutToClient.writeUTF("200 OK");

            // create fileReader to read the file line by line
            FileReader fileReader = new FileReader(folders);
            BufferedReader buffReader = new BufferedReader(fileReader);

            // Iterates through file line by line until end of file is reached
            try {
                while ((clientSentence = buffReader.readLine()) != null) {
                    dataOutToClient.writeUTF(clientSentence + System.getProperty("line.separator"));
                }

                // write the end of file indicator then close output stream
                dataOutToClient.writeUTF(EOF);
                dataOutToClient.close();

                // Close file reader and acknowledge successful download
                fileReader.close();
                System.out.println("File Downloaded Successfully!");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Writes unsuccessful status code
            dataOutToClient.writeUTF("550 ERROR");
        }

        // terminates the data socket after request
        dataSocket.close();
        System.out.println("Retrieve Data Socket closed");

    }

    /******************************************************************
     * Stores file from client.
     *
     * @param connectionSocket connection socket to client
     * @param dataPort connection socket port
     *
     * @return void
     ******************************************************************/
    private void storCommand(Socket connectionSocket, int dataPort) throws Exception {
        
        //Create data socket and streams to client
        Socket dataSocket = new Socket(connectionSocket.getInetAddress(), dataPort);
        DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
        DataOutputStream clientOut = new DataOutputStream(dataSocket.getOutputStream());
        StringBuffer stringBuffer = new StringBuffer();
       

        // get the filename from the user input
        try {
            filename = tokens.nextToken().toString();
        } catch (NoSuchElementException e) {
            //Bad Filename From Client
            System.out.print("Port:" + dataSocket.getPort() + " Bad Filename");
            clientOut.writeUTF("550 ERROR");
            dataSocket.close();
            return;
        }

        Path filePath = Paths.get(serverFilePath + "OUT_" + filename);
    
        try {
            String line;
            while (!(line = inData.readUTF()).equals(EOF)) {
                stringBuffer.append(line);
            }
            Files.write(filePath, stringBuffer.toString().getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        File file = new File(filePath.toString());
        if (file.exists()) {
            clientOut.writeUTF("200 OK");
        } else {
            clientOut.writeUTF("500 Error");
        }
        dataSocket.close();
        System.out.println("Data Socket Closed");
        return;
        }

    /******************************************************************
     * Quits connection to client.
     *
     * @param connectionSocket connection socket to client
     * @param port connection socket port
     *
     * @return void
     ******************************************************************/
    private void quitCommand(Socket connectionSocket, int port) throws Exception {
        try{
            System.out.println("Connection closed to client " + connectionSocket.getInetAddress()
                     + " Port:" + connectionSocket.getPort());
            connectionSocket.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
