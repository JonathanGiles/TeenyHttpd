package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.implementation.ResponseBase;
import net.jonathangiles.tools.teenyhttpd.model.Header;
import net.jonathangiles.tools.teenyhttpd.model.StatusCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class ResponseBaseTest {

    static class ResponseHelper extends ResponseBase {

        public ResponseHelper() {
            super(StatusCode.OK);
        }

        @Override
        public void writeBody(BufferedOutputStream dataOut) throws IOException {
            // do nothing
        }
    }

    @Test
    void testAddHeader() {

        ResponseBase response = new ResponseHelper();

        response.setHeader("key", "value");
        response.addHeader("key", "value2");

        Assertions.assertEquals(2, response.getHeaders()
                .get(0)
                .getValues().size());

        response.setHeader(new Header("key", "value"));

        Assertions.assertEquals(1, response.getHeaders()
                .get(0)
                .getValues().size());

        response.removeHeader("key");

        Assertions.assertEquals(0, response.getHeaders().size());

        response.addHeader("key", "value");
        response.addHeader("key", "value2", "value3", "value4");
        response.addHeader(new Header("key", "value5"));

        Assertions.assertEquals(5, response.getHeaders()
                .get(0)
                .getValues().size());

        response.addHeader("key2", "value");

        Assertions.assertEquals(2, response.getHeaders().size());

        response.removeHeader("key2");

        Assertions.assertEquals(1, response.getHeaders().size());

        response.setHeader("key", "value");

        Assertions.assertEquals(1, response.getHeaders()
                .get(0)
                .getValues().size());

    }
}
