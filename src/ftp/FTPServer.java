package ftp;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
//todo.................
//todo.................

class FTPServer {
    byte[] data;
    int connSockNum = 8415;

    public static void main(String argv[]) throws Exception {

        //socket outside of while loop for listening
        ServerSocket welcomeSocket = new ServerSocket(8415);

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
        String filename;
        String clientSentence;

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
                            String fileName = tokens.nextToken();
                            storCommand(connectionSocket, port, outToClient, fileName);
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

        //establish the data socket connection
        Socket dataSocket = new Socket(connectionSocket.getInetAddress(), port);
        DataOutputStream dataOutToClient =
                new DataOutputStream(dataSocket.getOutputStream());

        //get the filename from the user input
        try {
            filename = tokens.nextToken().toString();
        } catch (NoSuchElementException e) {
            System.out.println("No filename specified");
            return;
        }

        //read files into file array
        Path filepath = Paths.get("./" + filename);
        File folders = new File(filepath.toString());

        //Checks to see if the file exists in current directory
        if(folders.exists()){

            //Writes successful status code
            dataOutToClient.writeUTF("200 OK");

            //create fileReader to read the file line by line
            FileReader fileReader = new FileReader(folders);
            BufferedReader buffReader = new BufferedReader(fileReader);
            StringBuffer strBuffer = new StringBuffer();

            //Iterates through file line by line until end of file is reached
            try {
                while ((clientSentence = buffReader.readLine()) != null) {
                    dataOutToClient.writeUTF(clientSentence + System.getProperty("line.separator"));
                }

                //write the end of file indicator then close output stream
                dataOutToClient.writeUTF("!EOF!");
                dataOutToClient.close();

                //Close file reader and acknowledge successful download
                fileReader.close();
                System.out.println("File Downloaded Successfully!");

            } catch(IOException e) {
                e.printStackTrace();
            }
        }else {
            //Writes unsuccessful status code
            dataOutToClient.writeUTF("550 ERROR");
        }

        //terminates the data socket after request
        dataSocket.close();
        System.out.println("Retrieve Data Socket closed");

    }

        private void storCommand(Socket connectionSocket, int dataPort, DataOutputStream clientOut, String fileName)throws Exception{
            System.out.println("UNDER STOR");
            System.out.println("Port: " + dataPort);
            System.out.println("Filename: " + fileName);

            Path filePath = Paths.get("./" + "testStored.txt");

            Socket dataSocket = new Socket(connectionSocket.getInetAddress(), dataPort);
            DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();


            try {
                String line;
                while ( !(line = inData.readUTF()).equals("!EOF!")) {
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


            return;
        }

    private void quitCommand(Socket connectionSocket, int port)throws Exception{

    }
}
