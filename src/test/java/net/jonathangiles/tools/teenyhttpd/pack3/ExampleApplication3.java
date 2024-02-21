package net.jonathangiles.tools.teenyhttpd.pack3;

import net.jonathangiles.tools.teenyhttpd.GsonMessageConverter;
import net.jonathangiles.tools.teenyhttpd.TodoDto;
import net.jonathangiles.tools.teenyhttpd.annotations.*;
import net.jonathangiles.tools.teenyhttpd.model.MessageConverter;
import net.jonathangiles.tools.teenyhttpd.model.TypedResponse;

import java.util.ArrayList;
import java.util.List;

@Path("/todos3")
public class ExampleApplication3 {

    List<TodoDto> todos = new ArrayList<>();


    @Get("/todos")
    public TypedResponse<List<TodoDto>> getTodos() {

        if (todos.isEmpty()) return TypedResponse.noContent();

        return TypedResponse.ok(todos);
    }

    @Delete("/todos/:id")
    public TypedResponse<List<TodoDto>> remove(@PathParam("id") String id) {
        if (todos.removeIf(todo -> todo.getUuid().equals(id))) {
            return getTodos();
        } else {
            return TypedResponse.notFound();
        }
    }

    @Put("/todos")
    public TypedResponse<List<TodoDto>> add(TodoDto todo) {

        boolean updated = todos.removeIf(t -> t.getUuid().equals(todo.getUuid()));
        todos.add(todo);

        return getTodos();
    }

}
