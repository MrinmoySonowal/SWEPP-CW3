package shield;

public class BoxItem {
    int id;
    String name;
    int quantity;

    @Override
    public String toString() {
        return "BoxItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
