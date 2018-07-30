package field.entity.Message;


import java.util.ArrayList;
import java.util.List;

/**
 * Query的缓存（暂时无上限）
 * Created by Hong on 2017/7/11.
 */
public class QueryCache {

    /** Query的缓存 */
    public List<Query> queries;

    public QueryCache(){
        queries = new ArrayList<>();
    }

    public int size(){
        return queries.size();
    }

    /**
     * 向cache中添加新的query
     * @param query
     */
    public void add(Query query){
        queries.add(query);
    }

    public void remove(Query query){
        queries.remove(query);
    }

    /**
     * 查看当前query缓存中是否已有该query
     * @param query query
     * @return 若已存在，则返回该query，不存在则返回null
     */
    public Query containsQuery(Query query){
        for (Query cacheQuery : queries) {
            if (cacheQuery.getQueryID() == query.getQueryID())
                return cacheQuery;
        }
        return null;
    }
}
