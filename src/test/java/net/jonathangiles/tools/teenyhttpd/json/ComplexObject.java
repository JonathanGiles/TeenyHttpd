package net.jonathangiles.tools.teenyhttpd.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class ComplexObject {

    private String string;
    private boolean aBoolean;
    private Boolean aBoolean2;
    private int anInt;
    private Integer anInteger;
    private long aLong;
    private Long aLong2;
    private float aFloat;
    private Float aFloat2;
    private double aDouble;
    private Double aDouble2;
    private BigDecimal bigDecimal;
    private BigInteger bigInteger;
    private LocalDate localDate;
    private LocalDateTime localDateTime;
    private LocalTime localTime;
    private Automakers automaker;
    private List<String> list;
    private Set<String> set;
    private Map<String, ObjectC> map;
    private ArrayList<Integer> arrayList;
    private Date date;
    private String[] array;
    private ObjectC[] arrayOfObjectC;
    private LinkedList<String> linkedList;
    private LinkedHashSet<String> linkedHashSet;

    public ComplexObject() {
    }

    public ComplexObject setLinkedList(LinkedList<String> linkedList) {
        this.linkedList = linkedList;
        return this;
    }

    public ComplexObject setLinkedHashSet(LinkedHashSet<String> linkedHashSet) {
        this.linkedHashSet = linkedHashSet;
        return this;
    }

    public LinkedList<String> getLinkedList() {
        return linkedList;
    }

    public LinkedHashSet<String> getLinkedHashSet() {
        return linkedHashSet;
    }

    public String getString() {
        return string;
    }

    public ComplexObject setString(String string) {
        this.string = string;
        return this;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public ComplexObject setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
        return this;
    }

    public Boolean getaBoolean2() {
        return aBoolean2;
    }

    public ComplexObject setaBoolean2(Boolean aBoolean2) {
        this.aBoolean2 = aBoolean2;
        return this;
    }

    public int getAnInt() {
        return anInt;
    }

    public ComplexObject setAnInt(int anInt) {
        this.anInt = anInt;
        return this;
    }

    public Integer getAnInteger() {
        return anInteger;
    }

    public ComplexObject setAnInteger(Integer anInteger) {
        this.anInteger = anInteger;
        return this;
    }

    public long getaLong() {
        return aLong;
    }

    public ComplexObject setaLong(long aLong) {
        this.aLong = aLong;
        return this;
    }

    public Long getaLong2() {
        return aLong2;
    }

    public ComplexObject setaLong2(Long aLong2) {
        this.aLong2 = aLong2;
        return this;
    }

    public float getaFloat() {
        return aFloat;
    }

    public ComplexObject setaFloat(float aFloat) {
        this.aFloat = aFloat;
        return this;
    }

    public Float getaFloat2() {
        return aFloat2;
    }

    public ComplexObject setaFloat2(Float aFloat2) {
        this.aFloat2 = aFloat2;
        return this;
    }

    public double getaDouble() {
        return aDouble;
    }

    public ComplexObject setaDouble(double aDouble) {
        this.aDouble = aDouble;
        return this;
    }

    public Double getaDouble2() {
        return aDouble2;
    }

    public ComplexObject setaDouble2(Double aDouble2) {
        this.aDouble2 = aDouble2;
        return this;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public ComplexObject setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
        return this;
    }

    public BigInteger getBigInteger() {
        return bigInteger;
    }

    public ComplexObject setBigInteger(BigInteger bigInteger) {
        this.bigInteger = bigInteger;
        return this;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public ComplexObject setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
        return this;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public ComplexObject setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
        return this;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public ComplexObject setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
        return this;
    }

    public Automakers getAutomaker() {
        return automaker;
    }

    public ComplexObject setAutomaker(Automakers automaker) {
        this.automaker = automaker;
        return this;
    }

    public List<String> getList() {
        return list;
    }

    public ComplexObject setList(List<String> list) {
        this.list = list;
        return this;
    }

    public Set<String> getSet() {
        return set;
    }

    public ComplexObject setSet(Set<String> set) {
        this.set = set;
        return this;
    }

    public Map<String, ObjectC> getMap() {
        return map;
    }

    public ComplexObject setMap(Map<String, ObjectC> map) {
        this.map = map;
        return this;
    }

    public ArrayList<Integer> getArrayList() {
        return arrayList;
    }

    public ComplexObject setArrayList(ArrayList<Integer> arrayList) {
        this.arrayList = arrayList;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public ComplexObject setDate(Date date) {
        this.date = date;
        return this;
    }

    public String[] getArray() {
        return array;
    }

    public ComplexObject setArray(String[] array) {
        this.array = array;
        return this;
    }

    public ObjectC[] getArrayOfObjectC() {
        return arrayOfObjectC;
    }

    public ComplexObject setArrayOfObjectC(ObjectC[] arrayOfObjectC) {
        this.arrayOfObjectC = arrayOfObjectC;
        return this;
    }



    @Override
    public String toString() {
        return "ComplexObject{" +
                "string='" + string + '\'' +
                ", aBoolean=" + aBoolean +
                ", aBoolean2=" + aBoolean2 +
                ", anInt=" + anInt +
                ", anInteger=" + anInteger +
                ", aLong=" + aLong +
                ", aLong2=" + aLong2 +
                ", aFloat=" + aFloat +
                ", aFloat2=" + aFloat2 +
                ", aDouble=" + aDouble +
                ", aDouble2=" + aDouble2 +
                ", bigDecimal=" + bigDecimal +
                ", bigInteger=" + bigInteger +
                ", localDate=" + localDate +
                ", localDateTime=" + localDateTime +
                ", localTime=" + localTime +
                ", automaker=" + automaker +
                ", list=" + list +
                ", set=" + set +
                ", map=" + map +
                ", arrayList=" + arrayList +
                ", date=" + date +
                ", array=" + Arrays.toString(array) +
                ", arrayOfObjectC=" + Arrays.toString(arrayOfObjectC) +
                ", linkedList=" + linkedList +
                ", linkedHashSet=" + linkedHashSet +
                '}';
    }
}
