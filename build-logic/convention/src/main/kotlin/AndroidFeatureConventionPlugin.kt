import com.example.nexusbank.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for feature modules.
 * Applies: android library + compose + hilt, and wires in the shared core deps,
 * navigation, lifecycle, and standard test deps every feature needs.
 *
 * After applying this, a feature module only needs to declare its own extra
 * dependencies (e.g. another :core:* module specific to that feature).
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("nexusbank.android.library")
                apply("nexusbank.android.library.compose")
                apply("nexusbank.android.hilt")
            }

            dependencies {
                add("implementation", project(":core:core-domain"))
                add("implementation", project(":core:core-ui"))
                add("implementation", project(":core:core-common"))

                add("implementation", libs().findLibrary("androidx-core-ktx").get())
                add("implementation", libs().findLibrary("androidx-lifecycle-runtime-ktx").get())
                add("implementation", libs().findLibrary("hilt-navigation-compose").get())
                add("implementation", libs().findLibrary("navigation-compose").get())

                add("testImplementation", libs().findLibrary("junit").get())
            }
        }
    }
}
