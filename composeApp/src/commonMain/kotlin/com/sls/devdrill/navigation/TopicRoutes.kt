package com.sls.devdrill.navigation

data class TopicItem(
    val id: String,
    val title: String,
)

fun topicRoute(topicId: String): Any? = when (topicId) {
    "ttl_cache" -> TtlCacheRoute
    "gallery" -> GalleryRoute
    "fever" -> FeverRoute
    "factory_method" -> FactoryMethodRoute
    "abstract_factory" -> AbstractFactoryRoute
    "prototype" -> PrototypeRoute
    "adapter_pattern" -> AdapterPatternRoute
    "decorator" -> DecoratorRoute
    "facade" -> FacadeRoute
    "observer" -> ObserverRoute
    "strategy" -> StrategyRoute
    "command" -> CommandRoute
    "state_machine" -> StateMachineRoute
    else -> null
}

fun topicsForCategory(categoryId: String): List<TopicItem> = when (categoryId) {
    "caching" -> listOf(
        TopicItem(id = "ttl_cache", title = "TTL Cache"),
    )
    "ui" -> listOf(
        TopicItem(id = "gallery", title = "Gallery"),
        TopicItem(id = "fever", title = "Fever"),
    )
    "creational_patterns" -> listOf(
        TopicItem(id = "factory_method", title = "Factory Method"),
        TopicItem(id = "abstract_factory", title = "Abstract Factory"),
        TopicItem(id = "prototype", title = "Prototype"),
    )
    "structural_patterns" -> listOf(
        TopicItem(id = "adapter_pattern", title = "Adapter"),
        TopicItem(id = "decorator", title = "Decorator"),
        TopicItem(id = "facade", title = "Facade"),
    )
    "behavioral_patterns" -> listOf(
        TopicItem(id = "observer", title = "Observer"),
        TopicItem(id = "strategy", title = "Strategy"),
        TopicItem(id = "command", title = "Command"),
        TopicItem(id = "state_machine", title = "State Machine"),
    )
    else -> emptyList()
}
