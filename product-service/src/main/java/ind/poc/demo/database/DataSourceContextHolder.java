package ind.poc.demo.database;

public class DataSourceContextHolder {

    private static final ThreadLocal<DataSourceOperation> CONTEXT = new ThreadLocal<>();

    public static void set(DataSourceOperation type) {
        CONTEXT.set(type);
    }

    public static DataSourceOperation get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}





