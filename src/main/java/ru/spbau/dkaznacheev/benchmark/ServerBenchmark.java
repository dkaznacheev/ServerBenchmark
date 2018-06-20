package ru.spbau.dkaznacheev.benchmark;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class ServerBenchmark {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("a", true, "architecture");
        options.addOption("x", true, "X");
        options.addOption("p", true, "parameter to change");
        options.addOption("n", true, "N");
        options.addOption("m", true, "M");
        options.addOption("d", true, "Delta");
        options.addOption("pl", true, "min parameter");
        options.addOption("ph", true, "max parameter");
        options.addOption("ps", true, "parameter step");


        try (Socket socket = new Socket("127.0.0.1", 8080);
             InputStream is = socket.getInputStream();
             OutputStream os = socket.getOutputStream()) {
            CommandLine cmd = new DefaultParser().parse(options, args);

            ServerProtocols.ServerParametersMsg msg = buildMsg(cmd);

            msg.writeDelimitedTo(os);

            List<TestResult> serverResults = startTest(msg);
            List<Integer> calcTimeResults = serverResults.stream().map(TestResult::getCalcTime).collect(Collectors.toList());
            List<Integer> handleTimeResults = serverResults.stream().map(TestResult::getHandleTime).collect(Collectors.toList());
            List<Integer> clientTimeResults = ServerProtocols.ArrayMsg.parseDelimitedFrom(is).getNumberList();

            debugOut("calc:", calcTimeResults);
            debugOut("handle:", handleTimeResults);
            debugOut("client:", clientTimeResults);


            Charter.makeChart(msg, calcTimeResults, "sortingTime");
            Charter.makeChart(msg, handleTimeResults, "serverHandleTime");
            Charter.makeChart(msg, clientTimeResults, "clientHandleTime");


        } catch (ParseException e) {
            System.err.println("Invalid options.");
        } catch (IOException e) {
            e.printStackTrace();
        }



        /*startTest(
                ServerProtocols.ServerParametersMsg.newBuilder()
                        .setArchitecture("simple")
                        .setParameter("n")
                        .setM(1)
                        .setN(1)
                        .setD(1)
                        .setX(1)
                        .setPMin(1)
                        .setPMax(2)
                        .setPStep(1)
                        .build());
                        */
    }

    private static void debugOut(String title, List<Integer> list) {
        System.out.println(title);
        StringJoiner joiner = new StringJoiner(", ");
        list.stream().forEach((a)->joiner.add(Integer.toString(a)));
        System.out.println(joiner.toString());
        System.out.println();

    }

    private static List<TestResult> startTest(ServerProtocols.ServerParametersMsg msg) {
        List <TestResult> results = new LinkedList<>();
        for (int i = msg.getPMin(); i < msg.getPMax(); i += msg.getPStep()) {
            System.out.println("testing " + msg.getParameter() + " = " + Integer.toString(i));
            try (ServerSocket serverSocket = new ServerSocket(8000)) {

                Server server;

                if (msg.getArchitecture().equals("simple")) {
                    server = new SimpleServer(serverSocket, msg.getM(), msg.getX());
                } else if (msg.getArchitecture().equals("blocking")) {
                    server = new BlockingServer(serverSocket, msg.getM(), msg.getX());
                } else {
                    server = new SimpleServer(serverSocket, msg.getM(), msg.getX());
                }

                TestResult result = server.start();
                results.add(result);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    private static ServerProtocols.ServerParametersMsg buildMsg(CommandLine cmd) {
        int mVal = -1, nVal = -1, dVal = -1;
        String m = cmd.getOptionValue("m");
        if (m != null)
            mVal = Integer.parseInt(m);
        String n = cmd.getOptionValue("n");
        if (n != null)
            nVal = Integer.parseInt(n);
        String d = cmd.getOptionValue("d");
        if (d != null)
            dVal = Integer.parseInt(d);

        int xVal = Integer.parseInt(cmd.getOptionValue("x"));
        int pMinVal = Integer.parseInt(cmd.getOptionValue("pl"));
        int pMaxVal = Integer.parseInt(cmd.getOptionValue("ph"));
        int pStepVal = Integer.parseInt(cmd.getOptionValue("ps"));

        ServerProtocols.ServerParametersMsg msg =
                ServerProtocols.ServerParametersMsg.newBuilder()
                .setArchitecture(cmd.getOptionValue("a"))
                .setParameter(cmd.getOptionValue("p"))
                .setM(mVal)
                .setN(nVal)
                .setD(dVal)
                .setX(xVal)
                .setPMin(pMinVal)
                .setPMax(pMaxVal)
                .setPStep(pStepVal)
                .build();
        return msg;
    }
}
