package com.kodeholic.itbook.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kodeholic.itbook.lib.util.Log;

import java.util.Arrays;
import java.util.List;

public abstract class TABLE<T> {
    protected SQLiteDatabase db = null;
    protected String         tableName = null;
    public TABLE(SQLiteDatabase db, String tableName) {
        this.db = db;
        this.tableName = tableName;
    }

    /*** abstract ******************************/
    public abstract ContentValues fetchObject2Values(T o);
    public abstract List<T>       fetchCursor2List  (Cursor c) throws Exception;

    /**
     * DB객체를 반환한다.
     * @return
     */
    public SQLiteDatabase getSQLiteDatabase() {
        return db;
    }

    /**
     * 데이타를 삽입한다.
     * @param o
     * @return
     */
    public long insert(T o) {
        try {
            ContentValues values = fetchObject2Values(o);
            //
            long ret = db.insert(tableName, null, values);
            if (ret != 0) {
                Log.d(tableName, "insert() - ret: " + ret);
                return ret;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Log.w(tableName, "insert failed!");

        return -1;
    }

    public void fastInsert(List<ContentValues> dataList) {
        Log.d(tableName, "fastInsert()");
        try {
            db.beginTransaction();
            for (int i = 0; i < dataList.size(); i++){
                db.insert(tableName, null, dataList.get(i));
            }
            db.setTransactionSuccessful();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
    }

    /**
     * 데이타를 갱신한다.
     * @param o
     * @return
     */
    public boolean update(T o, String whereClause, String[] whereArgs) {
        try {
            ContentValues values = fetchObject2Values(o);
            //
            //String   whereClause = MESSAGE_ID + "=?";
            //String[] whereArgs   = new String[] { messageId };
            if (db.update(tableName, values, whereClause, whereArgs) != 0) {
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Log.w(tableName, "update failed!");

        return false;
    }

    public int update(ContentValues values, String whereClause, String[] whereArgs) {
        try {
            return db.update(tableName, values, whereClause, whereArgs);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Log.w(tableName, "update failed!");

        return -1;
    }

    public void execSQL(String sql) {
        Log.d(tableName, "sql: " + sql);
        db.execSQL(sql);
    }

    /**
     * 데이타를 삭제한다.
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int delete(String whereClause, String[] whereArgs) {
        try {
            Log.d(tableName,"delete key value : " + whereClause);
            return db.delete(tableName, whereClause, whereArgs);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Log.w(tableName, "delete failed!");

        return -1;
    }

    /**
     * row의 개수를 반환한다.
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int getCount(String whereClause, String[] whereArgs) {
        Cursor c = null;
        try {
            String sqlStr = "SELECT COUNT(*) FROM " + tableName ;
            if (whereClause != null && whereClause.length() != 0) {
                sqlStr += " WHERE " + whereClause;
            }
            if ((c = db.rawQuery(sqlStr, whereArgs)) != null) {
                if (c.getCount() != 0 && c.moveToFirst()) {
                    int count = c.getInt(0);

                    Log.d(tableName, "getCount() - count: " + count);
                    return count;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (c != null) {
                try {
                    c.close();
                }
                catch (Exception ignore) { }
            }
        }
        Log.w(tableName, "getCount() failed!");

        return 0;
    }

    /**
     * 오브젝트 목록을 조회한다.
     * @param columns
     * @param whereClause
     * @param whereArgs
     * @param orderBy
     * @param limit
     * @return
     */
    public List<T> select(String[] columns, String whereClause, String[] whereArgs, String orderBy, String limit) {
        Log.d(tableName, "select() - columns: " + Arrays.toString(columns)
                + ", whereClause: " + whereClause
                + ", whereArgs: " + Arrays.toString(whereArgs)
                + ", orderBy: " + orderBy
                + ", limit: " + limit
        );
        Cursor c = null;
        try {
            c = db.query(tableName, columns, whereClause, whereArgs, null /* groupBy */, null /* having */, orderBy, limit);
            if (c == null) {
                Log.e(tableName, "select() - cursor is null.");
                return null;
            }
            List<T> list = fetchCursor2List(c);

            Log.d(tableName, "select() - read count(" + list.size() + ")");

            return list;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (c != null) {
                try {
                    c.close();
                }
                catch (Exception ignore) { }
            }
        }

        return null;
    }

    public T select1(String[] columns, String whereClause, String[] whereArgs, String orderBy) {
        Log.d(tableName, "select1() - columns: " + Arrays.toString(columns)
                + ", whereClause: " + whereClause
                + ", whereArgs: " + Arrays.toString(whereArgs)
                + ", orderBy: " + orderBy
        );

        String limit = "1";
        List<T> list = select(columns, whereClause, whereArgs, orderBy, limit);
        if (list != null && list.size() >= 1) {
            return list.get(0);
        }

        return null;
    }

    public T select1(String[] columns, String whereClause, String[] whereArgs) {
        return select1(columns, whereClause, whereArgs, null);
    }

    public List<T> select_raw(String sql) {
        Log.d(tableName, "select_raw() - sql: " + sql);

        Cursor c = null;
        try {
            c = db.rawQuery(sql, null);
//            c = db.query(tableName, columns, whereClause, whereArgs, null /* groupBy */, null /* having */, orderBy, limit);
            if (c == null) {
                Log.e(tableName, "getList() - cursor is null.");
                return null;
            }
            List<T> list = fetchCursor2List(c);

            Log.d(tableName, "getList() - read count(" + list.size() + ")");

            return list;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (c != null) {
                try {
                    c.close();
                }
                catch (Exception ignore) { }
            }
        }

        return null;
    }

    public boolean update_raw(String sql) {
        Log.d(tableName, "update_raw() - sql: " + sql);
        try {
            db.execSQL(sql);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Truncate
     * @return
     */
    public int truncate() {
        Log.d(tableName, "truncate()");
        try {
            return db.delete(tableName, null, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Log.w(tableName, "truncate failed!");

        return -1;
    }

    public static String array2In(int[] n) {
        try {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < n.length; i++) {
                if (i != 0) { sb.append(","); }
                sb.append(n[i]);
            }

            return sb.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String array2In(List<Long> list) {
        try {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < list.size(); i++) {
                if (i != 0) { sb.append(","); }
                sb.append(list.get(i));
            }

            return sb.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String strArray2In(List<String> n) {
        try {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < n.size(); i++) {
                if (i != 0) { sb.append(","); }
                sb.append("'" + n.get(i) + "'");
            }

            return sb.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Cursor cursor(String sql) {
        Log.d(tableName, "cursor() - sql: " + sql);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return cursor;
    }

    public Cursor cursor(String[] columns, String whereClause, String[] whereArgs, String orderBy, String limit) {
        Log.d(tableName, "cursor() - columns: " + Arrays.toString(columns)
                + ", whereClause: " + whereClause
                + ", whereArgs: " + Arrays.toString(whereArgs)
                + ", orderBy: " + orderBy
                + ", limit: " + limit
        );
        try {
            Cursor c = db.query(tableName, columns, whereClause, whereArgs, null /* groupBy */, null /* having */, orderBy, limit);
            if (c == null) {
                Log.e(tableName, "select() - cursor is null.");
                return null;
            }

            return c;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Cursor cursor(String[] columns, String whereClause, String[] whereArgs, String groupBy, String having, String orderBy, String limit) {
        Log.d(tableName, "cursor() - columns: " + Arrays.toString(columns)
                + ", whereClause: " + whereClause
                + ", whereArgs: " + Arrays.toString(whereArgs)
                + ", groupBy: " + groupBy
                + ", having: " + having
                + ", orderBy: " + orderBy
                + ", limit: " + limit
        );

        try {
            Cursor c = db.query(tableName, columns, whereClause, whereArgs, groupBy, having, orderBy, limit);
            if (c == null) {
                Log.e(tableName, "select() - cursor is null.");
                return null;
            }

            return c;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void insertOrUpdate(T o, String whereClause, String[] whereArgs) {
        try {
            ContentValues values = fetchObject2Values(o);
            //
            long lret = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            Log.d(tableName, "insertOrUpdate() -> insertWithOnConflict() - lret: " + lret);
            if (lret == -1) {
                int iret = db.update(tableName, values, whereClause, whereArgs);
                Log.d(tableName, "insertOrUpdate() -> update() - iret: " + iret);
            }

            return;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Log.w(tableName, "insertOrUpdate failed!");
    }
}
