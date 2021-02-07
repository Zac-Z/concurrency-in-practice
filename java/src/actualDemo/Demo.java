package actualDemo;

import utils.CollectionUtils;
import utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Demo {

    public static void main(String[] args) {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i <100 ; i++) {
            ids.add(Long.valueOf(i));
        }
        List<Persion> persions = ShardingQueryUtils.queryShardingKeys(Demo::queryList, ids);

        Long count = persions.stream().map(Persion::getId).distinct().count();
        System.out.println(count);
    }

    /**
     * 查询
     * @param ids
     * @return
     */
    public static List<Persion> queryList(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            return ids.stream().map(v -> new Persion(v, "C-" + v, v.intValue())).collect(Collectors.toList());
        }
        return ListUtils.EMPTY_LIST;
    }
}


class Persion {
    private Long id;
    private String name;
    private Integer age;

    public Persion(Long id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Persion{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}