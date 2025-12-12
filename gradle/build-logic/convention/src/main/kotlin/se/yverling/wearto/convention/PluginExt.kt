package se.yverling.wearto.convention

import org.gradle.api.plugins.PluginContainer
import org.gradle.api.provider.Provider
import org.gradle.plugin.use.PluginDependency

internal fun PluginContainer.alias(provider: Provider<PluginDependency>) {
    apply(provider.get().pluginId)
}
