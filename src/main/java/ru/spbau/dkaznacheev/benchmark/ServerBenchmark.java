package ru.spbau.dkaznacheev.benchmark;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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
             OutputStream os = socket.getOutputStream()) {
            CommandLine cmd = new DefaultParser().parse(options, args);
            ServerParametersM params = new ServerParametersM(
                    cmd.getOptionValue("a"),
                    cmd.getOptionValue("x"),
                    cmd.getOptionValue("p"),
                    cmd.getOptionValue("n"),
                    cmd.getOptionValue("m"),
                    cmd.getOptionValue("d"),
                    cmd.getOptionValue("pl"),
                    cmd.getOptionValue("ph"),
                    cmd.getOptionValue("ps")
            );


        } catch (ParseException e) {
            System.err.println("Invalid options.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
