package base.http.annotations

/**
 * Get
 *
 * @author tiankang
 * @description:
 * @date :2022/12/28 17:16
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GET(val value: String)
