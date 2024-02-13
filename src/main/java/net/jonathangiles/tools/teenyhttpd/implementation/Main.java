package net.jonathangiles.tools.teenyhttpd.implementation;

import net.jonathangiles.tools.teenyhttpd.TeenyHttpd;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class Main {
    private static final List<Command> commands = List.of(
            new Command("--port", "The port to run the server on", "80",
                    (main, value) -> main.port = Integer.parseInt(value)),
            new Command("--dir", "The root directory to serve files from", ".",
                    (main, value) -> main.rootDirectory = value),
            new Command("--path", "The path to serve files from (e.g. '/blah' for http://localhost/blah", "/",
                    (main, value) -> main.path = value)
    );

    int port;
    String rootDirectory;
    String path;

    /**
     * Starts a new server instance on port 80, and serves requests from the current working directory.
     */
    public static void main(String... args) {
        new Main().start(args);
    }

    private void start(String... args) {
        processArgs(args);

        TeenyHttpd server = new TeenyHttpd(port);
        server.addFileRoute(path, new File(rootDirectory));
        server.start();

        System.out.println("Server started on port " + port + " serving files from " + rootDirectory + " at path " + path);
        System.out.println("Press enter to stop the server");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        server.stop();
    }

    private void processArgs(String... args) {
        // set all properties to their default values initially, and then overwrite if appropriate
        for (Command command : commands) {
            command.execute(this, command.defaultValue);
        }

        if (args != null && args.length > 0) {
            // look for the standard help flags, and if found, print help and exit
            for (String arg : args) {
                if (arg.equals("-h") || arg.equals("--help")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Usage: java -jar teenyhttpd.jar [options]\n\n");
                    sb.append("Options:\n");
                    for (Command command : commands) {
                        sb.append("  ").append(command.name).append("=").append(command.defaultValue)
                          .append("\n      ").append(command.description).append("\n");
                    }
                    sb.append("  -h, --help\n      Print this help message and exit\n");
                    System.out.println(sb);

                    System.exit(0);
                }
            }

            // parse the args array
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                for (Command command : commands) {
                    if (arg.startsWith(command.name + "=")) {
                        String value = arg.substring((command.name + "=").length());
                        command.execute(this, value);
                    }
                }
            }
        }
    }

    private static class Command {
        private final String name;
        private final String description;
        private final String defaultValue;
        private final BiConsumer<Main, String> action;

        public Command(String name, String description, String defaultValue, BiConsumer<Main, String> action) {
            this.name = name;
            this.description = description;
            this.defaultValue = defaultValue;
            this.action = action;
        }

        public void execute(Main main, String value) {
            action.accept(main, value);
        }
    }
}
