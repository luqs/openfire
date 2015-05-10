package com.skyseas.openfireplugins.group.iq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhangzhi on 2014/9/11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryHandler {
    /**
     * query节点的命名空间
     * @return
     */
    String namespace();

    /**
     * query节点的node属性
     * @return
     */
    String node() default "";
}
