tasks.register("a") { println("Task A") }
tasks.register("b") { println("Task B") }
tasks.register("c") { println("Task C") }
tasks.named("a").configure { dependsOn("b") }

