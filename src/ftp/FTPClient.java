package ftp;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.lang.*;

/****************************************************************
 * Client application that connects to a server, requests
 * a list of the server's files, retrieve a specified file
 * from the server, and send and store a specified file to
 * the server.
 *
 * @author Mike Ames
 * @author Phil Garza
 * @author Zachary Hern
 * @author Adam Slifco
 *
 * @version October 2017
 ******************************************************************/
class FTPClient {

    /* EOF character*/
    static final String EOF = "!EOF!";

    /* filepath for client files */
    static final String clientFilePath = "./res/client_files/";

    /******************************************************************
     * Main method for running program based on commands.
     ******************************************************************/
    public static void main(String argv[]) throws Exception {
        /* sentence from user */
        String sentence;

        /* if socket is still open */
        boolean isOpen = true;

        /* if client still connected */
        boolean clientgo = true;

        /* port to service*/
        int port;

        /* socket for passing data */
        Socket dataSocket = null;

        // user greeting
        System.out.println("Welcome!\nPlease connect to server with: " +
                "CONNECT <server name/IP address> <server port>" +
                "\nFor Example: CONNECT localhost 8415" +
                "\nThen type command from command list below" +
                "\nLIST (list files on server)" +
                "\nRETR <filename> (retrieves specified file from server)" +
                "\nSTOR <filename> (stores specified file on server)" +
                "\nQUIT (quits program)");

        /* User input */
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        sentence = inFromUser.readLine();
        /* tokenizer for user input */
        StringTokenizer tokens = new StringTokenizer(sentence);

        // check for "CONNECT" command ignoring case
        if (sentence.toUpperCase().startsWith("CONNECT")) {

            /* name of server */
            // pass the connect command
            String serverName = tokens.nextToken();
            serverName = tokens.nextToken();
            port = Integer.parseInt(tokens.nextToken());

            // connect message
            System.out.println("You are connected to " + serverName + "\n");

            // control socket for connection
            Socket ControlSocket = new Socket(serverName, port);

            // while the socket is open and client wishes to be connected
            while (isOpen && clientgo) {

                /* data passed to server */
                DataOutputStream controlOut =
                        new DataOutputStream(ControlSocket.getOutputStream());

                /* data passed to client */
                DataInputStream controlIn =
                        new DataInputStream(
                                new BufferedInputStream(
                                        ControlSocket.getInputStream()));

                // line typed by user
                sentence = inFromUser.readLine();

                // check for "LIST" command ignoring case
                if (sentence.toUpperCase().equals("LIST")) {
                    try {
                        list(port, sentence, controlOut, dataSocket);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    // checks for "RETR" command ignoring case
                } else if (sentence.toUpperCase().startsWith("RETR")) {
                    try {
                        retr(port, sentence, controlOut, dataSocket);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    // checks for "STOR" command ignoring case
                } else if (sentence.toUpperCase().startsWith("STOR")) {
                    try {
                        stor(port, sentence, controlOut, controlIn, dataSocket);
                    } catch (Exception e) {
                        System.out.println(e);
                        e.printStackTrace();
                    }
                } else if (sentence.toUpperCase().startsWith("QUIT")) {
                    quit(sentence, controlOut, ControlSocket);
                }

            }
            // if client has not connected yet
        } else if (sentence.toUpperCase().startsWith("LIST")
                || sentence.toUpperCase().startsWith("RETR")
                || sentence.toUpperCase().startsWith("STOR")
                || sentence.toUpperCase().startsWith("QUIT")) {
            System.out.println("Must be connected first (command:" +
                    "CONNECT <server name/IP address> <server port>");
            // if command doesn't match any known command
        } else {
            System.out.println("Not a valid command. Please connect first" +
                    "(command: CONNECT <server name/IP address> <server port>)"
            );
        }
    }

    /******************************************************************
     * Lists all files in server's directory.
     *
     * @param port connection socket port
     * @param sentence user input
     * @param controlOut output stream to server
     * @param dataSocket socket for sending/receiving data
     *
     * @return void
     ******************************************************************/
    private static void list(int port,
                             String sentence,
                             DataOutputStream controlOut,
                             Socket dataSocket) throws Exception {

        // Create server socket
        int dPort = port + 2;
        ServerSocket welcomeData = new ServerSocket(dPort);

        // write user sentence to server
        controlOut.writeBytes(dPort + " " + sentence + " " + '\n');

        // instantiate dataSocket
        dataSocket = welcomeData.accept();
        DataInputStream inData =
                new DataInputStream(
                        new BufferedInputStream(dataSocket.getInputStream()));
        try {
            /* first UTF line from server */
            String serverData = inData.readUTF();

            // start printing file list
            System.out.println("Files on server: \n" + serverData);

            // while server doesn't pass EOF character
            while (!serverData.equals(EOF)) {

                // continue reading each line and printing file name
                serverData = inData.readUTF();

                // dont print end of file character
                if (!serverData.equals(EOF))
                    System.out.println(serverData);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        welcomeData.close();
        dataSocket.close();
        endOfCommand();
    }

    /******************************************************************
     * Retrieves specified file from server.
     *
     * @param port connection socket port
     * @param sentence user input
     * @param controlOut output stream to server
     * @param dataSocket socket for sending/receiving data
     *
     * @return void
     ******************************************************************/
    private static void retr(int port,
                             String sentence,
                             DataOutputStream controlOut,
                             Socket dataSocket) throws Exception {
        // Create server socket
        int dPort = port + 2;
        ServerSocket welcomeData = new ServerSocket(dPort);
        controlOut.writeBytes(dPort + " " + sentence + " " + '\n');
        String[] getFileName = sentence.split(" ", 2);

        // Establish connection with data socket
        dataSocket = welcomeData.accept();
        // Read user input
        DataInputStream inData =
                new DataInputStream(
                        new BufferedInputStream(dataSocket.getInputStream()));
        StringBuffer stringBuffer = new StringBuffer();

        // get the Filepath
        Path filePath = Paths.get(clientFilePath + getFileName[1]);

        // Writes the file to the client
        try {
            // Retrieves the Status code
            String status = inData.readUTF().toString();
            System.out.println(status);

            // Checks to see if the file was found
            if (status.equals("200 OK")) {
                System.out.println("File Downloaded!");

                // Writes/downloads the file line by line
                String line;
                while (!(line = inData.readUTF()).equals(EOF)) {
                    stringBuffer.append(line);
                }

                System.out.println("Downloading File..." + "\n");
                Files.write(filePath, stringBuffer.toString().getBytes());

            } else {
                System.out.println("Error. File could not be downloaded.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // closes client side of the data socket after request
        welcomeData.close();
        dataSocket.close();
        endOfCommand();
    }

    /******************************************************************
     * Stores specified file on server.
     *
     * @param port connection socket port
     * @param sentence user input
     * @param controlOut output stream to server
     * @param controlIn input stream from server
     * @param dataSocket socket for sending/receiving data
     *
     * @return void
     ******************************************************************/
    private static void stor(int port,
                             String sentence,
                             DataOutputStream controlOut,
                             DataInputStream controlIn,
                             Socket dataSocket) throws Exception {

        int dPort = port + 2;

        StringTokenizer tokens = new StringTokenizer(sentence);
        String fileName;

        // get the filename from the user input
        try {
            fileName = tokens.nextToken(); // pass the connect command
            fileName = tokens.nextToken();
        } catch (NoSuchElementException e) {
            System.out.println("No filename specified");
            return;
        }

        // Create a file object with the path of the file. Some code from:
        // http://www.avajava.com/tutorials/lessons/
        // how-do-i-read-a-string-from-a-file-line-by-line.html
        File file = new File(clientFilePath + fileName);

        if (!file.exists() || file.isDirectory()) {
            System.out.println("No such file or directory");
            return;
        }

        //Create a socket to listen on
        ServerSocket welcomeData = new ServerSocket(dPort);
        //Pass the server the port the client is listening on, and the command
        controlOut.writeBytes(dPort + " " + sentence + " " + '\n');

        //check to see if the server successfully created a file to save the content to
        String serverFileCreationError = controlIn.readUTF().toString();
        if (serverFileCreationError.equals("550 Error")) {
            System.out.println("Error creating the file on the server side. Exiting");
            endOfCommand();
            welcomeData.close();
            dataSocket.close();
            return;
        }
        //Accept the data socket from the server
        dataSocket = welcomeData.accept();

        DataOutputStream dataControlOut =
                new DataOutputStream(dataSocket.getOutputStream());

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line;

        //Read from the file and store in a String Buffer
        try {
            while ((line = bufferedReader.readLine()) != null) {
                dataControlOut.writeUTF(line +
                        System.getProperty("line.separator"));
            }
            dataControlOut.writeUTF(EOF);
            fileReader.close();
            System.out.println("Uploading file . . .");
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

        DataInputStream inFromServer = new DataInputStream(dataSocket.getInputStream());
        String status = inFromServer.readUTF().toString();

        if (status.equals("200 OK")) {
            System.out.println("File Uploaded!");
        } else {
            System.out.println("Error. File not uploaded.");
        }
        inFromServer.close();
        welcomeData.close();
        dataControlOut.close();
        dataSocket.close();
        endOfCommand();
        return;
    }

    private static void quit(String sentence, DataOutputStream controlOut, Socket controlSocket) {
        try {
            controlOut.writeBytes(1 + " QUIT");
            //controlSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //controlSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("The Connection has been severed. Restarting client.");
        System.out.println();
        System.out.println();

        try {
            String[] args = {};
            main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    /*****************************************************************
     * prints command list upon completion of each command
     *
     * @return void
     ******************************************************************/
    private static void endOfCommand() {
        System.out.println("\nWhat would you like to do next?" +
                "\nLIST (list files on server)" +
                "\nRETR <filename> (retrieves specified file from server)" +
                "\nSTOR <filename> (stores specified file on server)" +
                "\nQUIT (quits program)");
    }
}