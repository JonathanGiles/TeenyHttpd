package net.jonathangiles.tools.teenyhttpd.json;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class JsonBenchmarks {

    static final ComplexObject complexObject = new ComplexObject()
            .setString("string :{}")
            .setAnInt(1)
            .setAnInteger(2)
            .setaLong(2)
            .setaLong2(2L)
            .setaFloat(1.0f)
            .setaFloat2(1.0f)
            .setaDouble(1.0)
            .setaDouble2(1.0)
            .setaBoolean(true)
            .setaBoolean2(true)
            .setBigDecimal(BigDecimal.TEN)
            .setBigInteger(BigDecimal.TEN.toBigInteger())
            .setLocalDate(LocalDate.now())
            .setLocalDateTime(LocalDate.now().atStartOfDay())
            .setLocalTime(LocalTime.now())
            .setArray(new String[]{"A", "B", "C"})
            .setLinkedList(new LinkedList<>(List.of("A", "B", "C")))
            .setLinkedHashSet(new LinkedHashSet<>(List.of("A", "B", "C")))
            .setArrayOfObjectC(new ObjectC[]{new ObjectC("John Doe", 23),
                    new ObjectC("Jane Doe", 25),
                    new ObjectC("John Smith", 30)})
            .setArrayList(new ArrayList<>(List.of(1, 2, 3, 4, 5)))
            .setDate(new java.util.Date())
            .setAutomaker(Automakers.TOYOTA)
            .setList(List.of("A", "B", "C"))
            .setSet(Set.of("A", "B", "C"))
            .setMap(Map.of("A", new ObjectC("John Doe", 23),
                    "B", new ObjectC("Jane Doe", 25),
                    "C", new ObjectC("John Smith", 30)));

    static final TeenyJson teenyJson = new TeenyJson();

    static final String json = "{\"localDateTime\": \"2024-03-10T00:00\", \"linkedHashSet\": [\"A\", \"B\", \"C\"], \"date\": \"10-03-2024 02:19:07\", \"aFloat2\": 1.0, \"string\": \"string :{}\", \"aLong\": 2, \"anInt\": 1, \"arrayOfObjectC\": [{\"name\": \"John Doe\", \"age\": 23}, {\"name\": \"Jane Doe\", \"age\": 25}, {\"name\": \"John Smith\", \"age\": 30}], \"aDouble\": 1.0, \"anInteger\": 2, \"aBoolean2\": true, \"array\": [\"A\", \"B\", \"C\"], \"arrayList\": [1, 2, 3, 4, 5], \"localDate\": \"2024-03-10\", \"bigDecimal\": 10, \"map\": {\"A\": {\"name\": \"John Doe\", \"age\": 23}, \"B\": {\"name\": \"Jane Doe\", \"age\": 25}, \"C\": {\"name\": \"John Smith\", \"age\": 30}}, \"linkedList\": [\"A\", \"B\", \"C\"], \"aBoolean\": true, \"aDouble2\": 1.0, \"set\": [\"A\", \"B\", \"C\"], \"aFloat\": 1.0, \"bigInteger\": 10, \"list\": [\"A\", \"B\", \"C\"], \"localTime\": \"02:19:07.036590\", \"automaker\": \"TOYOTA\", \"aLong2\": 2}";

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 1, warmups = 2)
    public void encodingBenchmark() {
        teenyJson.writeValueAsString(complexObject);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 1, warmups = 2)
    public void decodingBenchmark() {
        teenyJson.readValue(json, ComplexObject.class);
    }

}
