package com.iogarage.ke.pennywise;

import javax.inject.Scope;

/**
 * Created by choxxy on 07/04/2017.
 */

@Scope
public @interface DaggerScope {
    Class<?> value();
}