package com.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable


@Serializable
data class Ingredients(val id: Int, val name: String, val quantity: Double?, val unit: String?)
@Serializable
data class Recipes(val id: Int, val name: String, val instructions: String, val ingredients: List<Ingredients>)
@Serializable
data class RecipesRequest(val name: String, val instructions: String, val ingredients: List<IngredientsRequest>? = null)
@Serializable
data class IngredientsRequest(val id: Int? = null, val name: String, val quantity: Double?, val unit: String?)

object Repository {
    private var nextRecipeId = 1
    val recipes = mutableListOf<Recipes>(

    )

    // เพิ่มฟังก์ชันนี้เข้ามาเพื่อรีเซ็ตสถานะของ Repository สำหรับการทดสอบ
    fun reset() {
        recipes.clear()
        nextRecipeId = 1

        addRecipe(
            RecipesRequest(
                name = "Fried Egg",
                instructions = "Fried Egg",
                ingredients = listOf(
                    IngredientsRequest(name = "Egg", quantity = 2.0, unit = "units"),
                )
            )
        )
        addRecipe(
            RecipesRequest(
                name = "Chicken Soup",
                instructions = "Boil chicken with vegetables.",
                ingredients = listOf(
                    IngredientsRequest(name = "Chicken", quantity = 500.0, unit = "g"),
                    IngredientsRequest(name = "Carrot", quantity = 100.0, unit = "g")
                )
            )
        )
    }

    private fun getNextIngredientIdForList(ingredients: List<Ingredients>): Int {
        var maxId = 0
        for (ingredient in ingredients) {
            if (ingredient.id > maxId) {
                maxId = ingredient.id
            }
        }
        return maxId + 1
    }

    fun getAllRecipes(): List<Recipes> {
        return recipes
    }

    fun getRecipeById(id: Int): Recipes? { //
        return recipes.firstOrNull { it.id == id }
    }


    fun addRecipe(recipesRequest: RecipesRequest): Recipes {
        val recipeId = nextRecipeId++
        val ingredientsList = recipesRequest.ingredients?.mapIndexed { index, reqIngredient ->
            Ingredients(
                id = index + 1,
                name = reqIngredient.name,
                quantity = reqIngredient.quantity,
                unit = reqIngredient.unit
            )
        } ?: emptyList()

        val newRecipe = Recipes(
            id = recipeId,
            name = recipesRequest.name,
            instructions = recipesRequest.instructions,
            ingredients = ingredientsList
        )
        recipes.add(newRecipe)
        return newRecipe
    }

    fun updateRecipe(id: Int, updatedRecipesRequest: RecipesRequest): Recipes? {
        val index = recipes.indexOfFirst { it.id == id }
        if (index == -1) {
            return null
        }
        val existingRecipe = recipes[index]

        val finalIngredientsList: List<Ingredients>

        if (updatedRecipesRequest.ingredients == null) {
            finalIngredientsList = existingRecipe.ingredients
        } else if (updatedRecipesRequest.ingredients.isEmpty()) {
            finalIngredientsList = emptyList()
        } else {
            val mergedIngredientsMap = existingRecipe.ingredients.associateBy { it.id }.toMutableMap()

            for (reqIngredient in updatedRecipesRequest.ingredients) {
                if (reqIngredient.id != null) {
                    mergedIngredientsMap[reqIngredient.id] = Ingredients(
                        id = reqIngredient.id,
                        name = reqIngredient.name,
                        quantity = reqIngredient.quantity,
                        unit = reqIngredient.unit
                    )
                } else {
                    val newId = getNextIngredientIdForList(mergedIngredientsMap.values.toList())
                    mergedIngredientsMap[newId] = Ingredients(
                        id = newId,
                        name = reqIngredient.name,
                        quantity = reqIngredient.quantity,
                        unit = reqIngredient.unit
                    )
                }
            }
            finalIngredientsList = mergedIngredientsMap.values.toList()
        }

        val updatedRecipe = existingRecipe.copy(
            name = updatedRecipesRequest.name,
            instructions = updatedRecipesRequest.instructions,
            ingredients = finalIngredientsList
        )
        recipes[index] = updatedRecipe
        return updatedRecipe
    }


    fun deleteRecipe(id: Int): Boolean {
        return recipes.removeIf { it.id == id }
    }

    fun addIngredientToRecipe(recipeId: Int, ingredientRequest: IngredientsRequest): Ingredients? {
        val recipe = getRecipeById(recipeId)
        return if (recipe != null) {
            val newIngredientId = getNextIngredientIdForList(recipe.ingredients)
            val newIngredient = Ingredients(
                id = newIngredientId,
                name = ingredientRequest.name,
                quantity = ingredientRequest.quantity,
                unit = ingredientRequest.unit
            )
            val updatedIngredients = recipe.ingredients + newIngredient
            val updatedRecipe = recipe.copy(ingredients = updatedIngredients)
            val index = recipes.indexOfFirst { it.id == recipeId }
            if (index != -1) {
                recipes[index] = updatedRecipe
            }
            newIngredient
        } else {
            null
        }
    }

