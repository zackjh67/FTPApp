package ftp;

import java.io.*;
import java.net.*;
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


        if (sentence.startsWith("connect")) {
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

                if (sentence.equals("list:")) {

                    port = port + 2;
                    System.out.println(port);
                    ServerSocket welcomeData = new ServerSocket(port);
                    outToServer.writeBytes(port + " " + sentence + " " + '\n');

                    Socket dataSocket = welcomeData.accept();
                    DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
                    while (notEnd) {
                        modifiedSentence = inData.readUTF();
                        //todo........................................
                        //todo........................................
                    }

                    welcomeData.close();
                    dataSocket.close();
                    System.out.println("\nWhat would you like to do next: \n retr: file.txt || stor: file.txt  || close");

                } else if (sentence.startsWith("retr: ")) {
                    //todo....................................................
                }
                else if (sentence.startsWith("stor: ")) {
                    port +=2;

                    ServerSocket sendData = new ServerSocket(port);
                    outToServer.writeBytes(port + "stor" + '\n');

                    Socket dataSocket;

                    if (dataSocket.accept()) {

                        DataOutoutStream outData = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));

                        File file = new file(fileName);
                        BufferedReader reader = null;
                        //open file reader
                        try {
                            reader = new BufferedReader(new FileReader(file));
                            String text = null;

                            while ((text = reader.readLine()) != null) {
                                outData.writeBytes(line);
                            }
                        }
                    }
                }
            }
        }
    }
}


