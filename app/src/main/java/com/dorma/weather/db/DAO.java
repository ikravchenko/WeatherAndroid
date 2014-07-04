package com.dorma.weather.db;

import android.content.ContentValues;

public interface DAO<T> {

    T getById(long id);
    long insert(T newItem);
    void update(T newItem);
    void deleteById(long id);
    ContentValues toContentValues(T item);
    void deleteAll();
}
