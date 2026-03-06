package com.sls.devdrill.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object WelcomeRoute

@Serializable
data class CategoryRoute(
    val categoryId: String,
    val categoryName: String,
)

@Serializable
object TtlCacheRoute

@Serializable
object GalleryRoute

@Serializable
object FeverRoute

@Serializable
object FactoryMethodRoute

@Serializable
object AbstractFactoryRoute

@Serializable
object PrototypeRoute

@Serializable
object AdapterPatternRoute

@Serializable
object DecoratorRoute

@Serializable
object FacadeRoute

@Serializable
object ObserverRoute

@Serializable
object StrategyRoute

@Serializable
object CommandRoute

@Serializable
object StateMachineRoute
