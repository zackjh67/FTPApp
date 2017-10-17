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
    static final String clientFilePath = "." + File.separator+ "client_files" 
        + File.separator;

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
        System.out.println("Welcome Team Awesome's Super Cool FTP Server!\n\n" +
                "Please connect to server with: " +
                "CONNECT <server name/IP address> <server port>" +
                "\nFor Example: CONNECT localhost 8415" +
                "\nThen type command from command list below" +
                "\nLIST (list files on server)" +
                "\nRETR <filename> (retrieves specified file from server)" +
                "\nSTOR <filename> (stores specified file on server)" +
                "\nCLOSE (closes connection and quits program)");

        /* User input */
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        // while the socket is open and client wishes to be connected
        while (clientgo) {
            //Get input from user
            sentence = inFromUser.readLine();
            /* tokenizer for user input */
            StringTokenizer tokens = new StringTokenizer(sentence);
            //Take first token as command
            String myCommand = tokens.nextToken().toUpperCase();
            if(myCommand.equals("CONNECT") && tokens.countTokens() == 2){

                /* name of server */
                //Secodn Token - Port Third
                String serverName = tokens.nextToken();
                port = Integer.parseInt(tokens.nextToken());


                Socket ControlSocket;
                try{
                    //Try to Create Control Socket for connection
                    ControlSocket = new Socket(serverName, port);

                    /* data passed to server */
                    DataOutputStream controlOut =
                            new DataOutputStream(ControlSocket.getOutputStream());

                    /* data passed to client */
                    DataInputStream controlIn =
                            new DataInputStream(
                                    new BufferedInputStream(
                                            ControlSocket.getInputStream()));

                    //If you made it this far your connected
                    System.out.println("Connected to Server.");
                    isOpen = true;
                    // line typed by user
                    while(isOpen){
                        sentence = inFromUser.readLine();
                        tokens = new StringTokenizer(sentence);
                        switch(tokens.nextToken().toUpperCase()){
                            case "LIST":
                                list(port,sentence,controlOut,dataSocket);
                                break;
                            case "LIST:":
                                list(port,sentence,controlOut,dataSocket);
                                break;
                            case "RETR":
                                retr(port,sentence,controlOut,dataSocket);
                                break;
                            case "RETR:":
                                retr(port,sentence,controlOut,dataSocket);
                                break;
                            case "STOR":
                                stor(port, sentence, controlOut, controlIn, dataSocket);
                                break;
                            case "STOR:":
                                stor(port, sentence, controlOut, controlIn, dataSocket);
                                break;
                            case "CLOSE":
                                quit(port,sentence, controlOut);
                                isOpen = false;
                                clientgo = false;
                                break;
                            case "CLOSE:":
                                quit(port,sentence, controlOut);
                                isOpen = false;
                                clientgo = false;
                                break;
                            default:
                                System.out.println("Invalid Command");
                        }

                    }
                } catch (Exception e){
                    //If you can't connect its a bad name or port
                    System.out.println("Server not available or Bad Name/Port");
                    System.out.println("You can try to connect again");
                    isOpen = false;
                }
            } else if (myCommand.equals("QUIT")|| myCommand.equals("CLOSE")){
                //No Connection Made. So nothing todo.
                System.out.println("GoodBye!");
                clientgo = false;
            } else if (myCommand.equals("CONNECT")){
                System.out.println("Not enough connection parameters");
            }
            else{
                System.out.println("You Need to connect to a server first");
            }
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
        System.out.println(clientFilePath);

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
                System.out.println("Downloading File...");

                // Writes/downloads the file line by line
                String line;
                while (!(line = inData.readUTF()).equals(EOF)) {
                    stringBuffer.append(line);
                }
                Files.write(filePath, stringBuffer.toString().getBytes());
                System.out.println("File Downloaded!");
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

    /******************************************************************
     * Quits the application
     *
     * @param port connection socket port
     * @param sentence user input
     * @param controlOut output stream to server
     *
     * @return void
     ******************************************************************/
    private static void quit(int port,String sentence,DataOutputStream controlOut) throws Exception {
        //Tells the Server to close the connection on the port.
        try{
            controlOut.writeBytes(port + " " + sentence + " " + "\n");
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            controlOut.close();
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
                "\nCLOSE (closes connection and quits program)");
    }
}
