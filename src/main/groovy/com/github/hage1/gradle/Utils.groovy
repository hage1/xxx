package com.github.hage1.gradle

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
/**
 * Created by wataru on 13/12/05.
 */

@Retention(RetentionPolicy.RUNTIME)
@interface GradlePlugin {
    String alias()
}
