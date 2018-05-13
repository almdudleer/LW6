package Communication;

import CollectionCLI.CollectionHandler;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.util.InputMismatchException;
import java.util.Scanner;

import static CollectionCLI.Instruments.*;
import static Communication.Protocol.time;

public class TrueServer {
    public static void main(String[] args) throws IOException {


        int port = 0;
        //load collection
        CollectionHandler ch = new CollectionHandler();
        ShutdownHook shutdownHook = new ShutdownHook(ch);
        Runtime.getRuntime().addShutdownHook(shutdownHook);



        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName timeName = new ObjectName("lab4:type=Time");
            StandardMBean mbean = new StandardMBean(time, TimeMBean.class);
            server.registerMBean(mbean, timeName);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            ch.file = new File(extractFilePath(args[0]), extractFileName(args[0]));
        } catch (NullPointerException e) {
            System.out.println("You should enter the path");
            System.exit(1);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Not enough arguments");
        }
        try {
            ch.load();
        } catch (SecurityException e) {
            System.out.println("Permission to the file Denied");
        } catch (IOException | NullPointerException e) {
            System.out.println("Wrong input file. Enter the command again.");
            System.exit(1);
        }
        //server
        boolean listening = true;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println("Enter port:");
            try {
                port = scan.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Should be integer");
                scan.next();
            }
        } while (port == 0);
        try (
                ServerSocket serverSocket = new ServerSocket(port)
        ) {
            //kek
            Command commandsBean = new Command();
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            try {
                ObjectName commands = new ObjectName("lab4:type=Commands");
                //StandardMBean mbean = new StandardMBean(commandsBean, CommandMBean.class);
                server.registerMBean(commandsBean, commands);
                Protocol.commands = commandsBean;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //kek
            while (listening) {
                new MultiClientThread(serverSocket.accept(), ch).start();
            }
            System.out.println("Disconnected");
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
        }
    }
}

class ShutdownHook extends Thread {
    CollectionHandler ch;

    ShutdownHook(CollectionHandler inch) {
        this.ch = inch;
    }

    public void run() {
        try {
            ch.save();
        } catch (NullPointerException e) {
            System.out.println("No file");
        }
    }
}



