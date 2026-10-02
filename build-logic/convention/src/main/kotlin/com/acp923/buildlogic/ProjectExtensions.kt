package com.acp923.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * 约定插件里访问版本目录的入口。
 *
 * 版本目录在 `build-logic/settings.gradle.kts` 里通过
 * `versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }`
 * 显式指向根项目的 toml —— included build 默认不继承根项目的版本目录，
 * 少这一行，约定插件里所有 `libs.*` 都会报找不到。
 */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
