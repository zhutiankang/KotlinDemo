package base.http.annotations

/**
 * Field
 *
 * @author tiankang
 * @description:
 * @date :2022/12/28 17:16
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Field(val value: String)
