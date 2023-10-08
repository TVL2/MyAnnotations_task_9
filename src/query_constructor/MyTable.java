package query_constructor;

@Table(title = "my_table")
public class MyTable {
    @Column
    String name;

    @Column
    Integer age = 6;

    public MyTable(String name, Integer age) {
        this.name = name;
        this.age = age;
    }
}