    fun updateIngredientInRecipe(recipeId: Int, ingredientId: Int, ingredientRequest: IngredientsRequest): Ingredients? {
        val recipe = getRecipeById(recipeId)
        return if (recipe != null) {
            val index = recipe.ingredients.indexOfFirst { it.id == ingredientId }
            if (index != -1) {
                val updatedIngredient = recipe.ingredients[index].copy(
                    id = ingredientId,
                    name = ingredientRequest.name,
                    quantity = ingredientRequest.quantity,
                    unit = ingredientRequest.unit
                )
                val updatedIngredients = recipe.ingredients.toMutableList()
                updatedIngredients[index] = updatedIngredient
                val updatedRecipe = recipe.copy(ingredients = updatedIngredients)
                val recipeIndex = recipes.indexOfFirst { it.id == recipeId }
                if (recipeIndex != -1) {
                    recipes[recipeIndex] = updatedRecipe
                }
                updatedIngredient
            } else {
                null
            }
        } else {
            null
        }
    }

    fun deleteIngredientFromRecipe(recipeId: Int, ingredientId: Int): Boolean {
        val recipe = getRecipeById(recipeId)
        return if (recipe != null) {
            val initialSize = recipe.ingredients.size
            val updatedIngredients = recipe.ingredients.filter { it.id != ingredientId }
            if (updatedIngredients.size < initialSize) {
                val updatedRecipe = recipe.copy(ingredients = updatedIngredients)
                val recipeIndex = recipes.indexOfFirst { it.id == recipeId }
                if (recipeIndex != -1) {
                    recipes[recipeIndex] = updatedRecipe
                }
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun searchRecipesByIngredientName(ingredientName: String): List<Recipes> {
        val lowerCaseSearchTerm = ingredientName.lowercase()
        return recipes.filter { recipe ->
            recipe.ingredients.any { ingredient ->
                ingredient.name.lowercase().contains(lowerCaseSearchTerm)
            }
        }
    }
}




fun Application.configureRouting() {
    routing {
        get("/recipes") {
            val allRecipes = Repository.getAllRecipes()
            call.respond(allRecipes)

        }
        post("/recipes") {
            val recipesRequest = call.receive<RecipesRequest>()
            val newRecipe = Repository.addRecipe(recipesRequest)
            call.respond(HttpStatusCode.Created, newRecipe)
        }

        put("/recipes/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid recipe ID format")
                return@put
            }
            val recipesRequest = call.receive<RecipesRequest>()


            val updatedRecipe = Repository.updateRecipe(id, recipesRequest)
            if (updatedRecipe != null) {
                call.respond(HttpStatusCode.OK, updatedRecipe)
            } else {
                call.respond(HttpStatusCode.NotFound, "Recipe with ID $id not found")
            }
        }

        delete("/recipes/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid recipe ID format")
                return@delete
            }
            if (Repository.deleteRecipe(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Recipe with ID $id not found")
            }
        }

        get("/recipes/search") {
            val ingredientName = call.request.queryParameters["ingredient"]
            if (ingredientName == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing 'ingredient' query parameter")
                return@get
            }
            val foundRecipes = Repository.searchRecipesByIngredientName(ingredientName)
            call.respond(HttpStatusCode.OK, foundRecipes)
        }

        post("/recipes/{recipeId}/ingredients"){
            val recipeId = call.parameters["recipeId"]?.toIntOrNull()
            if (recipeId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid recipe ID format")
                return@post
            }
            val ingredientRequest = call.receive<IngredientsRequest>()

            val newIngredient = Repository.addIngredientToRecipe(recipeId, ingredientRequest)
            if (newIngredient != null) {
                call.respond(HttpStatusCode.Created, newIngredient)
            } else {
                call.respond(HttpStatusCode.NotFound, "Recipe with ID $recipeId not found")
            }

        }

        put("/recipes/{recipeId}/ingredients/{ingredientId}"){
            val recipeId = call.parameters["recipeId"]?.toIntOrNull()
            val ingredientId = call.parameters["ingredientId"]?.toIntOrNull()

            if (recipeId == null || ingredientId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format for recipe or ingredient")
                return@put
            }
            val ingredientRequest = call.receive<IngredientsRequest>() // no try-catch

            val updatedIngredient = Repository.updateIngredientInRecipe(recipeId, ingredientId, ingredientRequest)
            if (updatedIngredient != null) {
                call.respond(HttpStatusCode.OK, updatedIngredient)
            } else {
                call.respond(HttpStatusCode.NotFound, "Ingredient with ID $ingredientId not found in Recipe ID $recipeId or Recipe not found")
            }
        }

        delete("/recipes/{recipeId}/ingredients/{ingredientId}") {
            val recipeId = call.parameters["recipeId"]?.toIntOrNull()
            val ingredientId = call.parameters["ingredientId"]?.toIntOrNull()

            if (recipeId == null || ingredientId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format for recipe or ingredient")
                return@delete
            }

            if (Repository.deleteIngredientFromRecipe(recipeId, ingredientId)) {
                call.respond(HttpStatusCode.NoContent) // 204 No Content for successful deletion
            } else {
                call.respond(HttpStatusCode.NotFound, "Ingredient with ID $ingredientId not found in Recipe ID $recipeId")
            }
        }
    }
}



