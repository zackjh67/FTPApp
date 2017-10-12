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
                        if(modifiedSentence.equals(" "))
                            notEnd = false;
                        //todo remove this print statement i think
                        System.out.println("modified sentence: " + modifiedSentence);
                    }

                    welcomeData.close();
                    dataSocket.close();
                    //make notEnd true again
                    notEnd = true;
                    System.out.println("\nWhat would you like to do next: \n retr: file.txt ||  				stor: file.txt  || close");

                } else if (sentence.startsWith("retr: ")) {
                    //todo....................................................
                }
            }
        }
    }
}