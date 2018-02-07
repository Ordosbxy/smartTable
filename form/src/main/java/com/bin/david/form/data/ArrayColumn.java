package com.bin.david.form.data;


import android.util.Log;

import com.bin.david.form.data.format.IFormat;
import com.bin.david.form.data.format.draw.IDrawFormat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huang on 2018/2/1.
 * 用于解析数组Column
 */

public class ArrayColumn<T> extends Column<T> {

    public static final int ARRAY = 1;
    public static final int LIST = 2;
    private int[] lastPositionArray;
    private ColumnNode node;
    private ArrayStructure structure;
    /**
     * 数组类型
     */
    private int arrayType;
    /**
     * 是否查询到Array object 内部
     */
    private boolean isThoroughArray = false;

    public ArrayColumn(String columnName, String fieldName) {
        this(columnName, fieldName,true,null,null);
    }
    public ArrayColumn(String columnName, String fieldName,boolean isThoroughArray) {
        this(columnName, fieldName,isThoroughArray,null,null);
    }

    public ArrayColumn(String columnName, String fieldName,boolean isThoroughArray, IFormat<T> format) {
        this(columnName, fieldName, isThoroughArray,format,null);
    }

    public ArrayColumn(String columnName, String fieldName,boolean isThoroughArray, IDrawFormat<T> drawFormat) {
        this(columnName, fieldName,isThoroughArray, null,drawFormat);
    }

    public ArrayColumn(String columnName, String fieldName,boolean isThoroughArray, IFormat<T> format, IDrawFormat<T> drawFormat) {
        super(columnName, fieldName, format, drawFormat);
        structure = new ArrayStructure();
        this.isThoroughArray = isThoroughArray;
    }



    @Override
    public void fillData(List<Object> objects) throws NoSuchFieldException, IllegalAccessException {
        int level = ColumnNode.getLevel(node,0)-1;
        structure.clear();
        structure.setMaxLevel(level);
        if(getCountFormat() != null){
            getCountFormat().clearCount();
        }
        if (objects.size() > 0) {
            lastPositionArray = new int[objects.size()];
            String[] fieldNames = getFieldName().split("\\.");
            if (fieldNames.length > 0) {
                int size = objects.size();
                for (int k = 0; k < size; k++) {
                    Object child= objects.get(k);
                    getFieldData(fieldNames,0,child,0,true);
                    lastPositionArray[k] = getDatas().size()-1;
                }
            }

        }
    }

    private void getFieldData(String[] fieldNames,int start,Object child,int level,boolean isFoot) throws NoSuchFieldException, IllegalAccessException {

        for (int i = start; i < fieldNames.length; i++) {
            if (child == null) {
                addData(null,isFoot);
                countColumnValue(null);
                structure.putNull(level);
                break;
            }
            Class childClazz = child.getClass();
            Field childField = childClazz.getDeclaredField(fieldNames[i]);
            childField.setAccessible(true);
            child = childField.get(child);
            if(!isList(child)) {
                if (i == fieldNames.length - 1) {
                    if(child == null){
                        structure.putNull(level);
                    }
                    T t = (T) child;
                    addData(t, true);
                    countColumnValue(t);
                }
            }else{
               level++;
              if(child.getClass().isArray()){
                  T[] data = (T[]) child;
                  arrayType = ARRAY;
                  for (Object d : data) {
                      if (i == fieldNames.length - 1) {
                          addData((T)d, true);
                      } else {
                          getFieldData(fieldNames, i + 1, d,level,true);
                      }
                  }
                  structure.put(level-1,data.length);
              }else {
                  List data = (List) child;
                  arrayType = LIST;
                  for (Object d : data) {
                      if (i == fieldNames.length - 1) {
                          T t = (T) d;
                          addData(t, true);
                      } else {
                          getFieldData(fieldNames, i + 1, d,level,true);
                      }

                  }
                  structure.put(level-1,data.size());
              }
              break;
            }
        }
    }


    private boolean isList(Object o){
        return o !=null && (o instanceof List  || o.getClass().isArray());
    }



    public int getLineCount(int position){
        if(lastPositionArray == null){
            return 1;
        }
        int size;
        if(position == 0){
            size = lastPositionArray[position]+1;
        }else{
            size = lastPositionArray[position] - lastPositionArray[position-1];
        }
        return size;
    }

    public ColumnNode getNode() {
        return node;
    }

    public void setNode(ColumnNode node) {
        this.node = node;
    }

    public int[] getLastPositionArray() {
        return lastPositionArray;
    }

    public void setLastPositionArray(int[] lastPositionArray) {
        this.lastPositionArray = lastPositionArray;
    }

    public int getArrayType() {
        return arrayType;
    }

    public void setArrayType(int arrayType) {
        this.arrayType = arrayType;
    }

    public ArrayStructure getStructure() {
        return structure;
    }

    public void setStructure(ArrayStructure structure) {
        this.structure = structure;
    }

    public boolean isThoroughArray() {
        return isThoroughArray;
    }

    public void setThoroughArray(boolean thoroughArray) {
        isThoroughArray = thoroughArray;
    }


}
