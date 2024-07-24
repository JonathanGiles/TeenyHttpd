package net.jonathangiles.tools.teenyhttpd;

import net.jonathangiles.tools.teenyhttpd.annotations.Resource;

import java.util.ArrayList;
import java.util.List;

@Resource
public class TodosServiceImpl implements TodosService {

    private final List<TodoDto> todos = new ArrayList<>();

    public TodosServiceImpl() {
        todos.add(new TodoDto("1", "First", "This is the first todo", System.currentTimeMillis()));
        todos.add(new TodoDto("2", "Second", "This is the second todo", System.currentTimeMillis()));
        todos.add(new TodoDto("3", "Third", "This is the third todo", System.currentTimeMillis()));
    }

    @Override
    public List<TodoDto> list() {
        return todos;
    }

    @Override
    public boolean remove(String uuid) {
        return todos.removeIf(todo -> todo.getUuid().equals(uuid));
    }

    @Override
    public List<TodoDto> add(TodoDto todo) {
        todos.removeIf(t -> t.getUuid().equals(todo.getUuid()));
        todos.add(todo);

        return todos;
    }
}
