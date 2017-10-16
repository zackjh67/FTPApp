package ftp;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.lang.*;

class FTPClient {

    public static void main(String argv[]) throws Exception {
        String sentence;
        String modifiedSentence;
        boolean isOpen = true;
        int number = 1;
        boolean notEnd = true;
        String statusCode;
        boolean clientgo = true;
        int port1;


        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        sentence = inFromUser.readLine();
        StringTokenizer tokens = new StringTokenizer(sentence);


        if (sentence.toUpperCase().startsWith("CONNECT")) {
            String serverName = tokens.nextToken(); // pass the connect command
            serverName = tokens.nextToken();
            port1 = Integer.parseInt(tokens.nextToken());
            System.out.println("You are connected to " + serverName);

            Socket ControlSocket = new Socket(serverName, port1);

            while (isOpen && clientgo) {

                DataOutputStream outToServer =
                        new DataOutputStream(ControlSocket.getOutputStream());

                DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

                sentence = inFromUser.readLine();

                if (sentence.toUpperCase().equals("LIST:")) {

                    port1 = port1 + 2;
                    ServerSocket welcomeData = new ServerSocket(port1);
                    outToServer.writeBytes(port1 + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    while (notEnd) {
                        modifiedSentence = "" + inData.readUTF();

                        if (modifiedSentence.equals(" "))
                            notEnd = false;
                        //todo remove this print statement i think
                        System.out.println("modified sentence: " + modifiedSentence);
                    }

                    welcomeData.close();
                    dataSocket.close();
                    //make notEnd true again
                    notEnd = true;
                    System.out.println("\nWhat would you like to do next: \n retr: file.txt || stor: file.txt  || close");

                } else if (sentence.toUpperCase().startsWith("RETR:")) {

                    //Create server socket
                    int dPort = port1 + 2;
                    ServerSocket welcomeData = new ServerSocket(dPort);
                    outToServer.writeBytes(dPort + " " + sentence + " " + '\n');

                    //Establish connection with data socket
                    Socket dataSocket = welcomeData.accept();
                    //Read user input
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    StringBuffer stringBuffer = new StringBuffer();

                    //get the Filepath
                    Path filePath = Paths.get("./" + "retrieveSuccess.txt");

                    //Writes the file to the client
                    try {

                        //Retrieves the Status code
                        String status = inData.readUTF().toString();
                        System.out.println(status);

                        //Checks to see if the file was found
                        if (status.equals("200 OK")) {
                            System.out.println("File Downloaded!");

                            //Writes/downloads the file line by line
                            String line;
                            while (!(line = inData.readUTF()).equals("!EOF!")) {
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


                    //closes client side of the data socket after request
                    welcomeData.close();
                    dataSocket.close();

                    //make notEnd true again for future use
                    notEnd = true;
                    System.out.println("\nWhat would you like to do next: \n retr: file.txt || stor: file.txt  || close");
                } else if (sentence.toUpperCase().startsWith("STOR:")) {
                    stor(port1, sentence, outToServer, inFromServer);
                }

            }
        }
    }

    private static void stor(int port1, String sentence, DataOutputStream controlOut, DataInputStream controlIn) throws Exception {
        int dataPortNum = 8417;
        StringTokenizer tokens = new StringTokenizer(sentence);
        String fileName;

        //get the filename from the user input
        try {
            fileName = tokens.nextToken(); // pass the connect command
            fileName = tokens.nextToken();
        } catch (NoSuchElementException e) {
            System.out.println("No filename specified");
            return;
        }

        //Create a file object with the path of the file. Some code from:
        //http://www.avajava.com/tutorials/lessons/how-do-i-read-a-string-from-a-file-line-by-line.html
        File file = new File("./" + fileName);

        if (!file.exists() || file.isDirectory()) {
            System.out.println("No such file or directory");
            return;
        }

        //Create a socket to listen on
        ServerSocket welcomeData = new ServerSocket(dataPortNum);
        //Pass the server the port the client is listening on, and the tcommand
        controlOut.writeBytes(dataPortNum + " " + sentence + " " + '\n');
        //Accept the data socket from the server
        Socket dataSocket = welcomeData.accept();
        DataOutputStream dataOutToServer =
                new DataOutputStream(dataSocket.getOutputStream());

        //Read from the file and store in a String Buffer
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        //StringBuffer stringBuffer = new StringBuffer();
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) {
                //line = String.format(line + "\n", System.getProperty("line.separator"));
                dataOutToServer.writeUTF(line + System.getProperty("line.separator"));
            }
            dataOutToServer.writeUTF("!EOF!");
            dataOutToServer.close();
            fileReader.close();
            System.out.println("Uploading file . . .");
            //dataOutToServer.writeUTF(stringBuffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String status = controlIn.readUTF().toString();
        if (status.equals("200 OK")) {
            System.out.println("File Uploaded!");
        } else {
            System.out.println("Error. File not uploaded.");
        }

    }
}