package net.jonathangiles.tools.teenyhttpd;

import java.util.List;

public interface TodosService {

    List<TodoDto> list();

    List<TodoDto> add(TodoDto todo);

    boolean remove(String uuid);
}
