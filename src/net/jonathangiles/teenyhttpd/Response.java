package net.jonathangiles.teenyhttpd;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class Response {
    protected abstract void send(PrintWriter out, BufferedOutputStream dataOut) throws IOException;
}
