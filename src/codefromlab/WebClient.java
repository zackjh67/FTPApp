package codefromlab;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WebClient {

    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 8000;
        Socket socket1;
        //scanner to take user input
        Scanner scnr = new Scanner(System.in);


        // Step 1: Create a socket that connects to the above host and port number
        try {
            //user inputs port to use
            System.out.print("Type the Port Number to use (ex. www.cis.gvsu.edu uses port 80): ");
            portNumber = scnr.nextInt();
            //user inputs Host Name
            System.out.print("Type the Host Name (ex. www.cis.gvsu.edu): ");
            hostName = scnr.next();
            socket1 = new Socket(hostName, portNumber);

            // Step 2: Create a PrintWriter from the socket's output stream
            //         Use the autoFlush option
            PrintWriter printWriter = new PrintWriter(socket1.getOutputStream(), true);

            // Step 3: Create a BufferedReader from the socket's input stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket1.getInputStream()));

            // Step 4: Send an HTTP GET request via the PrintWriter.
            //         Remember to print the necessary blank line

            //user inputs GET request
            System.out.print("Type what to GET (ex. ~dulimarh/CS163/Examples/ArithmeticGUI.java): ");
            //string to hold user input for GET request
            String get = "";
            get = scnr.next();
            scnr.close();

            printWriter.println("GET /" + get + " HTTP/1.0");
            printWriter.println("Host: " + hostName +"\n");

            // Step 5a: Read the status line of the response
            String string = bufferedReader.readLine();
            printWriter.println(string);
            System.out.println("Status Line: " + string);

            // Step 5b: Read the HTTP response via the BufferedReader until
            //         you get a blank line
            System.out.println("HTTP Response:");
            string = bufferedReader.readLine();

            // String array to hold split line of content-type
            String[] typeGetter = {};
            while (!string.isEmpty()) {
                printWriter.println(string);
                System.out.println("echo: " + string);
                string = bufferedReader.readLine();
                //not really part of the assignment, but wanted to try this
                if(string.contains("Content-Type")) {
                    typeGetter = string.split("/");
                }
            }

            //string to hold type for file extension
            String type = typeGetter[1];

            // Step 6a: Create a FileOutputStream for storing the payload
            // Step 6b: Wrap the FileOutputStream in another PrintWriter
            FileOutputStream out = new FileOutputStream("Output." + type);
            PrintWriter printWriter2 = new PrintWriter(out);

            // Step 7: Read the rest of the input from BufferedReader and write
            //         it to the second PrintWriter.
            //         Hint: readLine() returns null when there is no more data
            //         to read
            while ((string = bufferedReader.readLine()) != null) {
                printWriter2.println(string);
                System.out.println("echo: " + bufferedReader.readLine());
            }
            // Step 8: Remember to close the writer
            printWriter.close();
            printWriter2.close();
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}