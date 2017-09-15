package xx.database.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import xx.database.db.sqlite.CursorUtils;
import xx.database.db.sqlite.DbModel;
import xx.database.db.sqlite.ManyToOneLazyLoader;
import xx.database.db.sqlite.OneToManyLazyLoader;
import xx.database.db.sqlite.SqlBuilder;
import xx.database.db.sqlite.SqlInfo;
import xx.database.db.table.KeyValue;
import xx.database.db.table.ManyToOne;
import xx.database.db.table.OneToMany;
import xx.database.db.table.TableInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class DragonsDb {

    private static final String TAG = "DragonsDb";

    private static HashMap<String, DragonsDb> daoMap = new HashMap<String, DragonsDb>();

    private SQLiteDatabase db;
    private final DaoConfig config;

    private DragonsDb(DaoConfig config) {
        if (config == null) {
            // throw new DbException("daoConfig is null");
            Log.d(TAG, "daoConfig is null:");
        }
        if (config.getContext() == null) {
            // throw new DbException("android context is null");
            Log.d(TAG, "android context is null:");
        }
        if (config.getTargetDirectory() != null
                && config.getTargetDirectory().trim().length() > 0) {
            this.db = this.createDbFileOnSDCard(config.getTargetDirectory(),
                    config.getDbName());
        } else {
            this.db = new SqliteDbHelper(config.getContext()
                    .getApplicationContext(), config.getDbName(),
                    config.getDbVersion(), config.getDbUpdateListener())
                    .getWritableDatabase();
        }
        this.config = config;
    }

    private synchronized static DragonsDb getInstance(DaoConfig daoConfig) {
        DragonsDb dao = daoMap.get(daoConfig.getDbName());
        if (dao == null) {
            dao = new DragonsDb(daoConfig);
            daoMap.put(daoConfig.getDbName(), dao);
        }
        return dao;
    }

    /**
     * 创建DragonsDb
     *
     * @param context
     */
    public static DragonsDb create(Context context) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        return create(config);
    }

    /**
     * 创建DragonsDb
     *
     * @param context
     * @param isDebug 是否是debug模式（debug模式进行数据库操作的时候将会打印sql语句）
     */
    public static DragonsDb create(Context context, boolean isDebug) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDebug(isDebug);
        return create(config);

    }

    /**
     * 创建DragonsDb
     *
     * @param context
     * @param dbName  数据库名称
     */
    public static DragonsDb create(Context context, String dbName) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        return create(config);
    }

    /**
     * 创建 DragonsDb
     *
     * @param context
     * @param dbName  数据库名称
     * @param isDebug 是否为debug模式（debug模式进行数据库操作的时候将会打印sql语句）
     */
    public static DragonsDb create(Context context, String dbName,
                                   boolean isDebug) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        return create(config);
    }

    /**
     * 创建DragonsDb
     *
     * @param context
     * @param dbName  数据库名称
     */
    public static DragonsDb create(Context context, String targetDirectory,
                                   String dbName) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        config.setTargetDirectory(targetDirectory);
        return create(config);
    }

    /**
     * 创建 DragonsDb
     *
     * @param context
     * @param dbName  数据库名称
     * @param isDebug 是否为debug模式（debug模式进行数据库操作的时候将会打印sql语句）
     */
    public static DragonsDb create(Context context, String targetDirectory,
                                   String dbName, boolean isDebug) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setTargetDirectory(targetDirectory);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        return create(config);
    }

    /**
     * 创建 DragonsDb
     *
     * @param context          上下文
     * @param dbName           数据库名字
     * @param isDebug          是否是调试模式：调试模式会log出sql信息
     * @param dbVersion        数据库版本信息
     * @param dbUpdateListener 数据库升级监听器：如果监听器为null，升级的时候将会清空所所有的数据
     * @return
     */
    public static DragonsDb create(Context context, String dbName,
                                   boolean isDebug, int dbVersion, DbUpdateListener dbUpdateListener) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        config.setDbVersion(dbVersion);
        config.setDbUpdateListener(dbUpdateListener);
        return create(config);
    }

    /**
     * @param context          上下文
     * @param targetDirectory  db文件路径，可以配置为sdcard的路径
     * @param dbName           数据库名字
     * @param isDebug          是否是调试模式：调试模式会log出sql信息
     * @param dbVersion        数据库版本信息
     * @param dbUpdateListener 数据库升级监听器
     *                         ：如果监听器为null，升级的时候将会清空所所有的数据
     * @return
     */
    public static DragonsDb create(Context context, String targetDirectory,
                                   String dbName, boolean isDebug, int dbVersion,
                                   DbUpdateListener dbUpdateListener) {
        DaoConfig config = new DaoConfig();
        config.setContext(context);
        config.setTargetDirectory(targetDirectory);
        config.setDbName(dbName);
        config.setDebug(isDebug);
        config.setDbVersion(dbVersion);
        config.setDbUpdateListener(dbUpdateListener);
        return create(config);
    }

    /**
     * 创建DragonsDb
     *
     * @param daoConfig
     * @return
     */
    public static DragonsDb create(DaoConfig daoConfig) {
        return getInstance(daoConfig);
    }

    /**
     * 保存数据库，速度要比save快
     *
     * @param entity
     */
    public void save(Object entity) {
        this.checkDbFile();
        this.checkTableExist(entity.getClass());
        this.exeSqlInfo(SqlBuilder.buildInsertSql(entity));
    }

    private void checkDbFile() {
        Log.d(TAG, "checkDbFile-->config-->" + this.config);
        if (!new File(this.config.getTargetDirectory(), this.config.getDbName())
                .exists()) {
            Log.d(TAG, "checkDbFile-->dbFile does not exist. create a new file now!!!!!!!!!!!!!");
            //[+LEUI][RUBY-19370] lifei3 当SD卡中到数据库文件被删除掉后，重新创建数据库文件时，需要清理掉表记录到缓存
            TableInfo.clearAll();
            //[-LEUI][RUBY-19370]
            this.db = this.createDbFileOnSDCard(
                    this.config.getTargetDirectory(), this.config.getDbName());
        }
    }

    /**
     * 保存数据到数据库<br />
     * <b>注意：</b><br />
     * 保存成功后，entity的主键将被赋值（或更新）为数据库的主键， 只针对自增长的id有效
     *
     * @param entity 要保存的数据
     * @return ture： 保存成功 false:保存失败
     */
    public boolean saveBindId(Object entity) {
        if (this.db != null) {
            this.checkTableExist(entity.getClass());
            List<KeyValue> entityKvList = SqlBuilder
                    .getSaveKeyValueListByEntity(entity);
            if (entityKvList != null && entityKvList.size() > 0) {
                TableInfo tf = TableInfo.get(entity.getClass());
                ContentValues cv = new ContentValues();
                this.insertContentValues(entityKvList, cv);
                Long id = -1L;
                try {
                    id = this.db.insert(tf.getTableName(), null, cv);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(TAG, "saveBindId-->Exception:" + e.toString());
                }
                if (id == -1) {
                    return false;
                }
                tf.getId().setValue(entity, id);
                return true;
            }
        }
        return false;
    }

    /**
     * 把List<KeyValue>数据存储到ContentValues
     *
     * @param list
     * @param cv
     */
    private void insertContentValues(List<KeyValue> list, ContentValues cv) {
        if (list != null && cv != null) {
            for (KeyValue kv : list) {
                cv.put(kv.getKey(), kv.getValue().toString());
            }
        } else {
            Log.w(TAG,
                    "insertContentValues: List<KeyValue> is empty or ContentValues is empty!");
        }

    }

    /**
     * 更新数据 （主键ID必须不能为空）
     *
     * @param entity
     */
    public void update(Object entity) {
        this.checkTableExist(entity.getClass());
        this.exeSqlInfo(SqlBuilder.getUpdateSqlAsSqlInfo(entity));
    }

    /**
     * 根据条件更新数据
     *
     * @param entity
     * @param strWhere 条件为空的时候，将会更新所有的数据
     */
    public void update(Object entity, String strWhere) {
        this.checkTableExist(entity.getClass());
        this.exeSqlInfo(SqlBuilder.getUpdateSqlAsSqlInfo(entity, strWhere));
    }

    /**
     * 删除数据
     *
     * @param entity entity的主键不能为空
     */
    public void delete(Object entity) {
        this.checkTableExist(entity.getClass());
        this.exeSqlInfo(SqlBuilder.buildDeleteSql(entity));
    }

    /**
     * 根据主键删除数据
     *
     * @param clazz 要删除的实体类
     * @param id    主键值
     */
    public void deleteById(Class<?> clazz, Object id) {
        this.checkTableExist(clazz);
        this.exeSqlInfo(SqlBuilder.buildDeleteSql(clazz, id));
    }

    /**
     * 根据条件删除数据
     *
     * @param clazz
     * @param strWhere 条件为空的时候 将会删除所有的数据
     */
    public void deleteByWhere(Class<?> clazz, String strWhere) {
        if (this.db != null) {
            this.checkTableExist(clazz);
            String sql = SqlBuilder.buildDeleteSql(clazz, strWhere);
            this.debugSql(sql);
            try {
                this.db.execSQL(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                Log.w(TAG, "deleteByWhere-->SQLException:" + e.toString());
            }
        }
    }

    /**
     * 删除表的所有数据
     *
     * @param clazz
     */
    public void deleteAll(Class<?> clazz) {
        if (this.db != null) {
            this.checkTableExist(clazz);
            String sql = SqlBuilder.buildDeleteSql(clazz, null);
            this.debugSql(sql);
            try {
                this.db.execSQL(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                Log.w(TAG, "deleteAll-->SQLException:" + e.toString());
            }
        }
    }

    /**
     * 删除指定的表
     *
     * @param clazz
     */
    public void dropTable(Class<?> clazz) {
        if (this.db != null) {
            this.checkTableExist(clazz);
            TableInfo table = TableInfo.get(clazz);
            String sql = "DROP TABLE " + table.getTableName();
            this.debugSql(sql);
            try {
                this.db.execSQL(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                Log.w(TAG, "dropTable-->SQLException:" + e.toString());
            }
        }
    }

    /**
     * 删除所有数据表
     */
    public void dropDb() {
        if (this.db != null) {
            Cursor cursor = this.db
                    .rawQuery(
                            "SELECT name FROM sqlite_master WHERE type ='table' AND name != 'sqlite_sequence'",
                            null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String sql = "DROP TABLE " + cursor.getString(0);
                    try {
                        this.db.execSQL(sql);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Log.w(TAG, "dropDb-->SQLException:" + e.toString());
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    private void exeSqlInfo(SqlInfo sqlInfo) {
        if (this.db != null) {
            //[+LEUI][XSIX-16108]
            synchronized(db){
                if (sqlInfo != null) {
                    this.debugSql(sqlInfo.getSql());
                    try {
                        this.db.execSQL(sqlInfo.getSql(),
                                sqlInfo.getBindArgsAsArray());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        Log.e(TAG,
                                "exeSqlInfo-->SQLException-->msg:" + e.toString());
                    }
                } else {
                    Log.e(TAG, "sava error:sqlInfo is null");
                }
            }//[-LEUI][XSIX-16108]
        }
    }

    /**
     * 根据主键查找数据（默认不查询多对一或者一对多的关联数据）
     *
     * @param id
     * @param clazz
     */
    public <T> T findById(Object id, Class<T> clazz) {
        if (this.db != null) {
            this.checkTableExist(clazz);
            SqlInfo sqlInfo = SqlBuilder.getSelectSqlAsSqlInfo(clazz, id);
            if (sqlInfo != null) {
                this.debugSql(sqlInfo.getSql());
                Cursor cursor = null;
                try {
                    cursor = this.db.rawQuery(sqlInfo.getSql(),
                            sqlInfo.getBindArgsAsStringArray());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(TAG, "findById-->Exception:" + e.toString());
                }
                try {
                    if (cursor != null && cursor.moveToNext()) {
                        return CursorUtils.getEntity(cursor, clazz, this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 根据主键查找，同时查找“多对一”的数据（如果有多个“多对一”属性，则查找所有的“多对一”属性）
     *
     * @param id
     * @param clazz
     */
    public <T> T findWithManyToOneById(Object id, Class<T> clazz) {
        this.checkTableExist(clazz);
        String sql = SqlBuilder.getSelectSQL(clazz, id);
        this.debugSql(sql);
        DbModel dbModel = this.findDbModelBySQL(sql);
        if (dbModel != null) {
            T entity = CursorUtils.dbModel2Entity(dbModel, clazz);
            return this.loadManyToOne(dbModel, entity, clazz);
        }

        return null;
    }

    /**
     * 根据条件查找，同时查找“多对一”的数据（只查找findClass中的类的数据）
     *
     * @param id
     * @param clazz
     * @param findClass 要查找的类
     */
    public <T> T findWithManyToOneById(Object id, Class<T> clazz,
                                       Class<?>... findClass) {
        this.checkTableExist(clazz);
        String sql = SqlBuilder.getSelectSQL(clazz, id);
        this.debugSql(sql);
        DbModel dbModel = this.findDbModelBySQL(sql);
        if (dbModel != null) {
            T entity = CursorUtils.dbModel2Entity(dbModel, clazz);
            return this.loadManyToOne(dbModel, entity, clazz, findClass);
        }
        return null;
    }

    /**
     * 将entity中的“多对一”的数据填充满 如果是懒加载填充，则dbModel参数可为null
     *
     * @param clazz
     * @param entity
     * @param <T>
     * @return
     */
    public <T> T loadManyToOne(DbModel dbModel, T entity, Class<T> clazz,
                               Class<?>... findClass) {
        if (entity != null) {
            try {
                Collection<ManyToOne> manys = TableInfo.get(clazz).manyToOneMap
                        .values();
                for (ManyToOne many : manys) {

                    Object id = null;
                    if (dbModel != null) {
                        id = dbModel.get(many.getColumn());
                    } else if (many.getValue(entity).getClass() == ManyToOneLazyLoader.class
                            && many.getValue(entity) != null) {
                        id = ((ManyToOneLazyLoader) many.getValue(entity))
                                .getFieldValue();
                    }

                    if (id != null) {
                        boolean isFind = false;
                        if (findClass == null || findClass.length == 0) {
                            isFind = true;
                        }
                        for (Class<?> mClass : findClass) {
                            if (many.getManyClass() == mClass) {
                                isFind = true;
                                break;
                            }
                        }
                        if (isFind) {

                            @SuppressWarnings("unchecked")
                            T manyEntity = (T) this.findById(
                                    Integer.valueOf(id.toString()),
                                    many.getManyClass());
                            if (manyEntity != null) {
                                if (many.getValue(entity).getClass() == ManyToOneLazyLoader.class) {
                                    if (many.getValue(entity) == null) {
                                        many.setValue(
                                                entity,
                                                new ManyToOneLazyLoader(entity,
                                                        clazz,
                                                        many.getManyClass(),
                                                        this));
                                    }
                                    ((ManyToOneLazyLoader) many
                                            .getValue(entity)).set(manyEntity);
                                } else {
                                    many.setValue(entity, manyEntity);
                                }

                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return entity;
    }

    /**
     * 根据主键查找，同时查找“一对多”的数据（如果有多个“一对多”属性，则查找所有的一对多”属性）
     *
     * @param id
     * @param clazz
     */
    public <T> T findWithOneToManyById(Object id, Class<T> clazz) {
        this.checkTableExist(clazz);
        String sql = SqlBuilder.getSelectSQL(clazz, id);
        this.debugSql(sql);
        DbModel dbModel = this.findDbModelBySQL(sql);
        if (dbModel != null) {
            T entity = CursorUtils.dbModel2Entity(dbModel, clazz);
            return this.loadOneToMany(entity, clazz);
        }

        return null;
    }

    /**
     * 根据主键查找，同时查找“一对多”的数据（只查找findClass中的“一对多”）
     *
     * @param id
     * @param clazz
     * @param findClass
     */
    public <T> T findWithOneToManyById(Object id, Class<T> clazz,
                                       Class<?>... findClass) {
        this.checkTableExist(clazz);
        String sql = SqlBuilder.getSelectSQL(clazz, id);
        this.debugSql(sql);
        DbModel dbModel = this.findDbModelBySQL(sql);
        if (dbModel != null) {
            T entity = CursorUtils.dbModel2Entity(dbModel, clazz);
            return this.loadOneToMany(entity, clazz, findClass);
        }

        return null;
    }

    /**
     * 将entity中的“一对多”的数据填充满
     *
     * @param entity
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T loadOneToMany(T entity, Class<T> clazz, Class<?>... findClass) {
        if (entity != null) {
            try {
                Collection<OneToMany> ones = TableInfo.get(clazz).oneToManyMap
                        .values();
                Object id = TableInfo.get(clazz).getId().getValue(entity);
                for (OneToMany one : ones) {
                    boolean isFind = false;
                    if (findClass == null || findClass.length == 0) {
                        isFind = true;
                    }
                    for (Class<?> mClass : findClass) {
                        if (one.getOneClass() == mClass) {
                            isFind = true;
                            break;
                        }
                    }

                    if (isFind) {
                        List<?> list = this.findAllByWhere(one.getOneClass(),
                                one.getColumn() + "=" + id);
                        if (list != null) {
                            /* 如果是OneToManyLazyLoader泛型，则执行灌入懒加载数据 */
                            if (one.getDataType() == OneToManyLazyLoader.class) {
                                OneToManyLazyLoader oneToManyLazyLoader = one
                                        .getValue(entity);
                                oneToManyLazyLoader.setList(list);
                            } else {
                                one.setValue(entity, list);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return entity;
    }

    /**
     * 查找所有的数据
     *
     * @param clazz
     */
    public <T> List<T> findAll(Class<T> clazz) {
        this.checkTableExist(clazz);
        return this.findAllBySql(clazz, SqlBuilder.getSelectSQL(clazz));
    }

    /**
     * 查找所有数据
     *
     * @param clazz
     * @param orderBy 排序的字段
     */
    public <T> List<T> findAll(Class<T> clazz, String orderBy) {
        this.checkTableExist(clazz);
        return this.findAllBySql(clazz, SqlBuilder.getSelectSQL(clazz)
                + " ORDER BY " + orderBy);
    }

    /**
     * 根据条件查找所有数据
     *
     * @param clazz
     * @param strWhere 条件为空的时候查找所有数据
     */
    public <T> List<T> findAllByWhere(Class<T> clazz, String strWhere) {
        this.checkTableExist(clazz);
        return this.findAllBySql(clazz,
                SqlBuilder.getSelectSQLByWhere(clazz, strWhere));
    }

    /**
     * 根据条件查找所有数据
     *
     * @param clazz
     * @param strWhere 条件为空的时候查找所有数据
     * @param orderBy  排序字段
     */
    public <T> List<T> findAllByWhere(Class<T> clazz, String strWhere,
                                      String orderBy) {
        this.checkTableExist(clazz);
        return this.findAllBySql(clazz,
                SqlBuilder.getSelectSQLByWhere(clazz, strWhere) + " ORDER BY "
                        + orderBy);
    }

    /**
     * 根据条件查找所有数据
     *
     * @param clazz
     * @param strSQL
     */
    private <T> List<T> findAllBySql(Class<T> clazz, String strSQL) {
        if (this.db != null) {
            this.checkTableExist(clazz);
            this.debugSql(strSQL);
            Cursor cursor = null;
            try {
                cursor = this.db.rawQuery(strSQL, null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            try {
                List<T> list = new ArrayList<T>();
                while (cursor != null && cursor.moveToNext()) {
                    T t = CursorUtils.getEntity(cursor, clazz, this);
                    list.add(t);
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                cursor = null;
            }
        }
        return null;
    }

    /**
     * 根据sql语句查找数据，这个一般用于数据统计
     *
     * @param strSQL
     */
    public DbModel findDbModelBySQL(String strSQL) {
        if (this.db != null) {
            this.debugSql(strSQL);
            Cursor cursor = null;
            try {
                cursor = this.db.rawQuery(strSQL, null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            try {
                if (cursor != null && cursor.moveToNext()) {
                    return CursorUtils.getDbModel(cursor);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public List<DbModel> findDbModelListBySQL(String strSQL) {
        if (this.db != null) {
            this.debugSql(strSQL);
            Cursor cursor = null;
            try {
                cursor = this.db.rawQuery(strSQL, null);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            List<DbModel> dbModelList = new ArrayList<DbModel>();
            try {
                while (cursor != null && cursor.moveToNext()) {
                    dbModelList.add(CursorUtils.getDbModel(cursor));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
            return dbModelList;
        }
        return null;
    }

    private void checkTableExist(Class<?> clazz) {
        if (this.db != null) {
            TableInfo info = TableInfo.get(clazz);
            if(null==info){
                Log.d(TAG, "checkTableExist-->TableInfo is null:");
                return ;
            }
            boolean exist = this.tableIsExist(info);
            Log.d(TAG, "checkTableExist-->exist:" + exist);
            if (!exist) {
                String sql = SqlBuilder.getCreatTableSQL(clazz);
                this.debugSql(sql);
                try {
                    this.db.execSQL(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.d(TAG,
                            "checkTableExist-->SQLException e:" + e.toString());
                }
            }
        } else {
            Log.d(TAG, "checkTableExist-->this.db is null...");
        }
    }

    private boolean tableIsExist(TableInfo table) {
        if (this.db != null) {
            if (table.isCheckDatabese()) {
                return true;
            }

            Cursor cursor = null;
            try {
                String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                        + table.getTableName() + "' ";
                this.debugSql(sql);
                cursor = this.db.rawQuery(sql, null);
                if (cursor != null && cursor.moveToNext()) {
                    int count = cursor.getInt(0);
                    if (count > 0) {
                        table.setCheckDatabese(true);
                        return true;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                cursor = null;
            }
        }

        return false;
    }

    private void debugSql(String sql) {
        if (this.config != null && this.config.isDebug()) {
            Log.d("Debug SQL", ">>>>>>  " + sql);
        }
    }

    public static class DaoConfig {
        private Context mContext = null; // android上下文
        private String mDbName = "afinal.db"; // 数据库名字
        private int dbVersion = 1; // 数据库版本
        private boolean debug = true; // 是否是调试模式（调试模式 增删改查的时候显示SQL语句）
        private DbUpdateListener dbUpdateListener;
        // private boolean saveOnSDCard = false;//是否保存到SD卡
        private String targetDirectory;// 数据库文件在sd卡中的目录

        public Context getContext() {
            return this.mContext;
        }

        public void setContext(Context context) {
            this.mContext = context;
        }

        public String getDbName() {
            return this.mDbName;
        }

        public void setDbName(String dbName) {
            this.mDbName = dbName;
        }

        public int getDbVersion() {
            return this.dbVersion;
        }

        public void setDbVersion(int dbVersion) {
            this.dbVersion = dbVersion;
        }

        public boolean isDebug() {
            return this.debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public DbUpdateListener getDbUpdateListener() {
            return this.dbUpdateListener;
        }

        public void setDbUpdateListener(DbUpdateListener dbUpdateListener) {
            this.dbUpdateListener = dbUpdateListener;
        }

        // public boolean isSaveOnSDCard() {
        // return saveOnSDCard;
        // }
        //
        // public void setSaveOnSDCard(boolean saveOnSDCard) {
        // this.saveOnSDCard = saveOnSDCard;
        // }

        public String getTargetDirectory() {
            return this.targetDirectory;
        }

        public void setTargetDirectory(String targetDirectory) {
            this.targetDirectory = targetDirectory;
        }

        @Override
        public String toString() {
            return "DaoConfig [mDbName=" + mDbName + ", dbVersion=" + dbVersion
                    + ", debug=" + debug + ", dbUpdateListener="
                    + dbUpdateListener + ", targetDirectory=" + targetDirectory
                    + "]";
        }


    }

    /**
     * 在SD卡的指定目录上创建文件
     *
     * @param sdcardPath
     * @param dbfilename
     * @return
     */
    private SQLiteDatabase createDbFileOnSDCard(String sdcardPath,
                                                String dbfilename) {
        Log.d(TAG, "sdcardPath:" + sdcardPath);
        Log.d(TAG, "dbfilename:" + dbfilename);
        File file = new File(sdcardPath);
        boolean mkdirs = true;
        if (!file.exists()) {
            mkdirs = file.mkdirs();
        }
        Log.d(TAG, "mkdirs:" + mkdirs);
        if (mkdirs) {
            File dbf = new File(sdcardPath, dbfilename);
            if (!dbf.exists()) {
                try {
                    if (dbf.createNewFile()) {
                        return SQLiteDatabase.openDatabase(dbf.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.CREATE_IF_NECESSARY);
                    } else {
                        Log.d(TAG, "createNewFile fail...");
                    }
                } catch (IOException ioex) {
                    // throw new DbException("数据库文件创建失败", ioex);
                    Log.d(TAG, "IOException:" + ioex.toString());
                }
            } else {
                return SQLiteDatabase.openDatabase(dbf.getPath(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.CREATE_IF_NECESSARY);
            }
        } else {
            Log.d(TAG, "mkdirs fail..." + sdcardPath);
        }
        return null;
    }

    class SqliteDbHelper extends SQLiteOpenHelper {

        private final DbUpdateListener mDbUpdateListener;

        public SqliteDbHelper(Context context, String name, int version,
                              DbUpdateListener dbUpdateListener) {
            super(context, name, null, version);
            this.mDbUpdateListener = dbUpdateListener;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (this.mDbUpdateListener != null) {
                this.mDbUpdateListener.onUpgrade(db, oldVersion, newVersion);
            } else { // 清空所有的数据信息
                DragonsDb.this.dropDb();
            }
        }

    }

    public interface DbUpdateListener {
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }

}
