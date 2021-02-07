package actualDemo;

import utils.CollectionUtils;
import utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 多线程分片查询
 */
public class ShardingQueryUtils {
    //private static final  Logger logger = LoggerFactory.getLogger(ShardingQueryUtils.class);

    private static volatile Executor executor;

    private static final Object LOCK=new Object();
    private static final Integer MODEL=10;
    private static final Integer SHARDING_MIN=50;
    private static final Integer HUNDRED=100;
    /**默认线程池*/
    public static final int fixedThreadPoolSize=16;

    /**
     * 根据分片建，分组model 并发查询数据
     * @param shardingKeyList  分片健值list
     * @param callProcess 查询函数
     * @param <T>
     * @return
     */
    public static <T> List<T> queryShardingKeys(Function<List<Long>, List<T>> callProcess, List<Long> shardingKeyList){
        if(CollectionUtils.isEmpty(shardingKeyList)){
            return ListUtils.EMPTY_LIST;
        }
        if(shardingKeyList.size()<=SHARDING_MIN){
            return callProcess.apply(shardingKeyList);
        }
        List<List<Long>> divideShardingKeyList=divideShardingKeyList(shardingKeyList);
        List<T> resultList=syncQueryByShardingKeys(callProcess,divideShardingKeyList);
        return resultList;
    }

    /**
     *
     * @param divideShardingKeyList  分片健值分组list
     * @param callProcess 查询函数
     * @param <T>
     * @return
     */
    private static <T>  List<T> syncQueryByShardingKeys(Function<List<Long>, List<T>> callProcess, List<List<Long>> divideShardingKeyList){
        if(CollectionUtils.isEmpty(divideShardingKeyList)){
            return ListUtils.EMPTY_LIST;
        }
        if(divideShardingKeyList.size()==1){
            return callProcess.apply(divideShardingKeyList.get(0));
        }
        //Context context=ContextUtils.get();
        List<T> resultList=new ArrayList<>();
        CompletableFuture.allOf(divideShardingKeyList.stream().
                map(keyList-> CompletableFuture.supplyAsync(()->{
                    //ContextUtils.set(context);
                    return callProcess.apply(keyList);
                },getExecutor()).exceptionally(e->{
                    //log.error("queryShardingKeys-exception", e);
                    return resultList;
                }).whenComplete((v,e)->{
                    if (Objects.nonNull(v)) {
                        resultList.addAll(v);
                    }
                })).toArray(CompletableFuture[]::new)).join();
        return resultList;
    }

    /**
     * 对分片健进行分组
     * @param shardingKeyList 分片健值list
     * @return
     */
    private static List<List<Long>> divideShardingKeyList(List<Long>  shardingKeyList){
        if(CollectionUtils.isEmpty(shardingKeyList)){
            return ListUtils.EMPTY_LIST;
        }
        List<List<Long>> result = new ArrayList<>(MODEL);
        for(int i=0;i<MODEL;i++){
            List<Long> list = new ArrayList<>();
            result.add(list);
        }
        //归类
        for(Long shardingKey : shardingKeyList){
            int index = getIndex(shardingKey);
            result.get(index).add(shardingKey);
        }
        result= result.stream().filter(l->!CollectionUtils.isEmpty(l)).collect(Collectors.toList());
        return result;
    }

    /**
     * 根据model 获取shardingKey对应分组下标
     * @param shardingKey
     * @return
     */
    private static int getIndex(Long shardingKey){
        Long l=(shardingKey / HUNDRED) % MODEL;
        return l.intValue();
    }

    /**
     * 通过双重校验锁的方式获取连接池
     * 如无法获取连接池Bean(tmsExecutorService),则用固定大小16的线程池
     * @return
     */
    private static Executor getExecutor() {
        if (executor == null) {
            synchronized (LOCK) {
                if (executor == null) {
                    //spring配置注释
//                    executor = (Executor)  SpringUtils.getBean("asyncThreadExecutor");
//                    if (null == executor) {
                        executor = Executors.newFixedThreadPool(fixedThreadPoolSize);
//                    }
                }
            }
        }
        return executor;
    }

}
