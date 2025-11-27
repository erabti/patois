package io.github.erabti.patois.plugin.parsers

import io.github.erabti.patois.plugin.application.parsers.YamlInputParser
import io.github.erabti.patois.plugin.models.TranslationNode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class YamlInputReaderTest : FunSpec({
    val reader = YamlInputParser()

    context("Basic scalar values") {
        test("should parse simple scalar values") {
            val yaml = """
                hello: "Hello World"
                goodbye: "Goodbye"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 2

            val helloNode = nodes.first().shouldBeInstanceOf<TranslationNode.LeafNode>()
            helloNode.key shouldBe "hello"
            helloNode.value shouldBe "Hello World"

            val goodbyeNode = nodes[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            goodbyeNode.key shouldBe "goodbye"
            goodbyeNode.value shouldBe "Goodbye"
        }

        test("should handle numbers and booleans as strings") {
            val yaml = """
                count: 42
                enabled: true
                price: 19.99
                negative: -5
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 4

            val countNode = nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            countNode.key shouldBe "count"
            countNode.value shouldBe "42"

            val enabledNode = nodes[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            enabledNode.key shouldBe "enabled"
            enabledNode.value shouldBe "true"

            val priceNode = nodes[2].shouldBeInstanceOf<TranslationNode.LeafNode>()
            priceNode.key shouldBe "price"
            priceNode.value shouldBe "19.99"

            val negativeNode = nodes[3].shouldBeInstanceOf<TranslationNode.LeafNode>()
            negativeNode.key shouldBe "negative"
            negativeNode.value shouldBe "-5"
        }

        test("should handle empty strings") {
            val yaml = """
                empty: ""
                blank: "   "
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 2

            val emptyNode = nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            emptyNode.key shouldBe "empty"
            emptyNode.value shouldBe ""

            val blankNode = nodes[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            blankNode.key shouldBe "blank"
            blankNode.value shouldBe "   "
        }

        test("should handle strings with special characters") {
            val yaml = """
                special: "Hello, {name}!"
                interpolation: "Count: %d items"
                newline: "Line 1\nLine 2"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 3

            nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "Hello, {name}!"
            nodes[1].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "Count: %d items"
            nodes[2].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "Line 1\nLine 2"
        }
    }

    context("Map structures") {
        test("should parse nested maps") {
            val yaml = """
                welcome:
                  title: "Welcome to App"
                  subtitle: "Get started now"
                errors:
                  notFound: "404 Not Found"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 2

            val welcomeNode = nodes[0].shouldBeInstanceOf<TranslationNode.MapNode>()
            welcomeNode.key shouldBe "welcome"
            welcomeNode.children.size shouldBe 2

            val titleNode = welcomeNode.children[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            titleNode.key shouldBe "title"
            titleNode.value shouldBe "Welcome to App"

            val subtitleNode = welcomeNode.children[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            subtitleNode.key shouldBe "subtitle"
            subtitleNode.value shouldBe "Get started now"

            val errorsNode = nodes[1].shouldBeInstanceOf<TranslationNode.MapNode>()
            errorsNode.key shouldBe "errors"
            errorsNode.children.size shouldBe 1

            val notFoundNode = errorsNode.children[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            notFoundNode.key shouldBe "notFound"
            notFoundNode.value shouldBe "404 Not Found"
        }

        test("should parse deeply nested map structures") {
            val yaml = """
                app:
                  navigation:
                    menu:
                      home: "Home"
                      about: "About"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 1

            val appNode = nodes[0].shouldBeInstanceOf<TranslationNode.MapNode>()
            appNode.key shouldBe "app"

            val navigationNode = appNode.children[0].shouldBeInstanceOf<TranslationNode.MapNode>()
            navigationNode.key shouldBe "navigation"

            val menuNode = navigationNode.children[0].shouldBeInstanceOf<TranslationNode.MapNode>()
            menuNode.key shouldBe "menu"
            menuNode.children.size shouldBe 2

            val homeNode = menuNode.children[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            homeNode.key shouldBe "home"
            homeNode.value shouldBe "Home"

            val aboutNode = menuNode.children[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            aboutNode.key shouldBe "about"
            aboutNode.value shouldBe "About"
        }

        test("should handle empty maps") {
            val yaml = """
                emptyMap: {}
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 1

            val emptyMapNode = nodes[0].shouldBeInstanceOf<TranslationNode.MapNode>()
            emptyMapNode.key shouldBe "emptyMap"
            emptyMapNode.children.size shouldBe 0
        }
    }

    context("List structures") {
        test("should parse simple lists") {
            val yaml = """
                items:
                  - "First"
                  - "Second"
                  - "Third"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 1

            val itemsNode = nodes[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            itemsNode.key shouldBe "items"
            itemsNode.items.size shouldBe 3

            val firstItem = itemsNode.items[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            firstItem.key shouldBe "0"
            firstItem.value shouldBe "First"

            val secondItem = itemsNode.items[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            secondItem.key shouldBe "1"
            secondItem.value shouldBe "Second"

            val thirdItem = itemsNode.items[2].shouldBeInstanceOf<TranslationNode.LeafNode>()
            thirdItem.key shouldBe "2"
            thirdItem.value shouldBe "Third"
        }

        test("should parse empty lists") {
            val yaml = """
                emptyList: []
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 1

            val emptyListNode = nodes[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            emptyListNode.key shouldBe "emptyList"
            emptyListNode.items.size shouldBe 0
        }

        test("should parse single-item lists") {
            val yaml = """
                singleItem:
                  - "Only One"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 1

            val singleItemNode = nodes[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            singleItemNode.key shouldBe "singleItem"
            singleItemNode.items.size shouldBe 1

            val item = singleItemNode.items[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            item.key shouldBe "0"
            item.value shouldBe "Only One"
        }
    }

    context("Complex nested structures") {
        test("should parse list containing maps") {
            val yaml = """
                users:
                  - name: "Alice"
                    role: "Admin"
                  - name: "Bob"
                    role: "User"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 1

            val usersNode = nodes[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            usersNode.key shouldBe "users"
            usersNode.items.size shouldBe 2

            // First user
            val firstUser = usersNode.items[0].shouldBeInstanceOf<TranslationNode.MapNode>()
            firstUser.key shouldBe "0"
            firstUser.children.size shouldBe 2

            val aliceName = firstUser.children[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            aliceName.key shouldBe "name"
            aliceName.value shouldBe "Alice"

            val aliceRole = firstUser.children[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            aliceRole.key shouldBe "role"
            aliceRole.value shouldBe "Admin"

            // Second user
            val secondUser = usersNode.items[1].shouldBeInstanceOf<TranslationNode.MapNode>()
            secondUser.key shouldBe "1"
            secondUser.children.size shouldBe 2

            val bobName = secondUser.children[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            bobName.key shouldBe "name"
            bobName.value shouldBe "Bob"

            val bobRole = secondUser.children[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            bobRole.key shouldBe "role"
            bobRole.value shouldBe "User"
        }

        test("should parse map containing lists") {
            val yaml = """
                menu:
                  items:
                    - "Home"
                    - "About"
                    - "Contact"
                  footer: "Copyright 2024"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 1

            val menuNode = nodes[0].shouldBeInstanceOf<TranslationNode.MapNode>()
            menuNode.key shouldBe "menu"
            menuNode.children.size shouldBe 2

            // List of items
            val itemsNode = menuNode.children[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            itemsNode.key shouldBe "items"
            itemsNode.items.size shouldBe 3

            itemsNode.items[0].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "Home"
            itemsNode.items[1].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "About"
            itemsNode.items[2].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "Contact"

            // Footer scalar
            val footerNode = menuNode.children[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            footerNode.key shouldBe "footer"
            footerNode.value shouldBe "Copyright 2024"
        }

        test("should parse deeply nested mixed structures: list -> map -> list -> scalar") {
            val yaml = """
                data:
                  - tags:
                      - "kotlin"
                      - "yaml"
                    count: "5"
                  - tags:
                      - "java"
                    count: "3"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 1

            val dataNode = nodes[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            dataNode.key shouldBe "data"
            dataNode.items.size shouldBe 2

            // First item: map with tags list and count
            val firstItem = dataNode.items[0].shouldBeInstanceOf<TranslationNode.MapNode>()
            firstItem.key shouldBe "0"
            firstItem.children.size shouldBe 2

            val firstTags = firstItem.children[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            firstTags.key shouldBe "tags"
            firstTags.items.size shouldBe 2
            firstTags.items[0].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "kotlin"
            firstTags.items[1].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "yaml"

            val firstCount = firstItem.children[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            firstCount.key shouldBe "count"
            firstCount.value shouldBe "5"

            // Second item: map with tags list and count
            val secondItem = dataNode.items[1].shouldBeInstanceOf<TranslationNode.MapNode>()
            secondItem.key shouldBe "1"
            secondItem.children.size shouldBe 2

            val secondTags = secondItem.children[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            secondTags.key shouldBe "tags"
            secondTags.items.size shouldBe 1
            secondTags.items[0].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "java"

            val secondCount = secondItem.children[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            secondCount.key shouldBe "count"
            secondCount.value shouldBe "3"
        }

        test("should parse nested lists (list of lists)") {
            val yaml = """
                matrix:
                  - - "a1"
                    - "a2"
                  - - "b1"
                    - "b2"
            """.trimIndent()

            val nodes = reader.read(yaml)

            nodes.size shouldBe 1

            val matrixNode = nodes[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            matrixNode.key shouldBe "matrix"
            matrixNode.items.size shouldBe 2

            // First inner list
            val firstRow = matrixNode.items[0].shouldBeInstanceOf<TranslationNode.ListNode>()
            firstRow.key shouldBe "0"
            firstRow.items.size shouldBe 2
            firstRow.items[0].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "a1"
            firstRow.items[1].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "a2"

            // Second inner list
            val secondRow = matrixNode.items[1].shouldBeInstanceOf<TranslationNode.ListNode>()
            secondRow.key shouldBe "1"
            secondRow.items.size shouldBe 2
            secondRow.items[0].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "b1"
            secondRow.items[1].shouldBeInstanceOf<TranslationNode.LeafNode>().value shouldBe "b2"
        }
    }

    context("Error cases") {
        test("should throw error when root is not a map") {
            val yaml = """
                - "item1"
                - "item2"
            """.trimIndent()

            val exception = shouldThrow<IllegalArgumentException> {
                reader.read(yaml)
            }

            exception.message shouldContain "Root YAML element must be a map"
        }

        test("should throw error when root is a scalar") {
            val yaml = "just a string"

            val exception = shouldThrow<IllegalArgumentException> {
                reader.read(yaml)
            }

            exception.message shouldContain "Root YAML element must be a map"
        }
    }

    context("Template argument extraction") {
        test("should extract single curly brace argument") {
            val yaml = """
                greeting: "Hello, {name}!"
            """.trimIndent()

            val nodes = reader.read(yaml)
            val node = nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>()

            node.arguments.size shouldBe 1
            node.arguments[0].name shouldBe "name"
            node.arguments[0].index shouldBe 0
        }

        test("should extract multiple arguments in order") {
            val yaml = """
                message: "Hello {firstName} {lastName}, you have {count} items"
            """.trimIndent()

            val nodes = reader.read(yaml)
            val node = nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>()

            node.arguments.size shouldBe 3
            node.arguments[0].name shouldBe "firstName"
            node.arguments[0].index shouldBe 0
            node.arguments[1].name shouldBe "lastName"
            node.arguments[1].index shouldBe 1
            node.arguments[2].name shouldBe "count"
            node.arguments[2].index shouldBe 2
        }

        test("should handle strings with no arguments") {
            val yaml = """
                simple: "Just a string"
            """.trimIndent()

            val nodes = reader.read(yaml)
            val node = nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>()

            node.arguments shouldBe emptyList()
        }

        test("should handle duplicate argument names") {
            val yaml = """
                repeated: "Hello {name}, goodbye {name}"
            """.trimIndent()

            val nodes = reader.read(yaml)
            val node = nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>()

            node.arguments.size shouldBe 2
            node.arguments[0].name shouldBe "name"
            node.arguments[0].index shouldBe 0
            node.arguments[1].name shouldBe "name"
            node.arguments[1].index shouldBe 1
        }

        test("should extract arguments from nested structures") {
            val yaml = """
                welcome:
                  greeting: "Hello, {name}!"
                  personalizedGreeting: "Welcome back, {firstName} {lastName}!"
            """.trimIndent()

            val nodes = reader.read(yaml)

            val welcomeNode = nodes[0].shouldBeInstanceOf<TranslationNode.MapNode>()
            welcomeNode.children.size shouldBe 2

            val greetingNode = welcomeNode.children[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            greetingNode.arguments.size shouldBe 1
            greetingNode.arguments[0].name shouldBe "name"

            val personalizedNode = welcomeNode.children[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            personalizedNode.arguments.size shouldBe 2
            personalizedNode.arguments[0].name shouldBe "firstName"
            personalizedNode.arguments[1].name shouldBe "lastName"
        }

        test("should handle arguments with underscores and numbers") {
            val yaml = """
                message: "User {user_id} has {item_count_1} items"
            """.trimIndent()

            val nodes = reader.read(yaml)
            val node = nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>()

            node.arguments.size shouldBe 2
            node.arguments[0].name shouldBe "user_id"
            node.arguments[1].name shouldBe "item_count_1"
        }

        test("should ignore malformed placeholders") {
            val yaml = """
                message: "Hello {name} and { invalid } and {}"
            """.trimIndent()

            val nodes = reader.read(yaml)
            val node = nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>()

            node.arguments.size shouldBe 1
            node.arguments[0].name shouldBe "name"
        }

        test("should handle arguments in list items") {
            val yaml = """
                messages:
                  - "Hello {name}"
                  - "Goodbye {name}"
            """.trimIndent()

            val nodes = reader.read(yaml)
            val listNode = nodes[0].shouldBeInstanceOf<TranslationNode.ListNode>()

            val firstItem = listNode.items[0].shouldBeInstanceOf<TranslationNode.LeafNode>()
            firstItem.arguments.size shouldBe 1
            firstItem.arguments[0].name shouldBe "name"

            val secondItem = listNode.items[1].shouldBeInstanceOf<TranslationNode.LeafNode>()
            secondItem.arguments.size shouldBe 1
            secondItem.arguments[0].name shouldBe "name"
        }

        test("should preserve original value with placeholders") {
            val yaml = """
                greeting: "Hello, {name}! You have {count} messages."
            """.trimIndent()

            val nodes = reader.read(yaml)
            val node = nodes[0].shouldBeInstanceOf<TranslationNode.LeafNode>()

            node.value shouldBe "Hello, {name}! You have {count} messages."
            node.arguments.size shouldBe 2
        }
    }
})
