package net.jonathangiles.tools.teenyhttpd.pack2;

import net.jonathangiles.tools.teenyhttpd.TodoDto;
import net.jonathangiles.tools.teenyhttpd.TodosService;
import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.util.List;

@Path("/todos2")
public class ExampleApplication2 {

    private final TodosService service;
    public static String postConstructHeader = "";

    public ExampleApplication2(TodosService service) {
        this.service = service;
    }

    @PostConstruct
    void testPostConstruct() {
        postConstructHeader = "testPostConstruct";
    }

    @Get("/todos")
    public TypedResponse<List<TodoDto>> getTodos() {

        List<TodoDto> list = service.list();

        if (list.isEmpty()) return TypedResponse.noContent();

        return TypedResponse.ok(list)
                .setHeader("X-Test", postConstructHeader);
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

}
