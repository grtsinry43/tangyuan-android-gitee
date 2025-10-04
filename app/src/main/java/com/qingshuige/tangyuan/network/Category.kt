package com.qingshuige.tangyuan.network;

import androidx.annotation.NonNull;

public class Category {
    public int categoryId;
    public String baseName;
    public String baseDescription;

    @NonNull
    @Override
    public String toString() {
        return baseName;
    }
}
