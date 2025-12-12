package se.yverling.wearto.convention

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project

internal val Project.libs get() = extensions.getByType(LibrariesForLibs::class.java)
