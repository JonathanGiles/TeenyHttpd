package net.jonathangiles.tools.teenyhttpd.pack1;

import net.jonathangiles.tools.teenyhttpd.TodoDto;
import net.jonathangiles.tools.teenyhttpd.TodosService;
import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.util.List;

@Path("/todos")
public class ExampleApplication1 {

    @Inject
    private TodosService service;
    @Value("server.port")
    private int serverPort;
    @Value("server.host:localhost")
    private String serverHost;

    @Get("/todos")
    public TypedResponse<List<TodoDto>> getTodos() {

        List<TodoDto> list = service.list();

        if (list.isEmpty()) return TypedResponse.noContent();

        return TypedResponse.ok(list);
    }

    @Delete("/todos/:id")
    public TypedResponse<List<TodoDto>> remove(@PathParam("id") String uuid) {
        if (!service.remove(uuid)) {
            return TypedResponse.notFound();
        }

        return getTodos();
    }

    @Put("/todos")
    public TypedResponse<List<TodoDto>> add(TodoDto todo) {
        return TypedResponse.ok(service.add(todo));
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerHost() {
        return serverHost;
    }
}
