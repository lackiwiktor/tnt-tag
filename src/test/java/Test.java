import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Test {


    private final List<Integer> list = new ArrayList<>(List.of(1, 3, 2, 4, 7, 0));

    @org.junit.Test
    public void test() {
        printList(list);
        final var moreThan1 = list.stream().filter(it -> it > 1).collect(Collectors.toList());
        printList(moreThan1);
        Collections.sort(moreThan1);
        printList(moreThan1);
        printList(list);
    }

    void printList(List list) {
        list.forEach(System.out::print);
        System.out.println(" ");
    }
}
