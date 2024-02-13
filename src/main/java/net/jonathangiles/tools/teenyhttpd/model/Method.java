package net.jonathangiles.tools.teenyhttpd.model;

/**
 * An enumeration listing all of the available request methods that are possible. Note that note all of these request
 * methods are supported in TeenyHttpd today.
 */
public enum Method {
    OPTIONS,
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    TRACE,
    PATCH,
    CONNECT;
}
